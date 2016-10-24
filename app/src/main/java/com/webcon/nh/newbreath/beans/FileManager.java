package com.webcon.nh.newbreath.beans;

import android.os.Environment;

import com.webcon.nh.newbreath.socket.JTools;
import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileManager
 * Created by NH on 2016/8/30.
 */

public class FileManager {
    private File waveFile;
    private File stateFile;
    private File alarmFile;

    public File getPath() {
        return path;
    }

    private File path;

    public FileManager() {
        creatFiles();
    }

    private void creatFiles() {
        SimpleDateFormat yMdH = new SimpleDateFormat("yyyy_MM_dd_HH");
        SimpleDateFormat yMd = new SimpleDateFormat("yyyy_MM_dd");
        Date now = new Date();
        String exDir = yMdH.format(now) + "_00_" + FreeApp.DEVICE_NUM;
        String alarmFileName = yMd.format(now) + FreeApp.RECORD_FILE_ALARM;
        String waveFileName = exDir + FreeApp.RECORD_FILE_BREATH;
        String stateFileName = exDir + FreeApp.RECORD_FILE_STATES;
        String fileDir = (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                ? (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FreeApp.getInstance().DEFAULT_SERVERID + File.separator)
                : (Environment.getDataDirectory().getAbsolutePath() + File.separator + FreeApp.getInstance().DEFAULT_SERVERID + File.separator);
        path = new File(fileDir);
        if (!path.exists()) {
            File pathParent = path.getParentFile();
            if (!pathParent.exists()) {
                pathParent.mkdirs();
            }
            path.mkdirs();
        }
        waveFile = new File(fileDir + waveFileName);
        if (!waveFile.exists()) {
            try {
                waveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stateFile = new File(fileDir + stateFileName);
        if (!stateFile.exists()) {
            try {
                stateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alarmFile = new File(fileDir + alarmFileName);
        if (!alarmFile.exists()) {
            try {
                alarmFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAlarmFile(int ymdhms, short timer, int[] currentBreathSignal, short alarmType) {

        try {
            FileOutputStream fos = new FileOutputStream(alarmFile, true);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
            byte b[] = new byte[2000];
            JTools.IntToBytes4(ymdhms, b, 0);
            JTools.ShortToBytes2(timer, b, 4);
            JTools.ShortToBytes2(alarmType, b, 6);
            for (byte a : b)
                dos.writeByte(a);
            for (int c : currentBreathSignal)
                dos.writeInt(c);
            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void setWaveFile(int[] wave) {
        try {
            FileOutputStream fos = new FileOutputStream(waveFile, true);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
            byte b[] = new byte[4];
            JTools.IntToBytes4(wave[0], b, 0);
            for (byte a : b)
                dos.writeByte(a);
            dos.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStateFile(int ymdhms, int respiratoryRate, int smallFluxCount, int overTimeCount, int stats1, int stats2, int stats3, int stats4, int stats5) {

        try {
            FileOutputStream fos = new FileOutputStream(stateFile, true);
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
            byte b[] = new byte[36];
            JTools.IntToBytes4(ymdhms, b, 0);
            JTools.IntToBytes4(respiratoryRate, b, 4);
            JTools.IntToBytes4(smallFluxCount, b, 8);
            JTools.IntToBytes4(overTimeCount, b, 12);
            JTools.IntToBytes4(stats1, b, 16);
            JTools.IntToBytes4(stats2, b, 20);
            JTools.IntToBytes4(stats3, b, 24);
            JTools.IntToBytes4(stats4, b, 28);
            JTools.IntToBytes4(stats5, b, 32);
            for (byte a : b)
                dos.writeByte(a);
            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
