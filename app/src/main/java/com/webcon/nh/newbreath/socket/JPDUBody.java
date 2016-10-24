package com.webcon.nh.newbreath.socket;

import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class JPDUBody {

    private byte m_szData[];
    private int setIndex;
    private int getIndex;
    private int PDUBODYLENGTH;

    public JPDUBody() {
        m_szData = null;
        getIndex = 0;
        PDUBODYLENGTH = 1024 * 100;
        m_szData = new byte[PDUBODYLENGTH];
        setIndex = 0;
    }

    public JPDUBody(int len) {
        PDUBODYLENGTH = len;
    }

    public int getPDUBODYLENGTH() {
        return PDUBODYLENGTH;
    }

    /**
     * 改变 body的大小
     *
     * @param pDUBODYLENGTH 缓存池 的大小 默认 1024*72 byte （72K）
     */
    public void setPDUBODYLENGTH(int pDUBODYLENGTH) {
        PDUBODYLENGTH = pDUBODYLENGTH;
    }

    public int getLength() {
        return setIndex;
    }

    /**
     * read() 将完整的输入流读入当前的body中
     *
     * @param in 符合格式的完整body数据流
     * @return int 判断成功与否，并将输入流赋值给给字段
     */
    public int read(InputStream in) {
        for (int i = 0; i < this.PDUBODYLENGTH; i++) {
            this.m_szData[i] = -1;  //将原body清空，并转成int[] 值为全-1
        }
        try {
            int a = 0, b = 0;
            while (b != this.PDUBODYLENGTH) {
                a = in.read(this.m_szData, b, this.PDUBODYLENGTH - b); //将输入流中的数据按4k的大小一次读取存入body
                if (a == -1) {
                    return -1;
                }
                b = a + b;
            }
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    /**
     * write（）将当前的body写入到输出流中
     *
     * @param out 将要输出的输出流
     * @return int 判断成与否
     */
    public int write(OutputStream out) {
        try {
            out.write(m_szData, 0, setIndex);
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    /**
     * pushData()
     * 分类将不同类型数据传入数据包
     *
     * @param obj 包含 Byte Short Int String File
     */
    public void pushData(Object obj) {
        if (obj instanceof Byte) {
            m_szData[setIndex] = (byte) obj;
            setIndex++;
        } else if (obj instanceof Short) {
            JTools.ShortToBytes2((short) obj, m_szData, setIndex);
            setIndex += 2;
        } else if (obj instanceof Integer) {
            JTools.IntToBytes4((int) obj, m_szData, setIndex);
            setIndex += 4;
        } else if (obj instanceof String) {
            try {
                if (FreeApp.getInstance().getEncodingCode() != null && !FreeApp.getInstance().getEncodingCode().equals("")) {
                    JTools.StringToBytes((String) obj, m_szData, setIndex, ((String) obj).getBytes(FreeApp.getInstance().getEncodingCode()).length);
                    setIndex += ((String) obj).getBytes(FreeApp.getInstance().getEncodingCode()).length + 1;
                } else {
                    JTools.StringToBytes((String) obj, m_szData, setIndex, ((String) obj).getBytes().length);
                    setIndex += ((String) obj).getBytes().length + 1;
                }
                m_szData[setIndex] = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte getByte() {
        if (getIndex > PDUBODYLENGTH) {
            return 0;
        } else {
            byte r = m_szData[getIndex];
            getIndex++;
            return r;
        }
    }

    public int getInt() {
        if (getIndex > PDUBODYLENGTH) {
            return 0;
        } else {
            int r = JTools.Bytes4ToInt(m_szData, getIndex);
            getIndex += 4;
            return r;
        }
    }

    public short GetshResult() {
        if (getIndex > PDUBODYLENGTH) {
            return 0;
        } else {
            short r = JTools.Bytes2ToShort(m_szData, getIndex);
            getIndex += 2;
            return r;
        }
    }

    public short getShort() {
        if (getIndex > PDUBODYLENGTH) {
            System.out
                    .println(getIndex + ",------------------" + PDUBODYLENGTH);
            return -400;
        } else {
            short r = JTools.Bytes2ToShort(m_szData, getIndex);
            getIndex += 2;
            return r;
        }
    }

    public String getString() {
        if (getIndex > PDUBODYLENGTH)
            return null;
        String s = JTools.toString(m_szData, getIndex);
        if (FreeApp.getInstance().getEncodingCode() != null && !FreeApp.getInstance().getEncodingCode().equals(""))
            try {
                getIndex += s.getBytes(FreeApp.getInstance().getEncodingCode()).length + 1;
            } catch (UnsupportedEncodingException e) {
                getIndex += s.getBytes().length + 1;
                e.printStackTrace();
            }
        else
            getIndex += s.getBytes().length + 1;
        return s;
    }

    public int getWriteindex() {
        return setIndex;
    }

    public void readSkip(int a) {
        getIndex += a;
    }

    public void setIndex() {
        setIndex = 0;

    }
}
