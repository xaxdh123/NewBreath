package com.webcon.nh.newbreath.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import com.webcon.nh.newbreath.alarmHandler.SystemStateManger;
import com.webcon.nh.newbreath.DataAnalyse.ArrayAnalyser;
import com.webcon.nh.newbreath.DataAnalyse.BreathCycleCondition;
import com.webcon.nh.newbreath.RxJava.DataTransmitter;
import com.webcon.nh.newbreath.beans.DataArray;
import com.webcon.nh.newbreath.beans.FileManager;
import com.webcon.nh.newbreath.socket.NetApi;
import com.webcon.nh.newbreath.utils.FreeApp;

import java.util.Arrays;

import rx.Observer;


/**
 * HandlerService
 * Created by NH on 2016/8/26.
 */

public class HandlerService extends IntentService {
    private DataArray dataArray;
    private static final String TAG = "HandlerService";
    private static final String ACTION_UPDATEUI = "com.webcon.newbreath.UPUI";
    private DataTransmitter transmitter;
    private FileManager fileManager;
    private int[] currentBreathSignal;
    private int[] localMins_refined;
    private int defalutFluxS;
    private int defalutFluxL;
    private Intent intent = new Intent();
    private Bundle bundle = new Bundle();
    private static int temp1, temp2, temp3, temp4;
    private static int count = 0;
    private static int timer = 0;
    private static int bigcount = 0;

    public HandlerService() {
        super(TAG);
    }

