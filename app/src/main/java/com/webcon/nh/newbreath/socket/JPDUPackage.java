package com.webcon.nh.newbreath.socket;

import java.io.InputStream;
import java.io.OutputStream;

public class JPDUPackage {
    private int PDUBODYLENGTH;
    private JPDUHead m_head;
    private JPDUBody m_body;

    public JPDUPackage() {
        m_head = new JPDUHead();
        m_body = new JPDUBody();
        PDUBODYLENGTH = m_body.getPDUBODYLENGTH();
    }


    public short getPduType() {
        return m_head.getM_nPduType();
    }

    public void setPduType(short a) {
        m_head.setM_nPduType(a);
    }

    public short getSubPduType() {
        return m_head.getM_subPduType();
    }

    public void setSubPduType(short a) {
        m_head.setM_subPduType(a);
    }

    public int read(InputStream in) {
        if (m_head.read(in) == -1)
            return -1;
        if (m_head.getM_nBodyLen() > PDUBODYLENGTH) {
            m_body = null;
            m_body = new JPDUBody(m_head.getM_nBodyLen());
        }
        m_body.setPDUBODYLENGTH(m_head.getM_nBodyLen());
        return m_body.read(in);
    }

    public int write(OutputStream out) {
        m_head.setM_nBodyLen(m_body.getWriteindex());

        int a = m_head.write(out);
        int b = m_body.write(out);
        if (a != 0 && b != 0)
            return 1;
        else
            return 0;
    }

    public void pushData(Object object) {
        m_body.pushData(object);
    }

   public int getLength() {
        return m_body.getWriteindex();
    }

    public byte getByte() {
        return m_body.getByte();
    }

    public short getShort() {
        return m_body.getShort();
    }

    public short getshResult() {
        return m_body.GetshResult();
    }

    public int getInt() {
        return m_body.getInt();
    }

    public String getString() {
        return m_body.getString();
    }

    public void readSkip(int a) {
        m_body.readSkip(a);
    }

    public void setIndex() {
        m_body.setIndex();
    }
}
