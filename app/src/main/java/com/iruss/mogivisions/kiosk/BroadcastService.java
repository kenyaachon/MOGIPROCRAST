package com.iruss.mogivisions.kiosk;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service {

    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_BR = "com.iruss.mogivisions.kiosk.count_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    Intent kiosk;
    private CountDownTimer cdt = null;

    boolean timerNotStarted;

    @Override
    public void onCreate() {
        super.onCreate();

        timerNotStarted = true;
    }

    @Override
    public void onDestroy() {

        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
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

        //Selected lock time from setting preferences
        int time = kiosk.getIntExtra("lockTime", 600);

        //bi.setAction(KioskService.BROADCAST_ACTION);



        //bi = new Intent(getBaseContext(), KioskService.class);

        Log.i(TAG, Integer.toString(startId));

        //prevent Timer from being started multiple times
        if(timerNotStarted){
            //Time started in the background
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
                }
            };


            timerNotStarted = false;

            cdt.start();

        }



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}