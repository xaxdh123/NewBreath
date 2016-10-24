package com.webcon.nh.newbreath.RxJava;


import com.webcon.nh.newbreath.utils.FreeApp;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * DataTransmitter
 * Created by Administrator on 16-1-13.
 */
public class DataTransmitter {

    public synchronized int streamIn(int[] ints1) {

        if (null == observerSubscriber) return FreeApp.OBSERVER_NULL;
        else {
            observerSubscriber.onNext(ints1);
            return FreeApp.DATA_SENT_OBSERVER;
        }
    }

    // advantage--> specifying the working thread of the observer: preferably non-UI thread

    public void setDataObserver(Observer<int[]> observer) {
        dataObservable
                .subscribeOn(Schedulers.computation())  // data generation on .... worker thread
                .observeOn(Schedulers.computation())   // data handling on .... (main thread.  AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Subscriber<int[]> observerSubscriber;

    // dataObservable's responsibility is to obtain the subscriber which the dataObservable is subscribed to
    private Observable<int[]> dataObservable = Observable.create(new Observable.OnSubscribe<int[]>() {
        @Override
        public void call(Subscriber<? super int[]> subscriber) {
            observerSubscriber = (Subscriber<int[]>) subscriber;
        }
    });


}
