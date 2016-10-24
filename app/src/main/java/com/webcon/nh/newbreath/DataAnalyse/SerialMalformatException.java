package com.webcon.nh.newbreath.DataAnalyse;

/**
 * Created by Administrator on 16-3-16.
 */
public class SerialMalformatException extends Exception {

    SerialMalformatException(){
        super("Serial transmission protocol violated!");
    }
}
