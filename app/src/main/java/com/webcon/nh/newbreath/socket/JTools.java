package com.webcon.nh.newbreath.socket;

import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVA数据类型和二进制转换的工具类
 *
 * @author Vieboo
 */
public class JTools {
    private static boolean NEEDTRANS = false; // Language character convert



    public static short Bytes2ToShort(byte mybytes[], int nOff) {
        return (short) ((0xff & mybytes[nOff]) << 8 | 0xff & mybytes[nOff + 1]);
    }

    public static int Bytes4ToInt(byte mybytes[], int nOff) {
        try {
            return (0xff & mybytes[nOff]) << 24 | (0xff & mybytes[nOff + 1]) << 16
                    | (0xff & mybytes[nOff + 2]) << 8 | 0xff & mybytes[nOff + 3];
        } catch (Exception e) {
            return 0;    // return 0--> 处理失败
        }
    }

    public static void ShortToBytes2(short i, byte mybytes[], int nOff) {
        mybytes[nOff + 1] = (byte) (0xff & i);
        mybytes[nOff] = (byte) ((0xff00 & i) >> 8);
    }

    /*
     * 32 Integer to byte array every 8 bits
     */
    public static void IntToBytes4(int i, byte mybytes[], int nOff) {

        mybytes[nOff + 3] = (byte) (0xff & i);
        mybytes[nOff + 2] = (byte) ((0xff00 & i) >> 8);
        mybytes[nOff + 1] = (byte) ((0xff0000 & i) >> 16);
        mybytes[nOff] = (byte) (int) (((long) 0xff000000 & (long) i) >> 24);
    }

    /**
     * Integer >> to get ip address
     *
     * @param a int
     * @return String
     */
    public static String int4ToIP(int a) {
        return ((a & 0xff000000) >>> 24) + "." + ((a & 0x00ff0000) >>> 16)
                + "." + ((a & 0x0000ff00) >>> 8) + "." + (a & 0x000000ff);
    }

    /**
     * Integer radio flag
     *
     * @param a    int
     * @param nLen int
     * @return String
     */
    public static String int2Bitmap(int a, int nLen) {
        if (nLen <= 0 || a <= 0)
            return "0";

        String str = Integer.toBinaryString(a);
        int str_len = str.length();

        if (str_len > nLen)
            return "0";

        while (str_len < nLen) {
            str = "0" + str;
            str_len++;
        }

        if (str_len > nLen) {
            str = str.substring(str_len - nLen, str_len);
        }

        return (str);
    }

    public static void StringToBytes(String strSource, byte ayRet[], int nOff,
                                     int nLen) throws Exception {
        for (int i = 0; i < nLen; i++)
            ayRet[nOff + i] = 0;

        if (strSource != null) {
            int nCopy = 0;
            if (FreeApp.getInstance().getEncodingCode() != null
                    && !FreeApp.getInstance().getEncodingCode().equals("")) {
                nCopy = strSource.getBytes().length;
                if (nCopy > nLen)
                    nCopy = nLen;
                System.arraycopy(strSource.getBytes(FreeApp.getInstance().getEncodingCode()), 0, ayRet, nOff, nCopy);
            } else {
                nCopy = strSource.getBytes().length;
                if (nCopy > nLen)
                    nCopy = nLen;
                System.arraycopy(strSource.getBytes(), 0, ayRet, nOff, nCopy);
            }
        }
    }

    public static String tranStr(String str) throws Exception {
        if (NEEDTRANS)
            return (new String(str.getBytes("iso8859-1"), "GBK"));
        else
            return str;
    }




    public static void main(String[] args) {
        if (args[0].equals("help"))
            System.out
                    .println("usage: java -cp ./ com.webcon.util.JTools.int2Bitmap YourInt bitlength");
        else
            System.out.println(int2Bitmap(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])));
    }

    /**
     * byte array =>String ends with '\0'
     *
     * @param aySource byte[]
     * @param nOff     int
     * @return String
     */
    public static String toString(byte[] aySource, int nOff) {
        int i = nOff;
        while (i < aySource.length && aySource[i] != 0)
            i++;

        try {
            if (FreeApp.getInstance().getEncodingCode() != null
                    && (!FreeApp.getInstance().getEncodingCode().equals(""))) {
                return new String(aySource, nOff, i - nOff,
                        FreeApp.getInstance().getEncodingCode());
            } else {
                return new String(aySource, nOff, i - nOff);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new String(aySource, nOff, i - nOff);
        }

    }


    /**
     * get(0)-->String get(1)-->offset
     *
     * @param aySource
     * @param nOff
     * @param code
     * @return
     * @throws Exception
     */
    public static List toStringList(byte[] aySource, int nOff, String code)
            throws Exception {
        List list = new ArrayList();
        int i = nOff;
        while (i < aySource.length && aySource[i] != 0)
            i++;
        if (FreeApp.getInstance().getEncodingCode() != null
                && (!FreeApp.getInstance().getEncodingCode().equals(""))) {
            list.add(new String(aySource, nOff, i - nOff, FreeApp.getInstance().getEncodingCode()));
            list.add(i + 1);
            return list;
        } else {
            list.add(new String(aySource, nOff, i - nOff, code));
            list.add(i + 1);
            return list;
        }

    }
}
