package com.webcon.nh.newbreath.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.webcon.nh.newbreath.R;
import com.webcon.nh.newbreath.socket.JTools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * InfoActivity
 * Created by NH on 2016/9/21.
 */

public class InfoActivity extends AppCompatActivity {
    private TextView tvInfo;
    private byte[] b = new byte[2000];
    private UIHandler uiHandler;
    private String str = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        tvInfo = (TextView) findViewById(R.id.text_info);
        handleInfo();
        uiHandler = new UIHandler(this);

    }

    private void handleInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File alarm = new File("sdcard/NH120212303/2016_09_21.nhAlarm");
                try {
                    FileInputStream fis = new FileInputStream(alarm);
                    BufferedInputStream in = new BufferedInputStream(fis);
                    while (in.available() > 0) {
                        if (in.read(b) != -1)
                            str = str + JTools.Bytes4ToInt(b, 0) + "\t";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uiHandler.sendEmptyMessage(1);
            }
        }).start();


    }

    // 更新UI handler 弱引用
    public class UIHandler extends Handler {

        WeakReference<Activity> mActivityReference;

        UIHandler(Activity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            final Activity mActivity = mActivityReference.get();
            super.handleMessage(msg);
            if (mActivity != null) {
                switch (msg.what) {
                    case 1:
                        tvInfo.setText(str);
                        break;
                }
            }
        }
    }
}
