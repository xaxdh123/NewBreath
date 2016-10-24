package com.webcon.nh.newbreath.DataAnalyse;

/**
 * Created by WW on 16-1-15.
 *
 * this class specifies the critical condition to separate an individual breath signal cycle
 * by the following 3 parameters
 * wall height: A.U.
 * wall width(time): 1--> 0.2sec
 * minimum Interval(time):1-->0.2sec
 */
public class BreathCycleCondition {

    public int wallHeight;     // 墙高
    public int wallWidth;      // 墙宽
    public int minimumInterval;// 最小间隔

    public BreathCycleCondition(int hight, int width, int minimumInterval){
        this.wallHeight=hight;
        this.wallWidth=width;
        this.minimumInterval=minimumInterval;
    }

}
