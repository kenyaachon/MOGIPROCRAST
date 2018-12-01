package com.iruss.mogivisions.kiosk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_BR = "com.iruss.mogivisions.kiosk.count_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    Intent kiosk;
    private CountDownTimer cdt = null;

    //Timer settings
    SharedPreferences mprefs;
    SharedPreferences.Editor editor;


    //boolean timerTurnOff = false;
    //boolean timerNotStarted;

    @Override
    public void onCreate() {
        super.onCreate();

        mprefs = this.getSharedPreferences("Tutorial", Context.MODE_PRIVATE);
        editor = mprefs.edit();

        //Checks to see if timerNotStarted and timerNotTurnOff are already created
        if(mprefs.getBoolean("timerNotTurnOff", true)) {
            Log.i(TAG, "Creating clock setting");
            editor.putBoolean("timerNotTurnOff", false).apply();
        }

        editor.putBoolean("timerNotStarted", true).apply();


    }


    @Override
    public void onDestroy() {

        //prevent system from destroying timer unless the timer is over
        if(mprefs.getBoolean("timerNotTurnOff", true)){
            cdt.cancel();
            Log.i(TAG, "Timer cancelled");
            super.onDestroy();
        }

    }

    /**
     * Starts background timer and broadcasts signals to Broasdcast Receiver in KioskService
     * @param intent, intent used to call BroadcastService which includes information on selected lock time
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.kiosk = intent;



        //bi = new Intent(getApplication().getBaseContext(), KioskService.class);

        Log.i(TAG, "Starting timer...");

        //bi.setAction(KioskService.BROADCAST_ACTION);

        int time = 0;
        if(intent != null){
            time = this.kiosk.getIntExtra("lockTime", 600);

        }


        //bi = new Intent(getBaseContext(), KioskService.class);

        Log.i(TAG, Integer.toString(startId));

        //prevent Timer from being started multiple times
        Log.i("Timer Started", Boolean.toString(mprefs.getBoolean("timerNotStarted", true)));
        if(mprefs.getBoolean("timerNotStarted", true)){
            //Time started in the background

            //Selected lock time from setting preferences
            //int time = this.kiosk.getIntExtra("lockTime", 600);

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

                @Override
                public void onFinish() {
                    bi.putExtra("countdown", "No more time remaining");
                    sendBroadcast(bi);
                    editor.putBoolean("timerTurnOff", true).apply();

                    onDestroy();
                }
            };

            //Prevent timer from being started twice
            editor.putBoolean("timerNotStarted", false).apply();

            cdt.start();

        }



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}