package com.iruss.mogivisions.kiosk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.iruss.mogivisions.procrastimatev1.HomeActivity;
import com.iruss.mogivisions.procrastimatev1.R;

//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationManagerCompat;


public class LongLifeBroadCastService extends JobService {
    private final static String TAG = "LongLifeBroadCast";

    NotificationCompat.Builder mBuilder;
    private String CHANNEL_ID = "PROCRASTIMATE_CHANNEL";
    private int NOTIFICATION_ID = 2003; // Just some unique ID

    private int timeLimit = 0;


    public static final String COUNTDOWN_BR = "com.iruss.mogivisions.kiosk.longlife_count_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    Intent kiosk;
    private CountDownTimer cdt = null;

    //Timer settings
    SharedPreferences mprefs;
    SharedPreferences.Editor editor;

    int startId;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        //initializeNotification();
        //showNotification();
        //startClock(intent, startId);


        this.startId = startId;

        return START_NOT_STICKY;
    }

    public int getTime(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString("lockout_time", "1");
        //Log.d("Settings", syncConnPref );
        //converts an hours into seconds

        String timePick = sharedPref.getString("time_picker", "00:30");
        Log.d("Settings", timePick);
        String[] timeSec = timePick.split(":");
        int timeLock = (Integer.parseInt(timeSec[0]) * 3600) + (Integer.parseInt(timeSec[1]) * 60);
        Log.d("Settings", Integer.toString(timeLock));
        return timeLock;
    }


    public void startClock(){
        Log.i(TAG, "Starting timer...");
        int time = getTime();

        /*
        if(intent != null){
            //time = this.kiosk.getIntExtra("lockTime", 600);
            time = getTime();
        }*/
        //bi = new Intent(getBaseContext(), KioskService.class);
        //Log.i(TAG, Integer.toString(startId));


        //prevent Timer from being started multiple times
        Log.i("Timer Started", Boolean.toString(mprefs.getBoolean("timerNotStarted", true)));
        /*
        if(mprefs.getBoolean("timerNotStarted", true)){
            //Time started in the background
            editor.putBoolean("timerNotStarted", false).apply();*/


            //Selected lock time from setting preferences
            cdt = new CountDownTimer(time * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    long seconds = millisUntilFinished / 1000;
                    Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);

                    //Toast.makeText(getBaseContext(), String.format("%02dH:%02dM:%02dS", (seconds / 3600) , (seconds % 3600) / 60, seconds % 60 ), Toast.LENGTH_SHORT).show();
                    //Updates current time on Kiosk Page
                    bi.putExtra("countdown", String.format("%02dH:%02dM:%02dS", (seconds / 3600) , (seconds % 3600) / 60, seconds % 60 ));
                    sendBroadcast(bi);
                }

                /**
                 * onFinish is called when the CountDown timer is completed
                 * Resets ability to start the timer, and kill the timer
                 */
                @Override
                public void onFinish() {
                    bi.putExtra("countdown", "No more time remaining");
                    sendBroadcast(bi);
                    editor.putBoolean("timerNotStarted", true).apply();
                    editor.putBoolean("timerNotTurnOff", true).apply();
                    Log.i(TAG, "Timer status from onFinish: " + mprefs.getBoolean("timerNotTurnOff", true));
                    initializeNotification();
                    showNotification();

                }
            };
            cdt.start();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Job is being started");

        setTimerStatus();
        startClock();

        return true;
    }


    public void setTimerStatus(){

        timeLimit = getTimeLimit();


        mprefs = this.getSharedPreferences("Tutorial", Context.MODE_PRIVATE);
        editor = mprefs.edit();

        //Checks to see if timerNotStarted and timerNotTurnOff are already created
        Log.i(TAG, "Setting the timer status");
        //editor.putBoolean("timerNotStarted", true).apply();

        if(mprefs.getBoolean("timerNotTurnOff", true)) {
            Log.i(TAG, "Creating clock setting");
            editor.putBoolean("timerNotTurnOff", false).apply();
            editor.putBoolean("timerNotStarted", true).apply();

        }

    }

    /**
     * Retrieve the usage limit that the user has set in the SettingActivity
     * @return, the phone usage limit in seconds
     */
    public int getTimeLimit(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String timePick = sharedPref.getString("phone_usage_limit", "00:30");
        Log.d("Settings", timePick );
        String[] timeSec = timePick.split(":");
        int timeLock = (Integer.parseInt(timeSec[0]) * 3600) + (Integer.parseInt(timeSec[1]) * 60);
        Log.d("Settings", Integer.toString(timeLock));

        return timeLock;
    }



    @Override
    public boolean onStopJob(JobParameters params) {
        //prevent system from destroying timer unless the timer is over
        Log.i(TAG, "Timer status: " + mprefs.getBoolean("timerNotTurnOff", true));
        if(mprefs.getBoolean("timerNotTurnOff", true)){
            editor.putBoolean("timerNotStarted", true).apply();
            editor.putBoolean("timerNotTurnOff", false).apply();
            cdt.cancel();
            Log.i(TAG, "Timer cancelled");
            //stopService(new Intent(this, LongLifeBroadCastService.class));
            super.onDestroy();


        }
        super.onDestroy();
        return false;
    }


    @Override
    public void onDestroy(){
        Log.i(TAG, "Timer status: " + mprefs.getBoolean("timerNotTurnOff", true));
        if(mprefs.getBoolean("timerNotTurnOff", true)){
            editor.putBoolean("timerNotStarted", true).apply();
            editor.putBoolean("timerNotTurnOff", false).apply();
            cdt.cancel();
            Log.i(TAG, "Timer cancelled");
            //stopService(new Intent(this, LongLifeBroadCastService.class));
            //super.onDestroy();


        }

        super.onDestroy();
    }



    /**
     * Sets up notification that user will see when they are over the their limit
     */
    private void initializeNotification() {
        Log.i(TAG, "notification is being created");


        // Set up intent for when notification is tapped
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock_outline_black_24dp)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Create a channel and set the importance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Show the notification after it has been created
    private void showNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
