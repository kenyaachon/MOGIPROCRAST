package com.iruss.mogivisions.kiosk;


import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

public class DelayService extends JobService {
    private final static String TAG = "DelayService";
    private int JOB_ID = 200259;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Job is being started");

        //Create the job of calling the usage limit analyzer
        ComponentName serviceName = new ComponentName(this, JobSchedulerService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiresCharging(false)
                .setPersisted(true)
                .setPeriodic(3600000)
                .build();

        //.setPersisted()

        //Schedule the job and make sure the scheduling has been successful
        JobScheduler scheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i("DelayService", "Notification Job scheduled successfully");
        }

        //start the usage limit analyzer
        Intent service = new Intent(this, JobSchedulerService.class);
        startService(service);


        return false;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Job is being stopped");
        return false;
    }
}