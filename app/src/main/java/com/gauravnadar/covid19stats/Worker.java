package com.gauravnadar.covid19stats;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker extends Thread {

    private static final AtomicBoolean alive = new AtomicBoolean(true);
    private ConcurrentLinkedQueue taskQueue = new ConcurrentLinkedQueue();

    public Worker()
    {
        super("Worker");
        start();
    }

    @Override
    public void run() {

        while (alive.get()) {
            Runnable task = (Runnable) taskQueue.poll();
            if(task!= null)
            {
                task.run();
            }
        }

        Log.i("Terminated", "xx");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {


            }
        });

    }


    public Worker execute(Runnable task){

        taskQueue.add(task);
        return this;
    }

    public void quit()
    {
        alive.set(false);
    }


}
