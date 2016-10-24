package com.webcon.nh.newbreath.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.LoginFilter;
import android.util.Log;

import com.webcon.nh.newbreath.beans.FileManager;
import com.webcon.nh.newbreath.socket.NetApi;
import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * SendService
 * Created by NH on 2016/9/10.
 */

public class SendService extends IntentService {
    private int now;
    private int result = 0;

    public SendService(String name) {
        super(name);
    }

    public SendService() {
        super("SendService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        now = intent.getIntExtra("Time", 0) - 24 * 60 * 60;
        sendFiles();
        receiverResult();
    }

    private void receiverResult() {
        int a = NetApi.getInstance().recvResult();
        Log.e("result:", String.valueOf(a));

    }

    public void sendFiles() {
        if (FreeApp.getInstance().isDebug())
            Log.d("SendSerivce", "sendFile Start");
        FileManager fileManager = new FileManager();
        File path = fileManager.getPath();
        File[] files = path.listFiles();
        LinkedList<File> list = new LinkedList<>();

        for (File f : files) {
            if ((int) (f.lastModified() / 1000) > now) {
                list.add(f);
            }
        }
        for (int a = 0; a < list.size(); a++) {
            File f = list.get(a);
            if (f.getName().contains(FreeApp.RECORD_FILE_BREATH))
                NetApi.getInstance().sendWaveFile(f);
            else
                NetApi.getInstance().sendStateFile(f);
        }
    }
}
