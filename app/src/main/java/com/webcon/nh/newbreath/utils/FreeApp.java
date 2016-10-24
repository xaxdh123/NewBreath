package com.webcon.nh.newbreath.utils;

import android.app.Application;

/**
 * FreeApp Application
 * Created by NH on 2016/8/24.
 */

public class FreeApp extends Application {
    // observer state
    public static final int OBSERVER_NULL = -234;
    public static final int DATA_SENT_OBSERVER = 667;
    public static final String THREAD_INDICATOR = "THREAD_INDICATOR";                     //数据处理所在线程名
    public static final String ALARMS_BREATH = "ALARMS_BREATH";
    public static final int DATAARRAY_TOTAL_LENGTH = 300;                                 //默认数组大小-一分钟数据

    // --- time interval above which a wakeUpAlarm is triggered ---

    public static final double THRESHOLD = 3L;                                            //信噪比 判断深睡状态的标志
    public static final int THRESHOLD2 = 2;                                            //波形平稳 标尺
    public static final int minBreathInterval = 50;                                       //最小呼吸异常判断时间 5*10    10s
    public static final int minAlarmFlux = 50;                                            // 最低报警通量 50%
    public static final int minBreathOverTimeDefault = 20;                                //阻塞性暂停报警 20%
    public static final int minBreathLowFluxDefault = 10;                                 // 中枢性暂停报警 10%
    public static final int defalutFluxAnaysyer = 3;                                     // 标准通量权重 3%

    public static final String SHAREPREFERENCE_DEFAULT_FLUX = "sp_default_flux";          //xml存储defaultFlux 用于自定义
    public static final String DEFAULT_FLUXS_STRING = "default_fluxS";                      //用于键值对-key
    public static final String DEFAULT_FLUXL_STRING = "default_fluxL";                      //用于键值对-key
    public static final int DEFAULT_FLUX_INT = 7000;                                      //用于键值对-value 默认defalut-flux

    //------  sleep state --------
    public static final int HAS_HUMAN_DEEPSLEEP = 1327;
    public static final int HAS_HUMAN_NONDEEP_SLEEP = 129827;
    public static final int NO_HUMAN = -2323324;
    //-------- pdu --------
    public static final short DBSUB_AddAlarmInfo_Req_FromNHSystem = 94;
    public static final short DBSUB_AddAlarmInfo_Rsp_ToNHSystem = 95;
    public static final short PDU_NHSystemDBROUTER0_REQ_FromNHSystem = 10001;
    public static final short PDU_NHSystemDBROUTER0_RSP_ToNHSystem = 10006;
    public static final short PDU_NHSystemServerReport_REQ_FromNHSystem = 10015;
    public static final short PDU_NHSystemServerReport_RSP_ToNHSystem = 10018;
    public static final short PDU_NHSystemServerSave_REQ_FromNHSystem = 10019;
    public static final short PDU_NHSystemServerSave_RSP_ToNHSystem = 10020;


    // types of alarms
    public static final short smallFluxAlarm = 0x1;// --breath flux is seen to be smaller than the target level--
    public static final short physicalPauseAlarm = 0x2; // --a target amount of time elapsed with out identifying an breath cycle--
    public static final short nervousPauseAlarm = 0x4; // --a target amount of time elapsed with out identifying an breath cycle--
    public static final short mixedPauseAlarm = 0x8; // --a target amount of time elapsed with out identifying an breath cycle--
    public static final short otherAlarm = 0x16; // --a target amount of time elapsed with out identifying an breath cycle--

    public static final String ADDRESS_IP = "180.153.155.7";// 本地IP:192.168.1.81 外网IP：180.153.155.7
    public static final int ADDRESS_PORT_REGIST = 8500;
    public static final int ADDRESS_PORT_SEND = 8502;
    public static final short REISTER_STATES = 0;
    public static final short END_FLAG_TRUE = 1;
    public static final short END_FLAG_FALSE = -1;

    public void setDEFAULT_SERVERID(String DEFAULT_SERVERID) {
        this.DEFAULT_SERVERID = DEFAULT_SERVERID;
    }

    public String DEFAULT_SERVERID = "NH120212303";

    //-----record files--------
    public static final String DEVICE_NUM = "A1";
    public static final String RECORD_FILE_BREATH = ".nhBreath";
    public static final String RECORD_FILE_ALARM = ".nhAlarm";
    public static final String RECORD_FILE_STATES = ".nhStates";


    private double percentageB;
    private double percentageN;
    private String encodingCode;

    public String getEncodingCode() {
        return encodingCode;
    }


    public double getPercentageB() {
        return percentageB;
    }

    public void setPercentageB(double percentageB) {
        this.percentageB = percentageB;
    }

    public double getPercentageN() {
        return percentageN;
    }

    public void setPercentageN(double percentageN) {
        this.percentageN = percentageN;
    }

    private static FreeApp singleton;

    //取得Application单件
    public static FreeApp getInstance() {
        return singleton;
    }

    //调试，正式版改为false 提高性能
    public boolean isDebug() {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        singleton = this;
    }
}