    public HandlerService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fileManager = new FileManager();
        transmitter = new DataTransmitter();
        dataArray = DataArray.getInstance();
        transmitter.setDataObserver(breathDataObserver);
        dataArray.setPreferences(getSharedPreferences(FreeApp.SHAREPREFERENCE_DEFAULT_FLUX, MODE_PRIVATE));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int[] temp = new int[1];
        temp[0] = intent.getIntExtra("DATA", 0);
        transmitter.streamIn(temp);
        fileManager.setWaveFile(temp);
        if (FreeApp.getInstance().isDebug())
            Log.i(TAG, "printf num:" + temp[0]);
    }


    private Observer<int[]> breathDataObserver = new Observer<int[]>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        // all the calls here are on worker thread as specified in DataTransmitter
        @Override
        public void onNext(int[] ints) {
            //update breath real time wave form 实时数据传入
            dataArray.pushData(ints);

            // call analyser to check if there is any type of alarm 实时数据处理
            defalutFluxS = dataArray.getDefaultFluxS();
            defalutFluxL = dataArray.getDefaultFluxL();

            // get breath signal 每0.2s 获取一组300个数据
            currentBreathSignal = dataArray.getMyArray();
            BreathCycleCondition condition = new BreathCycleCondition(defalutFluxS * FreeApp.minAlarmFlux / 100, 12, 10);
            localMins_refined = ArrayAnalyser.findLocalMins_refined(currentBreathSignal, condition);

            // 判断呼吸间隔----check if there is state new breath cycle-->
            // for timer to record the time elapsed until the next breath cycle
            int state = checkState();
            if (state == FreeApp.HAS_HUMAN_DEEPSLEEP) {
                if (FreeApp.getInstance().isDebug())
                    Log.i(TAG, FreeApp.ALARMS_BREATH + "localMins_refined" + Arrays.toString(localMins_refined));
                checkAlarm();
                timer++;  // also keep a record of time elapsed until a next breath cycle, one unit corresponds to 0.2 sec
            }

            saveFlux(state, currentBreathSignal);
            dataArray.setMyArray(currentBreathSignal);
            dataArray.setIndicators(localMins_refined);
            bundle.putParcelable("DATA_ARRAY", dataArray);
            intent.setAction(ACTION_UPDATEUI);
            intent.putExtras(bundle);
            sendBroadcast(intent);


            count++;
            int ymdhms, respiratoryRate, smallFluxCount, overTimeCount, temp5;
            if (count == 60) temp1 = state;
            if (count == 120) temp2 = state;
            if (count == 180) temp3 = state;
            if (count == 240) temp4 = state;
            if (count == 300) {
                temp5 = state;
                ymdhms = (int) (System.currentTimeMillis() / 1000);
                respiratoryRate = SystemStateManger.getBreathFreq();
                smallFluxCount = SystemStateManger.getFluxCount();
                overTimeCount = SystemStateManger.getPauseCount();
                fileManager.setStateFile(ymdhms, respiratoryRate, smallFluxCount, overTimeCount, temp1, temp2, temp3, temp4, temp5);
                if (FreeApp.getInstance().isDebug())
                    Log.d(TAG, "State Data:" + ymdhms + "-" + respiratoryRate + "-" + smallFluxCount + "-" + overTimeCount
                            + "-" + temp1 + "-" + temp2 + "-" + temp3 + "-" + temp4 + "-" + temp5);
                SystemStateManger.resetCount();
                count = 0;
            }
        }
    };

    private void saveFlux(int state, int[] currentBreathSignal) {
        BreathCycleCondition condition = new BreathCycleCondition(defalutFluxS * FreeApp.minAlarmFlux / 200, 12, 10);
        int[] localMins_refined = ArrayAnalyser.findLocalMins_refined(currentBreathSignal, condition);
        // 计算通量---- get breath fluxes ----
        int[] breathFlux = ArrayAnalyser.findBreathFlux(currentBreathSignal, localMins_refined);
        if (FreeApp.getInstance().isDebug())
            Log.d(TAG, "localMins_refined2:" + Arrays.toString(localMins_refined));
        //计算标准通量 传入SP
        int flux = 0;
        if (state == FreeApp.HAS_HUMAN_DEEPSLEEP && localMins_refined.length > 10) {
            for (int aa : breathFlux) {
                flux = flux + aa;
            }
            flux = flux / breathFlux.length;
            dataArray.saveDefaultFlux(flux);
                }

        }

    private int checkState()     {
        //计算300个数据的信噪比
        bigcount++;
        if (FreeApp.getInstance().isDebug())
            Log.d(TAG, "bigcount:" + bigcount);

        if (ArrayAnalyser.isNone(currentBreathSignal, 0.1)) {
            SystemStateManger.resetSleepState(FreeApp.NO_HUMAN);
            SystemStateManger.upNohumanCount();
            return FreeApp.NO_HUMAN;

        } else if (ArrayAnalyser.isDeepSleep(currentBreathSignal, FreeApp.THRESHOLD)) {
            SystemStateManger.resetSleepState(FreeApp.HAS_HUMAN_DEEPSLEEP);
            SystemStateManger.upHashumanDeepCount();
            return FreeApp.HAS_HUMAN_DEEPSLEEP;

        } else if (ArrayAnalyser.isDeepSleep2(currentBreathSignal, FreeApp.THRESHOLD2)) {

            SystemStateManger.resetSleepState(FreeApp.HAS_HUMAN_DEEPSLEEP);
            SystemStateManger.upHashumanDeepCount();
            return FreeApp.HAS_HUMAN_DEEPSLEEP;

        } else {
            SystemStateManger.resetSleepState(FreeApp.HAS_HUMAN_NONDEEP_SLEEP);
            SystemStateManger.upHashumanShalowCount();
            return FreeApp.HAS_HUMAN_NONDEEP_SLEEP;
        }
    }

    //如果判断是一个呼吸间隔，且是深睡状态，则进行判断是否为异常
    private synchronized void checkAlarm() {
        if (localMins_refined[0] == 12) {
            SystemStateManger.upBreathCycleCount();  // --------更新呼吸统计信息-----------
            checkAlarmFlux(); //check by Sleep state !
            timer = 0;  // new cycle identified --> reset timer to 0
        }
    }

    // sync on alarmManager instance, to make sure time stamping is correct.
    public void checkAlarmFlux() {

        // 计算通量---- get breath fluxes ----
        int breathFluxAVE = ArrayAnalyser.aveIntArray(ArrayAnalyser.findBreathFlux(currentBreathSignal, localMins_refined));

        int defaultFlux = breathFluxAVE < ((defalutFluxL + defalutFluxS) / 2) ? defalutFluxS : defalutFluxL;
        short alarmType;

        if (bigcount > 300)
            if (timer > FreeApp.minBreathInterval) {   // minimum alarm separation: 10 seconds
                if (FreeApp.getInstance().isDebug())
                    Log.e(TAG, "timer:" + timer);
                int[] breathFlux = ArrayAnalyser.findBreathFlux(currentBreathSignal, localMins_refined);
                if (breathFlux[0] >  defalutFluxS) {
                    SystemStateManger.upOtherFluxCount();
                    alarmType =FreeApp.otherAlarm;
                } else {
                    //通量<50% >20%
                    BreathCycleCondition recordCondition_1 =
                            new BreathCycleCondition(defaultFlux * FreeApp.minBreathOverTimeDefault / 100, 12, 10);
                    int[] localMins_refined = ArrayAnalyser.findLocalMins_refined(currentBreathSignal, recordCondition_1);

                    // 存在呼吸间隔，则为低通量
                    if (localMins_refined.length > 1) {
                        if (FreeApp.getInstance().isDebug()) {
                            String str_smallFlux = "small flux!" + Thread.currentThread().getName() + "   --current sleep state:" + SystemStateManger.getSleepState();
                            Log.i(FreeApp.ALARMS_BREATH, str_smallFlux);
                        }
                        SystemStateManger.upSmallFluxCount(); // up date alarm count
                        alarmType = FreeApp.smallFluxAlarm;

                    } else {

                        //通量<20% >10%
                        recordCondition_1 =
                                new BreathCycleCondition(defaultFlux * FreeApp.minBreathLowFluxDefault / 100, 12, 10);
                        localMins_refined = ArrayAnalyser.findLocalMins_refined(currentBreathSignal, recordCondition_1);

                        if (localMins_refined.length == 0) {
                            if (FreeApp.getInstance().isDebug()) {
                                String str_overTime = "nervousPauseAlarm" + Thread.currentThread().getName() + "   --current sleep state:" + SystemStateManger.getSleepState();
                                Log.i(FreeApp.ALARMS_BREATH, str_overTime);
                            }
//                    //通量<10%，为中枢性暂停
                            SystemStateManger.upNervousPauseCount();// up date alarm count
                            alarmType = FreeApp.nervousPauseAlarm;
                        } else if (localMins_refined.length > 3) {
                            if (FreeApp.getInstance().isDebug()) {
                                String str_overTime = "physicalPause!" + Thread.currentThread().getName() + "   --current sleep state:" + SystemStateManger.getSleepState();
                                Log.i(FreeApp.ALARMS_BREATH, str_overTime);
                            }
                            SystemStateManger.upPhysicalPauseCount();// up date alarm count
                            alarmType = FreeApp.physicalPauseAlarm;

                        } else {
                            //否则为混合型暂停
                            if (FreeApp.getInstance().isDebug()) {
                                String str_overTime = "mixedPauseAlarm" + Thread.currentThread().getName() + "   --current sleep state:" + SystemStateManger.getSleepState();
                                Log.i(FreeApp.ALARMS_BREATH, str_overTime);
                            }
                            SystemStateManger.upMixedPauseCount();// up date alarm count
                            alarmType = FreeApp.mixedPauseAlarm;
                        }
                    }
                    int ymdhms = (int) (System.currentTimeMillis() / 1000);
                    NetApi.getInstance().sendAlarmParcel(ymdhms, (short) timer, currentBreathSignal, alarmType);
                    if (FreeApp.getInstance().isDebug())
                        Log.e(TAG, "ALARMINFO:" + ymdhms + "-" + alarmType + "-" + Arrays.toString(currentBreathSignal) + "-" + timer / 5);
                    NetApi.getInstance().closeALL();
                    fileManager.setAlarmFile(ymdhms, (short) timer, currentBreathSignal, alarmType);
                }
            }
    }
}
public class HandlerService extends IntentService {
    private FreeApp freeApp;
    private DataArray dataArray;
    private static final String TAG = "HandlerService";
    private static final String ACTION_UPDATEUI = "com.webcon.newbreath.UPUI";
    private DataTransmitter transmitter;

