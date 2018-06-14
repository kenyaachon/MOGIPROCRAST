package com.iruss.mogivisions.experiment;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.kiosk.KioskService;

import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    //Ads
    private AdView mAdView;

    private boolean shouldBeInKioskMode = false;
    public final static int Overlay_REQUEST_CODE = 251;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private final int MY_PERMISSIONS_REQUEST_READ_USAGE_STATISTICS = 2;

    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;


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
    /**
     * Handles when the settings button is pressed
     * Calls the SettingsActivity
     */
    public void initializeSettings() {
        Button settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        setButtonTextSize(settingsButton);
    }

    /**
     * Handles when the kiosk buttons is pressed
     * calls the KioskService
     */
    public void initializeKiosk() {
        Button kioskButton = findViewById(R.id.kiosk);

        kioskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // If version >= 23, then need to ask for overlay permission

                if (Build.VERSION.SDK_INT >= 23) {
                    boolean okToStartKiosk = true;
                    // Check if you have permission to draw overlays already. If not, then ask
                    if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                        showExplanation(HomeActivity.this.getString(R.string.PhonePermissionRequest), HomeActivity.this.getString(R.string.PhonePermissionRational), MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        okToStartKiosk = false;
                    }
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        showExplanation(HomeActivity.this.getString(R.string.CameraPermissionRequest), HomeActivity.this.getString(R.string.CameraPermissionRequestRationale), MY_PERMISSIONS_REQUEST_CAMERA);
                        okToStartKiosk = false;
                    }
                    if (!Settings.canDrawOverlays(HomeActivity.this)) {
                        showExplanation(HomeActivity.this.getString(R.string.OverlayTitle), HomeActivity.this.getString(R.string.OverlayRequestRationale), Overlay_REQUEST_CODE);
                        okToStartKiosk = false;
                    }
                    if (okToStartKiosk){
                        startKiosk();
                    }
                } else {
                    startKiosk();
                }

            }
        });

        setButtonTextSize(kioskButton);
    }


    /**
     * changes the text size of a button
     * Reads the text size from the settings page
     * @param button that is going to be changed
     */
    public void setButtonTextSize(Button button){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        //Convert string to a float value
        float textSize = Float.parseFloat(syncConnPref);
        button.setTextSize(textSize);
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
                    //If app has permission to be on top, start the Kiosk stop
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
     * Asks the user if this app can be on top
     */
    public void requestPermissionOverlay(){
        if (Build.VERSION.SDK_INT >= 23) {
            // Check if you have permission already. If not, then ask
            if (!Settings.canDrawOverlays(HomeActivity.this)) {
                //Goes to the settings page to request the overlay permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Overlay_REQUEST_CODE);
            } else {
                startKiosk();
            }
        } else {
            startKiosk();
        }
    }



    /**
     * Asks the user permission to access their usage statistics
     */
    public void requestPermissionStats(){

        //create the builder that creates the alert Dialog to the user
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.UsageStatisticsTitle);
        alertDialogBuilder.setMessage(R.string.UsageStatisticsPermissionRequest);
        //If the user accepts request for permission to view usage statistics
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        //display and build the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    /**
     * Requset the permission to view phone usage statistics
     */
    private void askUsagePermission(){

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.PACKAGE_USAGE_STATS)
                    != PackageManager.PERMISSION_GRANTED) {
                /*
                // Permission is not granted
                // Should we show an explanation?
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                */
                    Log.d("Permission", "Showing rationale to access your usage statistics");
                    Log.d("Permission", "Showing rationale for permission request");
                    showExplanation(this.getString(R.string.UsageStatisticsTitle), this.getString(R.string.UsageStatisticsPermissionRational),
                            MY_PERMISSIONS_REQUEST_READ_USAGE_STATISTICS);
                }

    }



    /**
     * Tells how much the user has been using their phone
     */
    public void userStats(){
        //Checkks user phone statistics
        //Look to make sure we check if the app has access if it does then we just run the usage statistics


        //Checks which build version the app is and then checks usage statistics accordingly
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (!checkUsagePermissionGranted()) {
                askUsagePermission();
            }
            // Do something for lollipop and above versions
            long TimeInforground = 500;

            int minutes = 500, seconds = 500, hours = 500;
            //UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats);
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
            setTriviaDifficulty(totalPhoneTime);

            Log.e("End", "The Loop has ended");
        } else {
            // do something for phones running an SDK before lollipop
            Log.e("Statistics Error", "Not able to check attain usage statistics");
        }

    }

    /**
     * Explains why a permission is requested
      * @param title, title of the alert dialgo
     * @param message, message for the alert dialog
     * @param permissionRequestCode, permission being requested
     */
    private void showExplanation(String title,
                                 String message,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch (permissionRequestCode){
                            case MY_PERMISSIONS_REQUEST_READ_USAGE_STATISTICS:
                                requestPermissionStats();
                                break;
                            case Overlay_REQUEST_CODE:
                                requestPermissionOverlay();
                                break;
                            case MY_PERMISSIONS_REQUEST_CAMERA:
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST_CAMERA);
                                break;
                            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                                break;
                        }
                    }
                });
        builder.create().show();
    }




    /**
     * Checks if app has user permission to device camera
     * returns true if Permission to use Camera is allowed
     */
    public boolean cameraCheck(){
        // TODO: Figure this out

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                //"android.permission.PACKAGE_USAGE_STATS")) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("Permission", "Showing rationale to access your camera");
                showExplanation(this.getString(R.string.CameraPermissionTitle), this.getString(R.string.CameraPermissionRequestRationale),
                        MY_PERMISSIONS_REQUEST_CAMERA);

            } else {
                // No explanation needed; request the permission

                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return true;
            }
            //}
        } else {

            return true;
        }
        return false;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {


        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Permission", "Permission granted to access your camera");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to access your camera", Toast.LENGTH_SHORT).show();
                }
            }


        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // If request is cancelled, the result arrays are empty.

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Log.d("Permission", "Permission granted to read phone state");
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Log.d("Permission", "Permission denied to read phone state");
            }
        }

    }

    /**
     * Changes the triviaDifficult of the app
     * @param totalPhoneTime
     */
    public void setTriviaDifficulty(long totalPhoneTime){
        Log.i("Total phone use time", Long.toString(totalPhoneTime));
        //Set the trivia difficulty
        if (totalPhoneTime <= 9600) {
            TriviaAPI.questionDifficulty = "easy";
        } else if (totalPhoneTime >= 9601 && totalPhoneTime <= 18000) {
            TriviaAPI.questionDifficulty = "medium";
        } else {
            TriviaAPI.questionDifficulty = "hard";
        }
    }


    /**
     * Asks permission to read_Phone_state such as incoming and outgoing calls
     */
    public void askPhonePermission(){

        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("Permission", "Showing rationale to make app at the top");
                showExplanation(this.getString(R.string.PhoneTitle), this.getString(R.string.PhonePermissionRational),
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

            } else {
                // No explanation needed; request the permission

                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            //}
        } else {
            // Permission has already been granted

            Log.d("Permission", "Permission already granted to read phone state");
        }
    }


    /**
     * Check if the Usage statistics permission is granted
     * @return granted or not
     */
    public boolean checkUsagePermissionGranted(){
        boolean granted = false;
        Context context = getApplicationContext();
        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
                //int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            //granted = (context.checkCallingOrSelfPermission( "android.permission.PACKAGE_USAGE_STATS") == PackageManager.PERMISSION_GRANTED);
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }


}
