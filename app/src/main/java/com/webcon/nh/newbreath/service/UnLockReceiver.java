package com.webcon.nh.newbreath.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webcon.nh.newbreath.ui.MainActivity;


/**
 * UnLockReceiver 接受开机广播，开机自启动，并启动服务
 * Created by Administrator on 2016/7/21.
 */
public class UnLockReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equalsIgnoreCase("U_WILL_NOT_KILL_ME")) {
            mContext = context;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Intent i1 = new Intent(Intent.ACTION_RUN);
                    i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i1.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);  // include stopped packages..
                    i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i1.setClass(mContext, MainActivity.class);
                    mContext.startActivity(i1);
                }

            }).start();
        }

    }
}
