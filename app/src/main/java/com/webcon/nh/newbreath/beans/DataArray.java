package com.webcon.nh.newbreath.beans;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.webcon.nh.newbreath.utils.FreeApp;

/**
 * DataArray
 * Created by NH on 2016/8/26.
 */

public class DataArray implements Parcelable {
    // ---default constructor --> length 300----
    private DataArray() {
        this.myArray = new int[totalLength];
    }

    private static DataArray instance;

    public static synchronized DataArray getInstance() {
        if (instance == null) {
            instance = new DataArray();
        }
        return instance;
    }

    private int[] myArray;
    private int[] indicators;
    private SharedPreferences preferences;
    private int defaultFluxS;
    private int defaultFluxL;
    private int totalLength = FreeApp.DATAARRAY_TOTAL_LENGTH;


    public synchronized int[] getMyArray() { //---lock on dataArray object---
        return myArray;
    }

    public void setMyArray(int[] myArray) {
        this.myArray = myArray;
    }

    public int[] getIndicators() {
        return indicators;
    }

    public void setIndicators(int[] indicators) {
        this.indicators = indicators;
    }

    public int getDefaultFluxS() {
        return defaultFluxS;
    }

    public int getDefaultFluxL() {
        return defaultFluxL;
    }

    public void setPreferences(SharedPreferences sharedPreferences) {
        preferences = sharedPreferences;
        defaultFluxL = preferences.getInt(FreeApp.DEFAULT_FLUXL_STRING, FreeApp.DEFAULT_FLUX_INT);
        defaultFluxS = preferences.getInt(FreeApp.DEFAULT_FLUXS_STRING, FreeApp.DEFAULT_FLUX_INT);
    }

    public void saveDefaultFlux(int defaultFlux) {
        SharedPreferences.Editor editor = preferences.edit();
        boolean nearL = defaultFlux > defaultFluxL || defaultFlux >= defaultFluxS && (Math.abs(defaultFlux - defaultFluxL)) <= (Math.abs(defaultFlux - defaultFluxS));
//        if (defaultFlux < 1.2 * defaultFluxL && defaultFlux > 0.8 * defaultFluxL
//                || defaultFlux < 1.2 * defaultFluxS && defaultFlux > 0.8 * defaultFluxS) {

            if (nearL) {
                defaultFluxL = defaultFluxL * (100 - FreeApp.defalutFluxAnaysyer) / 100 + defaultFlux * FreeApp.defalutFluxAnaysyer / 100;
                editor.putInt(FreeApp.DEFAULT_FLUXL_STRING, defaultFluxL);
            } else {
                defaultFluxS = defaultFluxS * (100 - FreeApp.defalutFluxAnaysyer) / 100 + defaultFlux * FreeApp.defalutFluxAnaysyer / 100;
                editor.putInt(FreeApp.DEFAULT_FLUXS_STRING, defaultFluxS);
            }
            editor.apply();
//        }
    }

    public void pushData(int[] ints) {
        synchronized (this) {  //---lock on dataArray object---
            if (FreeApp.getInstance().isDebug())
                Log.i(FreeApp.THREAD_INDICATOR, "pushing in" + Thread.currentThread().getName());
            System.arraycopy(myArray, 0, myArray, 1, totalLength - 1);  // shift the array to right by inputLength
            System.arraycopy(ints, 0, myArray, 0, 1);  //  add the first inputLength sub-array
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.myArray);
        dest.writeIntArray(this.indicators);
        dest.writeInt(this.defaultFluxS);
        dest.writeInt(this.defaultFluxL);
    }

    private DataArray(Parcel in) {
        this.myArray = in.createIntArray();
        this.indicators = in.createIntArray();
        this.defaultFluxS = in.readInt();
        this.defaultFluxL = in.readInt();
    }

    public static final Creator<DataArray> CREATOR = new Creator<DataArray>() {
        @Override
        public DataArray createFromParcel(Parcel source) {
            return new DataArray(source);
        }

        @Override
        public DataArray[] newArray(int size) {
            return new DataArray[size];
        }
    };
}
