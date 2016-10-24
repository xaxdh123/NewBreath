package com.webcon.nh.newbreath.alarmHandler;

import android.util.Log;

import com.webcon.nh.newbreath.utils.FreeApp;

import java.text.DecimalFormat;

/**
 * Created by WW on 16-4-19.
 * <p/>
 * 状态记录类：
 * <p/>
 * 判断当前的状态：  无人/有人浅睡眠/有人深睡眠
 * 并且记录各个状态的比例。
 * <p/>
 * 记录呼吸频率，报警次数。
 */
public class SystemStateManger {
    private static int currentSleepState = FreeApp.NO_HUMAN;

    public synchronized static void resetSleepState(int newState) {
        currentSleepState = newState;
    }

    public synchronized static int getSleepState() {
        return currentSleepState;
    }


    // 不同状态的记录
    private static int no_human_count = 1;
    private static int has_human_deepSleep_count = 1;
    private static int has_human_nonDeepSleep_count = 1;

    public static void upNohumanCount() {
        no_human_count++;
    }

    public static void upHashumanDeepCount() {
        has_human_deepSleep_count++;
    }

    public static void upHashumanShalowCount() {
        has_human_nonDeepSleep_count++;
    }


    public static String getPrintString() {
        DecimalFormat df1 = new DecimalFormat("0.00");

        float percent1 = ((float) no_human_count) / (no_human_count + has_human_deepSleep_count + has_human_nonDeepSleep_count);
        String str1 = df1.format(percent1);

        float percent2 = ((float) has_human_deepSleep_count) / (no_human_count + has_human_deepSleep_count + has_human_nonDeepSleep_count);
        String str2 = df1.format(percent2);

        float percent3 = ((float) has_human_nonDeepSleep_count) / (no_human_count + has_human_deepSleep_count + has_human_nonDeepSleep_count);
        String str3 = df1.format(percent3);

        String str4;
        switch (currentSleepState) {
            case FreeApp.NO_HUMAN:
                str4 = "没人";
                break;
            case FreeApp.HAS_HUMAN_DEEPSLEEP:
                str4 = "深睡-->此时发送报警";
                break;
            case FreeApp.HAS_HUMAN_NONDEEP_SLEEP:
                str4 = "浅睡-->此时报警忽略";
                break;
            default:
                str4 = "状态错误";
                break;
        }
        return "当前状态:" + str4 + "   没人比例:" + str1 + " 深睡比例:" + str2 + " 浅睡比例:" + str3
                + "\n无人/深睡/浅睡 的累计：" + no_human_count + "//" + has_human_deepSleep_count + "//" + has_human_nonDeepSleep_count
                + "\n 呼吸/噪声 比例：" + FreeApp.getInstance().getPercentageB() + "//" + FreeApp.getInstance().getPercentageN();
    }


    // 不同报警的记录
    private static int smallFluxCount = 0;
    private static int physicalPauseCount = 0;
    private static int nervousPauseCount = 0;
    private static int mixedPauseCount = 0;
    private static int otherCount = 0;
    // 呼吸次数的记录
    private static int breathCycleCount = 0;

    public static void upSmallFluxCount() {
        smallFluxCount++;
        Log.i("CCC", "smallFluxCount:" + smallFluxCount);
    }
    public static void upOtherFluxCount() {
        otherCount++;
        Log.i("CCC", "otherCount:" + smallFluxCount);
    }
    public static void upPhysicalPauseCount() {
        physicalPauseCount++;
        Log.i("CCC", "physicalPauseCount:" + physicalPauseCount);
    }

    public static void upNervousPauseCount() {
        nervousPauseCount++;
        Log.i("CCC", "nervousPauseCount:" + nervousPauseCount);
    }

    public static void upMixedPauseCount() {
        mixedPauseCount++;
        Log.i("CCC", "mixedPauseCount:" + mixedPauseCount);
    }

    public static void upBreathCycleCount() {
        breathCycleCount++;
        Log.i("CCC", "breathCycleCount:" + breathCycleCount);
    }

    public static void resetCount() {
        smallFluxCount = 0;
        physicalPauseCount = 0;
        nervousPauseCount = 0;
        mixedPauseCount = 0;
    }

    public static int getPauseCount() {
        return physicalPauseCount + nervousPauseCount + mixedPauseCount;
    }

    public static int getFluxCount() {
        return smallFluxCount;
    }

    public static int getBreathFreq() {  // 获取呼吸频率 次/分钟*10  比如 164表示 ：16.4次呼吸/每分钟
        return 10 * 60 * 5 * breathCycleCount / has_human_deepSleep_count;
    }

    public static int getAHIIndex() {  // 获取AHI指数*100  比如465表示:AHI指数4.65， AHI指数表示呼吸异常次数/每小时
        return 100 * 3600 * 5 * (smallFluxCount + physicalPauseCount) / (no_human_count + has_human_deepSleep_count + has_human_nonDeepSleep_count);
    }


}
