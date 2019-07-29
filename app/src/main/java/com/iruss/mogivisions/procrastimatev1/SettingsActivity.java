// Copyright 2017 SDK Bridge
package com.iruss.mogivisions.procrastimatev1;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.iruss.mogivisions.kiosk.DelayService;

//import android.support.v4.app.NotificationCompat;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;

/**
 * Activity to show the settings
 */
public class SettingsActivity extends AppCompatActivity {
    // For notifications
    NotificationCompat.Builder mBuilder;
    private String CHANNEL_ID = "PROCRASTIMATE_CHANNEL";
    private int NOTIFICATION_ID = 2001; // Just some unique ID
    private int JOB_ID = 200258;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Allows for a custome title to be used
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.settingtitle);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new com.iruss.mogivisions.procrastimatev1.SettingsFragment())
                .commit();

        //Goes back to home activity
        ImageButton settingsButton = findViewById(R.id.returnHome);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


        //decideToCreateNotification();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        decideToCreateNotification();
    }
    public void decideToCreateNotification(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean timePick = sharedPref.getBoolean("phone_usage_limit_notification", false);
        if(timePick){
            Log.i("SettingsActivity", "Notifications are turned on");
            backgroundNotification();
        }
    }


    public void backgroundNotification(){
        ComponentName serviceName = new ComponentName(this, DelayService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiresCharging(false)
                .setMinimumLatency(3600000)
                .build();


        //3600000
        JobScheduler scheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Log.i("SettingsActivity", " Delay Job scheduled successfully");
        }

        Intent service = new Intent(this, DelayService.class);
        stopService(service);
        startService(service);
    }




}
