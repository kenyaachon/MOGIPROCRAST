package com.iruss.mogivisions.experiment;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.kiosk.KioskService;

import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class HomeActivity extends AppCompatActivity  {
    //Ads
    private AdView mAdView;

    private boolean shouldBeInKioskMode = false;
    public final static int Overlay_REQUEST_CODE = 251;

    public boolean isShouldBeInKioskMode() {
        return shouldBeInKioskMode;
    }

    public void setShouldBeInKioskMode(boolean shouldBeInKioskMode) {
        this.shouldBeInKioskMode = shouldBeInKioskMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        shouldBeInKioskMode = false;

        initializeKiosk();
        initializeSettings();
        //sample AddMob Id
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        userStats();

    }



    public void initializeSettings(){
        Button settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    public void initializeKiosk(){
        Button kioskButton = findViewById(R.id.kiosk);

        kioskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // If version >= 23, then need to ask for overlay permission
                if (Build.VERSION.SDK_INT >= 23) {
                    // Check if you have permission already. If not, then ask
                    if (!Settings.canDrawOverlays(HomeActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, Overlay_REQUEST_CODE);
                    } else {
                        startKiosk();
                    }
                } else {
                    startKiosk();
                }
            }
        });
    }

    // Starts the kiosk service to overlay on top
    private void startKiosk() {
        // Terrible workaround -- not good practice at all to use a static member for this
        KioskService.homeActivity = this;

        Intent intent = new Intent(this, KioskService.class);
        stopService(intent);
        startService(intent);
        shouldBeInKioskMode = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldBeInKioskMode) {
            startKiosk();
        }
    }

    // Called when permission is granted
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Overlay_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        startKiosk();
                    }
                } else {
                    startKiosk();
                }
                break;
            }
        }
    }

    /**
     * Tells how much the user has been using their phone
     */
    public void userStats() {
        //Checkks user phone statistics
        //Look to make sure we check if the app has access if it does then we just run the usage statistics
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);

        long TimeInforground = 500;

        int minutes = 500, seconds = 500, hours = 500;
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        long time = System.currentTimeMillis();

        long totalPhoneTime = 0;

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

        if (stats != null) {
            for (UsageStats usageStats : stats) {

                TimeInforground = usageStats.getTotalTimeInForeground();
                totalPhoneTime += TimeInforground;

                //conversion of phone usage statisticts from milli to seoncds, minutes, and hours
                minutes = (int) ((TimeInforground / (1000 * 60)) % 60);
                seconds = (int) (TimeInforground / 1000) % 60;
                hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);

                Log.i("BAC", "PackageName is" + usageStats.getPackageName() + "Time is: " + hours + "h" + ":" + minutes + "m" + seconds + "s");
            }
        }

        Log.i("Total phone use time", Long.toString(totalPhoneTime));
        //Set the trivia difficulty
        if(totalPhoneTime <= 9600){
            TriviaAPI.questionDifficulty = "easy";
        }
        else if(totalPhoneTime >= 9601 && totalPhoneTime <= 18000 ){
            TriviaAPI.questionDifficulty = "medium";
        }
        else {
            TriviaAPI.questionDifficulty = "hard";
        }

        Log.e("End", "The Loop has ended");
    }
}
