package com.webcon.nh.newbreath.socket;

import android.util.Log;

import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * NetApi for socket
 * Created by NH on 2016/8/30.
 */
public class NetApi {
    private PDUSend pduSend;
    private String serverID;
    private DataInputStream is = null;
    private static final int BUFFERSIZR = 4 * 300 * 60;
    private static NetApi instance;

    public static synchronized NetApi getInstance() {
        if (instance == null) {
            instance = new NetApi();
        }
        return instance;
    }

    public int register(String serverID, short optFlag) {
        this.serverID = serverID;
        pduSend = new PDUSend(FreeApp.ADDRESS_PORT_REGIST);
        pduSend.setPduType(FreeApp.PDU_NHSystemServerReport_REQ_FromNHSystem);
        pduSend.pushData(serverID);
        pduSend.pushData(optFlag);
        pduSend.send();
        return 0;


    }


    public int sendWaveFile(File file) {
        try {
            pduSend = new PDUSend();
            byte buffer[] = new byte[BUFFERSIZR];
            is = new DataInputStream(new FileInputStream(file));
            while (true) {
                pduSend.setPduType(FreeApp.PDU_NHSystemServerSave_REQ_FromNHSystem);
                pduSend.pushData(serverID);
                pduSend.pushData(file.getName());
                int len = is.read(buffer, 0, BUFFERSIZR);
                short endFlag = (short) (len == BUFFERSIZR ? 0 : 1);
                pduSend.pushData(endFlag);
                pduSend.pushData(len);
                for (int i = 0; i < len; i++) {
                    pduSend.pushData(buffer[i]);
                }
                if (FreeApp.getInstance().isDebug())
                    Log.e("KKKKK", pduSend.getLength() + "");
                pduSend.send();
                if (endFlag == 1)
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int sendStateFile(File file) {
        try {
            pduSend = new PDUSend();
            byte buffer[] = new byte[BUFFERSIZR];
            is = new DataInputStream(new FileInputStream(file));
            while (true) {
                pduSend.setPduType(FreeApp.PDU_NHSystemServerSave_REQ_FromNHSystem);
                pduSend.pushData(serverID);
                pduSend.pushData(file.getName());
                int len = is.read(buffer, 0, BUFFERSIZR);
                short endFlag = (short) (len == BUFFERSIZR ? 0 : 1);
                pduSend.pushData(endFlag);
                pduSend.pushData(len);
                for (int i = 0; i < len; i++) {
                    pduSend.pushData(buffer[i]);
                }
                if (FreeApp.getInstance().isDebug())
                    Log.e("KKKKK", pduSend.getLength() + "");
                pduSend.send();
                if (endFlag == 1)
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }


    public int sendAlarmParcel(int ymdhms, short timer, int[] waveForm, short alarmType) {
        pduSend = new PDUSend(FreeApp.ADDRESS_PORT_REGIST);
        pduSend.setPduType(FreeApp.PDU_NHSystemDBROUTER0_REQ_FromNHSystem);
        pduSend.pushData(FreeApp.DBSUB_AddAlarmInfo_Req_FromNHSystem);
        pduSend.pushData(serverID);
        pduSend.pushData(alarmType);
        pduSend.pushData(ymdhms);
        pduSend.pushData(timer);
        pduSend.pushData((short) (waveForm.length * 4));
        for (int aa = 0; aa < 300; aa++) {
            pduSend.pushData(waveForm[aa]);
        }
        pduSend.send();
        return 0;

    }

    public int recvResult() {
        pduSend = new PDUSend();
        try {
            pduSend.recv();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pduSend.getPduType() == FreeApp.PDU_NHSystemServerReport_RSP_ToNHSystem) {
            return pduSend.getShort();
        } else if (pduSend.getPduType() == FreeApp.PDU_NHSystemServerSave_RSP_ToNHSystem) {
            return pduSend.getShort();
        } else if (pduSend.getPduType() == FreeApp.PDU_NHSystemDBROUTER0_RSP_ToNHSystem) {
            if (pduSend.getShort() == FreeApp.DBSUB_AddAlarmInfo_Rsp_ToNHSystem)
                return pduSend.getShort();
            else
                return 0;
        } else
            return 0;
    }


    public void closeALL() {
        try {
            if (is != null)
                is.close();
            if (pduSend != null)
                pduSend.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
