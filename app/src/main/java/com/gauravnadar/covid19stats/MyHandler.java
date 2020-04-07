package com.gauravnadar.covid19stats;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class MyHandler extends Handler {

    Looper looper;
    Context context;
    public MyHandler(Looper looper, Context context){
        this.looper = looper;
        this.context = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);

        Log.e("XXXXXXXXXXX", (String) msg.obj);
    }
}
