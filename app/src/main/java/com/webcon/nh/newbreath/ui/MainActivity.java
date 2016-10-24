package com.webcon.nh.newbreath.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.webcon.nh.newbreath.DataAnalyse.ArrayAnalyser;
import com.webcon.nh.newbreath.R;
import com.webcon.nh.newbreath.alarmHandler.SystemStateManger;
import com.webcon.nh.newbreath.beans.DataArray;
import com.webcon.nh.newbreath.socket.NetApi;
import com.webcon.nh.newbreath.utils.FreeApp;
import com.webcon.nh.newbreath.utils.LineChartUtil;

import java.lang.ref.WeakReference;

/**
 * project: BreathSerial
 * author: NH Xu Dh
 * data: 2016.6.26
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String ACTION_WORK = "com.webcon.newbreath.WORK";
    private static final String ACTION_UPUI = "com.webcon.newbreath.UPUI";

    private ViewStub stub;
    private Button btnStart;
    private LineChart mLineChart;
    private LineData mLineData;
    private LineChartUtil mLineChartUtil;
    private TextView txt1;
    private BroadcastReceiver uireceiver;
    private DataArray dataArray;

    private int[] inputDatas;
    private int[] inputIndex;
    private int defalutFluxS;
    private int defalutFluxL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getDeviceID();
        register();
        startWorkService();
        initView();
        getSharedPreferences(FreeApp.SHAREPREFERENCE_DEFAULT_FLUX, MODE_PRIVATE);

    }

    private void register() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetApi.getInstance().register(FreeApp.getInstance().DEFAULT_SERVERID, FreeApp.REISTER_STATES);
                NetApi.getInstance().closeALL();
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uireceiver != null)
            this.unregisterReceiver(uireceiver);
    }


    private void startWorkService() {
        //开启Service 启动串口
        Intent intent = new Intent();
        intent.setAction(ACTION_WORK);
        intent.setPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        if (FreeApp.getInstance().isDebug()) {
            Log.i(TAG, "WorkService Started");
        }
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_show);
        btnStart.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show:
                pressed();
                break;
            case R.id.btn_next:
                startActivity(new Intent(MainActivity.this, FFTActivity.class));
                break;
            default:
                break;
        }
    }

    private void pressed() {
        initDraw();
        initReceive();
    }


    private void initDraw() {
        btnStart.setVisibility(View.GONE);

        //初始化布局
        stub = (ViewStub) findViewById(R.id.content);
        stub.setLayoutResource(R.layout.mpchart);
        View v = stub.inflate();
        mLineChart = (LineChart) v.findViewById(R.id.chart);
        mLineChartUtil = new LineChartUtil();
        inputDatas = new int[300];
        inputIndex = new int[]{};
        mLineData = mLineChartUtil.initDataFromArray(inputDatas, inputIndex);
        mLineChartUtil.initChart(mLineChart);   //初始化图表
        mLineChartUtil.plotLine1(mLineChart, mLineData);//初始化折线

        txt1 = (TextView) v.findViewById(R.id.txt_state);
        v.findViewById(R.id.btn_next).setOnClickListener(this);

        if (FreeApp.getInstance().isDebug()) {
            Log.i(TAG, "lineChart plotLine and view draw success");
        }

    }

    private void initReceive() {
        uireceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UIHandler uiHandler = new UIHandler(MainActivity.this);
                dataArray = intent.getExtras().getParcelable("DATA_ARRAY");
                inputDatas = new int[300];
                inputIndex = new int[]{};
                if (dataArray != null) {
                    inputDatas = dataArray.getMyArray();
                    inputIndex = dataArray.getIndicators();
                    defalutFluxS = dataArray.getDefaultFluxS();
                    defalutFluxL = dataArray.getDefaultFluxL();
                    uiHandler.sendEmptyMessage(1);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPUI);
        this.registerReceiver(uireceiver, filter);

    }

    public void getDeviceID() {
        String m_szDevIDShort = "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
//        if (FreeApp.getInstance().isDebug())
        Log.d(TAG, "IMEI+IMSI:" + m_szDevIDShort);
        FreeApp.getInstance().setDEFAULT_SERVERID(m_szDevIDShort);
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
            if (FreeApp.getInstance().isDebug())
                Log.d(TAG, "handleMessage......");
            super.handleMessage(msg);
            if (mActivity != null) {
                switch (msg.what) {
                    case 1:
                        mLineData = mLineChartUtil.initDataFromArray(inputDatas, inputIndex);
                        if (ArrayAnalyser.findMin_inIntArray(inputDatas) < 30000)
                            mLineChartUtil.resetChart(mLineChart);
                        mLineChartUtil.plotLine1(mLineChart, mLineData);
                        txt1.setText(SystemStateManger.getPrintString()
                                + "\n标准大通量:" + defalutFluxL + "············标准小通量：" + defalutFluxS);
                        break;
                    case 0:
                        break;
                }
            }
        }
    }
}
