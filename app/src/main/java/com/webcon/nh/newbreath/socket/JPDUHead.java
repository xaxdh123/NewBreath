
package com.webcon.nh.newbreath.socket;

import java.io.InputStream;
import java.io.OutputStream;

public class JPDUHead {

    private static final int HEAD_LEN = 16;
    private int m_nBodyLen;
    private short m_nDataType;
    private int m_nVerifty;
    private short m_nVersion;
    private short m_nPduType;
    private short m_subPduType;

    public JPDUHead() {
        m_nBodyLen = 0;
        m_nDataType = 0;
        m_nVerifty = 0;
        m_nVersion = 0x0101;
        m_nPduType = 105;
        m_subPduType = 0;
    }


    /**
     * read() 将完整的输入流读入当前的包头中
     * @param in 符合格式的完整包头数据流
     * @return int 判断成功与否，并将输入流赋值给给字段
     */
    public int read(InputStream in) {
        try {
            byte[] szBuf = new byte[HEAD_LEN];
            int nRead = in.read(szBuf, 0, szBuf.length);
            if (nRead == -1)
                return -1;
            this.m_nBodyLen = JTools.Bytes4ToInt(szBuf, 0);
            if (this.m_nBodyLen == 0) {
                return -1;
            }
            this.m_nDataType = JTools.Bytes2ToShort(szBuf, 4);
            this.m_nVerifty = JTools.Bytes4ToInt(szBuf, 6);
            this.m_nVersion = JTools.Bytes2ToShort(szBuf, 10);
            this.m_nPduType = JTools.Bytes2ToShort(szBuf, 12);
            this.m_subPduType = JTools.Bytes2ToShort(szBuf, 14);
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    /**
     * write（）将当前的包头写入到输出流中
     * @param out 将要输出的输出流
     * @return int 判断成与否
     */
    public int write(OutputStream out) {
        try {
            byte szRet[] = new byte[HEAD_LEN];
            m_nVerifty = (m_nBodyLen + m_nDataType + m_nVersion) - m_nPduType;
            JTools.IntToBytes4(m_nBodyLen, szRet, 0);
            JTools.ShortToBytes2(m_nDataType, szRet, 4);
            JTools.IntToBytes4(m_nVerifty, szRet, 6);
            JTools.ShortToBytes2(m_nVersion, szRet, 10);
            JTools.ShortToBytes2(m_nPduType, szRet, 12);
            JTools.ShortToBytes2(m_subPduType, szRet, 14);

            out.write(szRet);
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    /**
     * headToString
     * @return 输出 包头字符串
     */
    public String headToString() {
        return "BodyLen=" + Integer.toString(m_nBodyLen) + " DataType="
                + Short.toString(m_nDataType) + " Verifty="
                + Integer.toString(m_nVerifty) + " Version="
                + Short.toString(m_nVersion) + " PduType="
                + Short.toString(m_nPduType);
    }

    public int getM_nBodyLen() {
        return m_nBodyLen;
    }

    public void setM_nBodyLen(int m_nBodyLen) {
        this.m_nBodyLen = m_nBodyLen;
    }

    public short getM_nDataType() {
        return m_nDataType;
    }

    public void setM_nDataType(short m_nDataType) {
        this.m_nDataType = m_nDataType;
    }

    public short getM_nVersion() {
        return m_nVersion;
    }

    public void setM_nVersion(short m_nVersion) {
        this.m_nVersion = m_nVersion;
    }

    public short getM_nPduType() {
        return m_nPduType;
    }

    public void setM_nPduType(short m_nPduType) {
        this.m_nPduType = m_nPduType;
    }

    public short getM_subPduType() {
        return m_subPduType;
    }

    public void setM_subPduType(short m_subPduType) {
        this.m_subPduType = m_subPduType;
    }
}
