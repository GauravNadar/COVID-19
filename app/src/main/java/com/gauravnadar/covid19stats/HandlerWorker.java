package com.gauravnadar.covid19stats;

import android.os.Handler;
import android.os.HandlerThread;

public class HandlerWorker extends HandlerThread {

private Handler handler;

    public HandlerWorker() {
        super("Handler Thread");
        start();
        handler = new Handler(getLooper()){

        };
    }

    public HandlerWorker execute(Runnable task)
    {
        handler.post(task);
        return this;
    }
}
