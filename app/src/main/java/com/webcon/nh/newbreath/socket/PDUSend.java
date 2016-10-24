package com.webcon.nh.newbreath.socket;

import android.util.Log;

import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PDUSend {
    private Socket socket;
    private JPDUPackage jpackage = null;
    private String TAG = "PDUSend";
    private DataOutputStream out;

    public PDUSend() {
        try {
            this.jpackage = new JPDUPackage();
            socket = new Socket();
            socket.setSoTimeout(5000);//读取数据超时设置5s
            socket.connect(new InetSocketAddress(FreeApp.ADDRESS_IP, FreeApp.ADDRESS_PORT_SEND), 20000);//建立连接超时设置
        } catch (Exception e) {
            try {
                Thread.sleep(60000);
                socket = new Socket();
                socket.setSoTimeout(5000);//读取数据超时设置5s
                socket.connect(new InetSocketAddress(FreeApp.ADDRESS_IP, FreeApp.ADDRESS_PORT_SEND), 20000);//建立连接超时设置
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Log.w(TAG, "PDUSend fail,try again", e);
        }
    }

    public PDUSend(int nPort) {

        try {
            this.jpackage = new JPDUPackage();
            socket = new Socket();
            socket.setSoTimeout(5000);//读取数据超时设置5s
            socket.connect(new InetSocketAddress(FreeApp.ADDRESS_IP, nPort), 20000);//建立连接超时设置
        } catch (Exception e) {
            try {
                Thread.sleep(60000);
                socket = new Socket();
                socket.setSoTimeout(5000);//读取数据超时设置5s
                socket.connect(new InetSocketAddress(FreeApp.ADDRESS_IP, nPort), 20000);//建立连接超时设置
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Log.w(TAG, "PDUSend fail,try again", e);
        }
    }


    public void send() {
        try {
            if (socket != null) {
                out = new DataOutputStream(socket.getOutputStream());
                if (this.jpackage.write(out) != -1) {
                    out.flush();
                }
            }
            this.jpackage.setIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int recv() throws Exception {
        try {
            if (socket == null)
                throw new Exception(TAG + "socket is null");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            if (this.jpackage.read(in) == -1) {
                return -1;
            }
        } catch (Exception e) {
            socket = null;
            throw new Exception(TAG + "-1");
        }
        return 0;
    }


    public void close() {
        try {
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            Log.w(TAG, "socket error:", e);
        }
    }

    public void pushData(Object obj) {
        this.jpackage.pushData(obj);
    }

    public int getLength() {
        return jpackage.getLength();
    }

    public byte getByte() {
        return this.jpackage.getByte();
    }

    public short getShort() {
        return this.jpackage.getShort();
    }

    public int getInt() {
        return this.jpackage.getInt();
    }

    public String getString() {
        return this.jpackage.getString();
    }

    public void setPduType(short a) {
        this.jpackage.setPduType(a);
    }

    public short getPduType() {
        return this.jpackage.getPduType();
    }

    public void setSubPduType(short a) {
        this.jpackage.setSubPduType(a);
    }

    public short getSubPduType() {
        return this.jpackage.getSubPduType();
    }

    public void readSkip(int a) {
        this.jpackage.readSkip(a);
    }

}
