package com.webcon.nh.newbreath.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * LineChartUtil
 * Created by Administrator on 16-1-14.
 */
public class LineChartUtil {


    // constructors
    public LineChartUtil() {
    }

    public void resetChart(LineChart mChart) {
        mChart.getAxisLeft().resetAxisMaxValue();
        mChart.getAxisLeft().resetAxisMinValue();

    }

    public void initChart(LineChart mChart) {


        mChart.setDescription("实时数据--最近一分钟波形图");   //图表默认右下方的描述
        mChart.setDescriptionColor(Color.BLUE);             //描述文字颜色
        mChart.setDescriptionPosition(400f, 25f);           //描述文字位置，参数是float(像素)，从图表左上角开始计算
        mChart.setDescriptionTextSize(14f);                 //描述文字大小，float类型[6,16]
        mChart.setDrawGridBackground(true);                //设置图表内格子背景是否显示，默认是false
        mChart.setBackgroundColor(Color.LTGRAY);            //设置图表背景
        mChart.setDrawBorders(true);                        //设置图表内格子外的边框是否显示
        mChart.setDrawGridBackground(false);                 //设置是否显示表格


        XAxis xAxis = mChart.getXAxis();
        YAxis yAxis = mChart.getAxisLeft();
        mChart.getAxisRight().setEnabled(false);

        // set up X-axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);      //坐标轴设置为底部
        xAxis.setTextSize(12f);                             //字体大小
        xAxis.setTextColor(Color.RED);                      //字体颜色
        xAxis.addLimitLine(new LimitLine(40000));
        xAxis.setLabelsToSkip(49);                          //提示线间隔
        // set up Y-axis
        yAxis.setStartAtZero(false);                        //设置图表起点从0开始
        yAxis.setAxisMinValue(30000);
        yAxis.setAxisMaxValue(50000);
        yAxis.setSpaceTop(10f);                             //Y轴坐标距顶有多少距离，即留白
    }


    /**
     * create data set form input data
     *
     * @param ints         data to draw line
     * @param localMinPosi data of position indicators
     * @return LineData
     */
    public LineData initDataFromArray(int[] ints, int[] localMinPosi) {

        int total_Length = ints.length;
        LineData mLdata;

        // arraylist of y-x entry
        ArrayList<Entry> valsComp1 = new ArrayList<>();

        // arraylist of y-x entry--> to show Local minimum indicators--> for debugging
        ArrayList<Entry> valsComp2 = new ArrayList<>();

        // arraylist of x-label
        ArrayList<String> xVals = new ArrayList<>();
        Entry c1e1;
        Entry c1e2;
        for (int i = 0; i < total_Length; i++) {
            c1e1 = new Entry((float) ints[i], i);     //  y(float)-x(int) pairs

            valsComp1.add(c1e1);                            //  add entry ( y(float)-x(int) pair )
            xVals.add(i / 5f + "");                         //string as labels
        }

        // set 2nd line to show Local Minimum indicators
        int local_min_numbers = localMinPosi.length;
        if (local_min_numbers > 0) {
            for (int a : localMinPosi) {
                c1e2 = new Entry((float) ints[a], a);
                valsComp2.add(c1e2);
            }
        }

        // create line-data-set
        LineDataSet setComp1 = new LineDataSet(valsComp1, "signal 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.YELLOW);
        setComp1.setDrawCircles(false);// no circles

        LineDataSet setComp2 = new LineDataSet(valsComp2, "minimums");// show local minimums
        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp2.setDrawCircles(true);// circles
        setComp2.setCircleColor(Color.RED);
        setComp2.setLineWidth(0);
        setComp2.setColor(Color.LTGRAY);  // set the line color to match the background so that only the indicators are shown

        // a plot can have multiple lines, by using line-data-set array
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setComp1);
        dataSets.add(setComp2);

        mLdata = new LineData(xVals, dataSets);

        return mLdata;
    }

    public void plotLine1(LineChart mChart, LineData mLdata) {
        mChart.setData(mLdata);

        Legend l = mChart.getLegend();  //set legend position
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
        mChart.invalidate(); // refresh
    }

}
