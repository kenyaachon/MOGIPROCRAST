package com.iruss.mogivisions.kiosk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.iruss.mogivisions.procrastimatev1.R;
import com.iruss.mogivisions.procrastimatev1.SplashActivity;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class JobSchedulerService extends JobService {

    private final static String TAG = "JobSchedulerService";

    NotificationCompat.Builder mBuilder;
    private String CHANNEL_ID = "PROCRASTIMATE_CHANNEL";
    private int NOTIFICATION_ID = 2002; // Just some unique ID

    private int timeLimit = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        //initializeNotification();
        //showNotification();
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Job is being started");

        timeLimit = getTimeLimit();
        getUsageStats();

        return false;
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

    public void getUsageStats(){
        // Do something for lollipop and above versions




        int minutes = 500, seconds = 500, hours = 500;
        //UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats);
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long totalPhoneTime = 0;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);


        //Get the list of app usage stats for one full day
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(), System.currentTimeMillis());



        //Puts the list of app usages into a sorted map
        SortedMap<String, UsageStats> mySortedMap = new TreeMap<>();
        if (stats != null) {
            for (UsageStats usageStats : stats) {
                mySortedMap.put(usageStats.getPackageName(), usageStats);
            }
        }

        //Going through the map to put it into a list to format the data for display in the bar chart
        SortedSet<Map.Entry<String, UsageStats>> sortedMap = entriesSortedByValues(mySortedMap);
        Iterator it = sortedMap.iterator();

        //Formating the data as BarEntry's
        for (int i = 0; i < mySortedMap.size(); i++) {
            Map.Entry<String, UsageStats> pair = (Map.Entry<String, UsageStats>) it.next();
            //restricts the amount of apps shown to be ones with good values bigger than 0 minutes
            totalPhoneTime += pair.getValue().getTotalTimeInForeground();


        }


        /*
        if (stats != null) {
            for (UsageStats usageStats : stats) {

                totalPhoneTime += usageStats.getTotalTimeInForeground();

            }

            //Log.i(TAG, "Total Phone usage" + (totalPhoneTime /(1000 * 60 * 60) % 24) + "h" + (totalPhoneTime/(1000 * 60) % 60) + "m" + ( totalPhoneTime/(1000) % 60) + "s"  );
        }*/

        Log.i(TAG, "Total Phone usage" + (totalPhoneTime /(1000 * 60 * 60) % 24) + "h" + (totalPhoneTime/(1000 * 60) % 60) + "m" + ( totalPhoneTime/(1000) % 60) + "s"  );

        //Check if phone usage amount exceeds the set phone usage limit
        if((totalPhoneTime/1000) >= timeLimit){
            Log.i(TAG, "Notification is being created since it met the time limit");
            initializeNotification();
            showNotification();
        }


    }


    //Sorts a map by its Values
    //Helps sort the UsageStats by how long each app has been in the foreground
    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,UsageStats>> entriesSortedByValues(Map<K,UsageStats> map) {
        SortedSet<Map.Entry<K,UsageStats>> sortedEntries = new TreeSet<Map.Entry<K,UsageStats>>(
                new Comparator<Map.Entry<K,UsageStats>>() {
                    @Override public int compare(Map.Entry<K,UsageStats> e1, Map.Entry<K,UsageStats> e2) {
                        //int res = e1.getValue().compareTo(e2.getValue());
                        int res = Long.compare(e1.getValue().getTotalTimeInForeground(), e2.getValue().getTotalTimeInForeground());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }




    /**
     * Sets up notification that user will see when they are over the their limit
     */
    private void initializeNotification() {
        Log.i(TAG, "notification is being created");


        // Set up intent for when notification is tapped, it starts of by showing the splash screen in the beginning
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        //Add action, if user presses on word "Lock Phone", then KioskServices starts and phone is locked
        Intent lock = new Intent(this, KioskService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, lock, 0);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(
                        0, "Lock Phone", pi
                ).build();

        // Build notification
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock_outline_black_24dp)
                .addAction(action)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLights(getResources().getColor(R.color.green), 2000, 2000)
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
    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


}
