package com.iruss.mogivisions.kiosk;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Moses on 5/8/2018.
 */

public class MyTimer {

    private Timer timer;
    private TimerRunning timerRuningListener;
    private double remainingSec, startSec;
    public boolean isRunning;


    private static final String TAG = "MyTimer";
    private static final double TIMER_DELAY = 0.25; // Seconds


    private static final MyTimer ourInstance = new MyTimer();

    public static MyTimer getInstance() {
        return ourInstance;
    }


    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            int[] timeArr = (int[]) msg.obj;

            if (timeArr[0] <= 0) {
                MyTimer.this.stopTimer();

                if(timerRuningListener != null) {
                    timerRuningListener.onTimerStopped();
                }
            } else {
                if(timerRuningListener != null) {
                    timerRuningListener.onTimerChange(createDateFormat(timeArr[0]));
                }
            }


        }
    };


    /**
     * Sets how long the timer will run for
     * @param seconds
     */
    public void startTimer(final int seconds) {

        startSec = 0;
        //Log.i("Hours", Integer.toString((seconds % 3600) / 3600));


        remainingSec = seconds;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isRunning = true;
                Message message = Message.obtain();
                int[] counters = new int[2];
                counters[0] = (int)remainingSec;
                counters[1] = (int)startSec;
                //Log.i(TAG, "Timer running: " + remainingSec + ", " + startSec);
                message.obj = counters;
                message.setTarget(mHandler);
                //checks whether or not the timer should be stopped
                mHandler.sendMessage(message);
                remainingSec -= TIMER_DELAY;
                startSec += TIMER_DELAY;


            }
        }, 100, (int)(TIMER_DELAY * 1000));

    }


    /**
     * Setting the timer format
     *
     * @param seconds
     * @return
     */
    public String createDateFormat(int seconds) {
        //return String.format("Time remaining: %02dH:%02dM:%02dS", TimeUnit.MILLISECONDS.toHours(millisUntilFinished), TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

        return String.format("Time remaing: %02dH:%02dM:%02dS", (seconds / 3600) , (seconds % 3600) / 60, seconds % 60 );
    }

    public void setTimerRuningListener(TimerRunning timerRuningListener) {
        this.timerRuningListener = timerRuningListener;
    }


    /**
     * Requires the KioskService to implement these methods so the Timer can update the time in the KioskFragment
     */
    public interface TimerRunning {
        void onTimerChange(String remainSec);

        void onTimerStopped();
    }

    /**
     * Stops the timer when the timer runs out
     */
    private void stopTimer() {
        if (timer != null) {
            timerRuningListener.onTimerStopped();
            isRunning = false;
            timer.cancel();
        }
    }

}
