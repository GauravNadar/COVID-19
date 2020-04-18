package com.gauravnadar.covid19stats.Util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.gauravnadar.covid19stats.Scheduler;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class Job {


    public static void scheduleJob(Context context)
    {

        ComponentName componentName = new ComponentName(context, Scheduler.class);
        JobInfo info = new JobInfo.Builder(111, componentName)
                .setPeriodic(15 * 60 * 1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();


        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        int resutCode = scheduler.schedule(info);


        if (resutCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Job", "Result Success");

        } else {
            Log.d("Job", "Result Failed");
        }

    }

}
