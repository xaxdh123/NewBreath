package com.webcon.nh.newbreath.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.webcon.nh.newbreath.DataAnalyse.ArrayAnalyser;
import com.webcon.nh.newbreath.R;
import com.webcon.nh.newbreath.beans.DataArray;
import com.webcon.nh.newbreath.utils.FreeApp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * FFTActivity
 * Created by NH on 2016/10/9.
 */

public class FFTActivity extends AppCompatActivity {
    private static final String TAG = "FFTActivity";
    private static final String ACTION_UPUI = "com.webcon.newbreath.UPUI";
    private BroadcastReceiver uireceiver;
    private DataArray dataArray;
    private int[] inputDatas;
    private LineChart mLineChart;
    private LineData mLineData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fftchart);
        mLineChart = (LineChart) findViewById(R.id.fftchart);
        mLineChart.setDescription("频率：(Hz)");
        mLineChart.setDescriptionTextSize(30);


        inputDatas = new int[300];
        uireceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UIHandler uiHandler = new UIHandler(FFTActivity.this);
                dataArray = intent.getExtras().getParcelable("DATA_ARRAY");
                if (dataArray != null) {
                    inputDatas = dataArray.getMyArray();
                    mLineData = getData(inputDatas);
                    uiHandler.sendEmptyMessage(1);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPUI);
        this.registerReceiver(uireceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uireceiver != null)
            this.unregisterReceiver(uireceiver);
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
            ArrayAnalyser.fft(inputDatas);
            if (mActivity != null) {
                switch (msg.what) {
                    case 1:
                        drawChart(mLineData, mLineChart);
                        break;
                    case 0:
                        break;
                }
            }
        }
    }

    private LineData getData(int[] inputDatas) {
        double[] datas = ArrayAnalyser.fft(inputDatas);

        ArrayList<String> xValues = new ArrayList<>();
        for (int i = 1; i < 128; i++) {
            // x轴显示的数据，这里默认使用数字下标显示

            xValues.add(String.format("%.2f", ((float) i * 5 / 256)));
        }
        // y轴的数据
        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 1; i < 128; i++) {
            float value = (float) datas[i];
            yValues.add(new Entry(value, i));
        }
        // create a dataset and give it a type
        // y轴的数据集合
        LineDataSet lineDataSet = new LineDataSet(yValues, "FFT频谱折线图" /*显示在比例图上*/);
        lineDataSet.setColor(Color.RED);

        //用y轴的集合来设置参数
        lineDataSet.setLineWidth(3f); // 线宽
        lineDataSet.setCircleSize(3f);// 显示的圆形大小

        ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
        lineDataSets.add(lineDataSet); // add the datasets

        // create a data object with the datasets
        return new LineData(xValues, lineDataSets);
    }

    private void drawChart(LineData mLineData, LineChart mLineChart) {
        mLineChart.setDescriptionColor(Color.BLUE);             //描述文字颜色
        mLineChart.setDescriptionTextSize(14f);                 //描述文字大小，float类型[6,16]
        mLineChart.setDrawGridBackground(true);                //设置图表内格子背景是否显示，默认是false
        mLineChart.setBackgroundColor(Color.LTGRAY);            //设置图表背景
        mLineChart.setDrawBorders(true);                        //设置图表内格子外的边框是否显示
        mLineChart.setDrawGridBackground(false);                 //设置是否显示表格

        XAxis xAxis = mLineChart.getXAxis();
        YAxis yAxis = mLineChart.getAxisLeft();
        mLineChart.getAxisRight().setEnabled(false);

        // set up X-axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);      //坐标轴设置为底部
        xAxis.setTextSize(12f);                             //字体大小
        xAxis.setTextColor(Color.RED);                      //字体颜色
        // set up Y-axis
        yAxis.setStartAtZero(false);                        //设置图表起点从0开始
        yAxis.setSpaceTop(10f);                             //Y轴坐标距顶有多少距离，即留白

        mLineChart.setData(mLineData);

        Legend l = mLineChart.getLegend();  //set legend position
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        mLineChart.invalidate(); // refreshn
    }
}
