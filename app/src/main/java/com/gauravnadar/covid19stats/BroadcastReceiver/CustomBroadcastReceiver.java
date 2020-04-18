package com.gauravnadar.covid19stats.BroadcastReceiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.gauravnadar.covid19stats.MainActivity;
import com.gauravnadar.covid19stats.Scheduler;
import com.gauravnadar.covid19stats.Util.Job;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.content.Context.MODE_PRIVATE;


public class CustomBroadcastReceiver extends BroadcastReceiver {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        Log.e("log", intent.getAction());

        if(intentAction != null)
        {
            Log.e("check", "inside");

          if(Intent.ACTION_MY_PACKAGE_REPLACED.equals(intentAction))
          {

              Job.scheduleJob(context);
              Log.e("Broadcast", "Working");
          }

        }


    }
}