    @Override
    public void onCreate() {
        super.onCreate();
        freeApp = (FreeApp) getApplication();
        dataArray = new DataArray();
        transmitter = new DataTransmitter();
        transmitter.setDataObserver(breathDataObserver);

    }

    public HandlerService() {
        super("HandlerService");
    }

    public HandlerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int[] temp = new int[1];
        temp[0] = intent.getIntExtra("DATA", 0);
        transmitter.streamIn(temp);
        if (freeApp.isDebug())
            Log.i(TAG, "printf num:" + temp[0]);
    }

    private Observer<int[]> breathDataObserver = new Observer<int[]>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        // all the calls here are on worker thread as specified in DataTransmitter
        @Override
        public void onNext(int[] ints) {
            //update breath real time wave form 实时数据传入
            dataArray.pushData(ints);

            // call analyser to check if there is any type of alarm 实时数据处理
            analyse();

        }
    };
    private int[] currentBreathSignal;
    private int[] localMins_refined;
    private int defalutFlux;
    private BreathCycleCondition condition;
    private Intent intent = new Intent();

    private Bundle bundle = new Bundle();

    private void analyse() {
        // get breath signal 每0.2s 获取一组300个数据
        currentBreathSignal = dataArray.getBreathArray();
        defalutFlux = 7000;
        condition = new BreathCycleCondition(defalutFlux * FreeApp.minAlarmFlux / 100, 12, 10);
        localMins_refined = ArrayAnalyser.findLocalMins_refined(currentBreathSignal, condition);
        dataArray.setMyArray(currentBreathSignal);
        dataArray.setIndicators(localMins_refined);
        bundle.putParcelable("DATA_ARRAY", dataArray);
        intent.setAction(ACTION_UPDATEUI);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

}

