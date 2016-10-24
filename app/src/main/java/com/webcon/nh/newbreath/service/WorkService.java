package com.webcon.nh.newbreath.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.webcon.nh.newbreath.DataAnalyse.ArrayAnalyser;
import com.webcon.nh.newbreath.R;
import com.webcon.nh.newbreath.beans.DataSignal;
import com.webcon.nh.newbreath.socket.NetApi;
import com.webcon.nh.newbreath.utils.FreeApp;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import com.webcon.nh.newbreath.DataAnalyse.SerialMalformatException;

import android_serialport_api.SerialPort;


/**
 * WorkService
 * Created by NH on 2016/8/24.
 */

public class WorkService extends Service {
    private static final String TAG = "WorkService";
    private static final String ACTION_HANDLE = "com.webcon.newbreath.HANDLE";
    private static final String ACTION_SEND = "com.webcon.newbreath.SEND";
    private Intent intent;
    private PowerManager.WakeLock wakeLock = null;
    private static final int FORE_NOTI_ID = 768;

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        }
        if (null != wakeLock) {
            wakeLock.acquire();
        }
        initDataSignal(DataSignal.serialSource);//  信号来源：asssetFileSource /  serialSource
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FORE_NOTI_ID, createNotice());
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //-----------------启动数据源------------
    private void initDataSignal(DataSignal signal) {
        intent = new Intent();
        intent.setAction(ACTION_HANDLE);
        intent.setPackage(getPackageName());
        switch (signal) {
            case asssetFileSource:
                new Thread(recordSignalInput).start(); //从asset file 中的文件读取数据
                if (FreeApp.getInstance().isDebug())
                    Log.i(TAG, "read data from asset file success");
                break;
            case serialSource:
                new Thread(serialSignalInput).start();  // 从串口读取数据
                if (FreeApp.getInstance().isDebug())
                    Log.i(TAG, "read data from serial port success");
                break;
            default:
                break;
        }
    }

    //-----读取文件中数据的线程------
    //--传感器波形文件录制下后，可以放到assets文件夹下面，可以读取这里的数据来模拟串口的传感器数据
    //这样就可以在手机上进行测试。
    private Runnable recordSignalInput = new Runnable() {
        @Override
        public void run() {
            // read from asset folder
            AssetManager am = getAssets();
            InputStream is;
            DataInputStream dis;
            int count = 1;
            int temp;
            try {
                is = am.open("data_rec/2016_10_17_02_00_A1.nhBreath");
                dis = new DataInputStream(is);
                dis.skipBytes(4 * 5 * 60 * 40);         //跳过个小时的数据X
                while (count > 0) {
                    Thread.sleep(40);//模拟一个数据用时0.2s 的状态，并防止线程阻塞
                    temp = dis.readInt();
                    count = dis.available();
                    intent.putExtra("DATA", temp);    // 输入0.2秒波形数据（一个int）到 HandlerService
                    startService(intent);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    };


    private Runnable serialSignalInput = new Runnable() {

        @Override
        public void run() {
            File file = new File("/dev/ttyS2"); // 新的核心板上串口
            InputStream mInputStream;
            SerialPort port1;
            byte[] buffer = new byte[710];  // 读取710个字节的数据 ~对应越0.2秒数据
            int size;
            int aa = 0;  // convert to integers and find the average
            try {
                port1 = new SerialPort(file, 115200, 0); // 设置串口波特率
                mInputStream = port1.getInputStream();
                if (FreeApp.getInstance().isDebug())
                    Log.i(TAG, "port successful");
                while (mInputStream != null) {
                    Thread.sleep(200);
                    if (mInputStream.available() > 710) {
                        size = mInputStream.read(buffer);
                        if (size > 0)
                            try {
                                if (ArrayAnalyser.decodeSerial(buffer) != 0)
                                    aa = ArrayAnalyser.decodeSerial(buffer);
                                else aa = aa + 1;

                            } catch (SerialMalformatException e) {
                                aa = aa + 1;
                                e.printStackTrace();
                            }
                        intent.putExtra("DATA", aa);    // 输入0.2秒波形数据（一个int）到 HandlerService
                        startService(intent);
                    }


                    if (timeup(16, 17, 50)) {
                        Intent i = new Intent();
                        i.setAction(ACTION_SEND);
                        int now = (int) (System.currentTimeMillis() / 1000);
                        i.putExtra("Time", now);
                        i.setPackage(getPackageName());
                        startService(i);
                        NetApi.getInstance().closeALL();
                    }
                    if (FreeApp.getInstance().isDebug())
                        Log.i(TAG, "available:" + mInputStream.available());
                }

            } catch (SecurityException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }


    };

    private boolean timeup(int h, int m, int s) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int min = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);
        return hour == h && min == m && sec == s;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null) {
            wakeLock.acquire();
            wakeLock.release();
            wakeLock = null;
        }
        stopForeground(true);
        Intent i_destroy = new Intent();
        i_destroy.setAction("U_WILL_NOT_KILL_ME");
        sendBroadcast(i_destroy);

    }
    //-----------Notification Test------------------

    private Notification createNotice() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("NewBreath")
                        .setContentText("WorkService Starts");
        return mBuilder.build();
    }
}
