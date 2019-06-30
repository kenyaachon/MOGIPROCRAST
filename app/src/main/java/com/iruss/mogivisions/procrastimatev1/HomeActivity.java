package com.iruss.mogivisions.procrastimatev1;

//Copyright (c) 2017,2018   Mathijs Lagerberg, Pixplicity BV
//Copyright (C) 2016-2018   Samuel Wall

//

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.kiosk.KioskService;
import com.iruss.mogivisions.statistics.StatisticsActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pixplicity.generate.OnFeedbackListener;
import com.pixplicity.generate.Rate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import model.Card;
import model.Deck;
import model.DeckCollection;
import model.DownloadableDeckInfo;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import view.DeckListActivity;

public class HomeActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    //Ads
    private AdView mAdView;

    private boolean shouldBeInKioskMode = false;
    public final static int Overlay_REQUEST_CODE = 251;

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private final int MY_PERMISSIONS_REQUEST_READ_USAGE_STATISTICS = 2;

    private final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;

    // For notifications
    NotificationCompat.Builder mBuilder;
    private String CHANNEL_ID = "PROCRASTIMATE_CHANNEL";
    private int NOTIFICATION_ID = 2001; // Just some unique ID

    //Storing settings that are crucial for use
    private SharedPreferences mprefs;

    private SharedPreferences.Editor editor;


    //Download extra decks
    private final String SERVER_URL = "https://stacksrs.droppages.com/";

    private List<DownloadableDeckInfo> deckNames = new ArrayList<>();

    private AsyncHttpClient httpClient = new AsyncHttpClient();


    public boolean isShouldBeInKioskMode() {
        return shouldBeInKioskMode;
    }

    public void setShouldBeInKioskMode(boolean shouldBeInKioskMode) {
        this.shouldBeInKioskMode = shouldBeInKioskMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_home);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.mytitle);

        mprefs = this.getSharedPreferences("Tutorial", Context.MODE_PRIVATE);


        shouldBeInKioskMode = false;

        initializeKiosk();
        initializeSettings();
        //sample AddMob Id
        /*
        //MobileAds.initialize(this, "ca-app-pub-5475955576463045~8715927181");
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
        loadAds();

        firstTimeUser();

        rateMe();

    }

        /**
         * Displays first time User
         */
    public void firstTimeUser(){

        editor = mprefs.edit();
        //make sure the firstTime preference is only created once
        if(mprefs.getBoolean("firstTime", true)) {
            Log.i("Tutorial", "Creating first time setting" );
            editor.putBoolean("firstTime", true).apply();
        }

        //Call Usage statistics
        userStats();
        Log.i("Tutorial", Boolean.toString(mprefs.getBoolean("firstTime", true)));

        if(mprefs.getBoolean("firstTime", true)){
            //turn off the tutorial after it is displayed
            editor.putBoolean("firstTime", false).apply();

            new MaterialTapTargetPrompt.Builder(HomeActivity.this)
                    .setTarget(R.id.settings)
                    .setBackgroundColour(getResources().getColor(R.color.titlebackgroundcolor))
                    .setFocalColour(getResources().getColor(R.color.tutorialFocalColor))
                    .setPrimaryText("Change how long phone is locked")
                    .setSecondaryText("Tap the menu gear to open the popup menu so you can go to settings")
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                // User has pressed the prompt target
                                Log.i("Tutorial", "User has pressed the prompt target: Settings");
                            }
                            //userStats();

                        }
                    })
                    .show();
            //downloadDeckList();
            downloadDeckListPreload();
        }

        /*
        if(networkCheck()){
            downloadDeckList();

        }*/
        downloadDeckListPreload();

        printDeckCollectionInfo();

    }



    /**
     * Creates a Snackbar that asks user to rate the app
     * Only popups aflter a couple days of use
     */
    public void rateMe(){
        final SharedPreferences.Editor editor = mprefs.edit();
        //make sure the firstTime preference is only created once
        if(mprefs.getBoolean("showDialog", true)) {
            Log.i("Tutorial", "Creating first time setting" );
            editor.putBoolean("showDialog", true).apply();
        }

        //Rating dialog
        Rate rate = new Rate.Builder(this)
                // Trigger dialog after this many events (optional, defaults to 6)
                .setMinimumInstallTime(0)
                .setRepeatCount(10)
                .setFeedbackAction(new OnFeedbackListener() {       // Optional
                    @Override
                    public void onFeedbackTapped() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("mailto:mogivisionsapp@gmail.com"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        HomeActivity.this.startActivity(intent);
                        /*
                        if (mRate.canOpenIntent(intent)) {
                            mRate.mContext.startActivity(intent);
                        }*/

                        editor.putBoolean("showDialog", false).apply();

                    }
                    @Override
                    public void onRateTapped() {
                        final Uri uri = Uri.parse(("market://details?id=" + HomeActivity.this.getPackageName()));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        HomeActivity.this.startActivity(intent);

                        editor.putBoolean("showDialog", false).apply();
                    }
                    @Override
                    public void onRequestDismissed(boolean dontAskAgain) {
                        // User has dismissed the request
                        if(dontAskAgain){
                            editor.putBoolean("showDialog", false).apply();
                        }
                    }
                })
                .setSnackBarParent((ViewGroup) ((ViewGroup) this
                        .findViewById(android.R.id.content)).getChildAt(0))
                .setPositiveButton("Sure!")                         // Optional
                .setCancelButton("Maybe later")                     // Optional
                .setNegativeButton("Give Feedback")                         // Optional
                .setSwipeToDismissVisible(true)                    // Add this when using the Snackbar
                // without a CoordinatorLayout as a parent.
                .build();

        // .setFeedbackAction(Uri.parse("mailto:mogivisionsapp@gmail.com"))
        Log.i("Rate Me", Long.toString(rate.getRemainingCount()));

        //Countsdown the timer
        rate.count();

        rate.showRequest();

        if(rate.getRemainingCount() == 0 && mprefs.getBoolean("showDialog", true)){
            rate.reset();
        }

        //rate.test();

    }



    /**
     * Loads the ads at the bottom of the page
     * Checks if there is internet if not then ads are not loaded
     */

    private void loadAds(){
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        //Checks if there is internet
        try {
            NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if(isConnected){
                Log.d("Network", "Network connect" +
                        "ion available");
                //Loading unique ad id
                //Test id
                MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
                //Real id
               // MobileAds.initialize(this, "ca-app-pub-5475955576463045~8715927181");


                //displaying the ads
                mAdView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    /**
     * Handles when the settings button is pressed
     * Calls the SettingsActivity
     */
    public void initializeSettings() {
        final ImageButton settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                //startActivity(intent);

                PopupMenu popup = new PopupMenu(HomeActivity.this, settingsButton);

                //Forces the menu icons to be visible
                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                HomeActivity.this,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        switch (item.getTitle().toString()) {
                            case "Settings":
                                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case "Statistics":
                                /*
                                Intent intent1 = new Intent(HomeActivity.this, StatisticsActivity.class);
                                startActivity(intent1);*/
                                getPermissionForStatsPage();
                                break;
                            case "Flashcards":
                                Intent intent1 = new Intent(HomeActivity.this, DeckListActivity.class);
                                startActivity(intent1);
                                break;

                            case "Share App":
                                Intent myIntent = new Intent(Intent.ACTION_SEND);
                                myIntent.setType("text/plain");
                                String sharebody = "Hey you should try out this app Procrasti Mate";
                                myIntent.putExtra(Intent.EXTRA_TEXT, sharebody);
                                startActivity(Intent.createChooser(myIntent, "Share using"));
                                break;
                            case "Feedback":
                                Intent feedbackintent = new Intent(Intent.ACTION_VIEW);
                                feedbackintent.setData(Uri.parse("mailto:mogivisionsapp@gmail.com"));
                                feedbackintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(feedbackintent);
                        }

                        return true;
                    }
                });


                popup.show();

            }
        });

        //setButtonTextSize(settingsButton);
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



                    //Weird error, if statements are going in reverse order
                    //Fixed by putting if statements for Phone and Camera permission after OverlayRequest
                    if (!Settings.canDrawOverlays(HomeActivity.this)) {
                        showExplanation(HomeActivity.this.getString(R.string.OverlayTitle), HomeActivity.this.getString(R.string.OverlayRequestRationale), Overlay_REQUEST_CODE);
                        okToStartKiosk = false;
                    }
                    if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.i("HomeActivity", "Phone");
                        showExplanation(HomeActivity.this.getString(R.string.PhonePermissionRequest), HomeActivity.this.getString(R.string.PhonePermissionRational), MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        okToStartKiosk = false;
                    }

                    if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.i("HomeActivity", "Camera");
                        showExplanation(HomeActivity.this.getString(R.string.CameraPermissionRequest), HomeActivity.this.getString(R.string.CameraPermissionRequestRationale), MY_PERMISSIONS_REQUEST_CAMERA);
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
    public void startKiosk() {
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
     * Permissions needed for App to work
     */



    /**
     * Requires permission granted to usage statistics in order to access statistics page
     */
    public void getPermissionForStatsPage(){
        if (!checkUsagePermissionGranted()) {
            showExplanation(this.getString(R.string.UsageStatisticsTitle), this.getString(R.string.UsageStatisticsPermissionRationalStats),
                    MY_PERMISSIONS_REQUEST_READ_USAGE_STATISTICS);
        }else{
            Intent intent1 = new Intent(HomeActivity.this, StatisticsActivity.class);
            startActivity(intent1);
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

        /*
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
        alertDialog.show();*/

        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
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
            granted = (context.checkCallingOrSelfPermission( "android.permission.PACKAGE_USAGE_STATS") == PackageManager.PERMISSION_GRANTED);
            //granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
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


    /*
     *Checks if there is an internet connection if there is it returns true
     * and false otherwise
     */
    private boolean networkCheck(){
        Log.d("attempt", "network attempt");


        ConnectivityManager conMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            try {
                if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                        || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                    // notify user you are online
                    Log.d("Network", "Network state connected");
                    return true;
                } else {

                    //false if network check fails
                    Log.d("Network", "No Network");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            //Returns network state for older API's
            try {
                NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(isConnected){
                    Log.d("Network", "Network connection available");
                }
                return isConnected;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //false if network check fails
        Log.d("Network", "No Network");
        return false;

    }


    /**
     * List of language decks are loaded when there is internet
     */
    public void downloadDeckList() {

        httpClient.get(SERVER_URL + "decks.txt", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                deckNames.clear();
                try {
                    JSONArray deckListArray = response.getJSONArray("decks");
                    for (int i = 0; i < deckListArray.length(); ++i) {
                        DownloadableDeckInfo deckInfo = new DownloadableDeckInfo(
                                deckListArray.getJSONObject(i ));
                        deckNames.add(deckInfo);
                        downloadDeck(deckInfo.getFile(), 2, deckInfo.getName());
                    }
                } catch (JSONException e) {
                    Log.i("DeckListActivity", Integer.toString(R.string.could_not_load_deck_list_from_server));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {

                Log.i("DeckListActivity", Integer.toString(R.string.could_not_connect_to_server));

            }
        });
    }

    /**
     * Preloaded Deck List
     */
    public void downloadDeckListPreload() {
        //Attaining what are the important files
        String[] decks = new String[]{"Essential_French.json", "Duolingo_German.json", "Duolingo_Swedish.json"};
        for( int j = 0; j < decks.length; j++){
            preloadDeck( 2, decks[j]);
        }
    }


    /**
     * Retrieving a specific level
     * @param level, how important the deck is
     */
    public void preloadDeck(final int level, final String filename) {


        try {
            //Attain the JSON object of the file
            JSONObject response = new JSONObject(loadJSONFromAsset(HomeActivity.this, filename));
            DownloadableDeckInfo deckInfo = new DownloadableDeckInfo(response);
            deckNames.add(deckInfo);


            //Loading the directory for where Deck
            DeckCollection deckCollection = new DeckCollection();
            deckCollection.reload(provideStackSRSDir());
            //Retrieving the name of the deck
            String deckName = response.getString("name");

            Deck newDeck = new Deck(deckName, response.getString("back"));

            //Getting the list of flashcards
            JSONArray cardArray = response.getJSONArray("cards");
            List<Card> cards = new ArrayList<>();
            //traversing the cardArray to get the contents of each cards
            for (int i = 0; i < cardArray.length(); ++i) {
                JSONObject cardObject = cardArray.getJSONObject(i);
                Card c = new Card(cardObject.getString("front"),
                        cardObject.getString("back"), level);
                cards.add(c);
            }
            //Adding the cards to our new deck
            newDeck.fillWithCards(cards);

            Log.i("DeckListActivity deck:", deckName);

        } catch (IOException e) {
            Log.e("DeckListActivity", "exception", e);
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * Retrieving a specific level
     * @param file, the file that has the deckInfo
     * @param level, how important the deck is
     */
    public void downloadDeck(final String file, final int level, final String filename) {


        try {
            JSONObject response = new JSONObject(loadJSONFromAsset(HomeActivity.this, filename));

            DeckCollection deckCollection = new DeckCollection();
            deckCollection.reload(provideStackSRSDir());
            String deckName = response.getString("name");
            Deck newDeck = new Deck(deckName, response.getString("back"));
            JSONArray cardArray = response.getJSONArray("cards");
            List<Card> cards = new ArrayList<>();
            //retrieving the cards from the JSON file
            for (int i = 0; i < cardArray.length(); ++i) {
                JSONObject cardObject = cardArray.getJSONObject(i);
                Card c = new Card(cardObject.getString("front"),
                        cardObject.getString("back"), level);
                cards.add(c);
            }
            newDeck.fillWithCards(cards);

            Log.i("DeckListActivity deck:", deckName);

        }catch (IOException e ) {
            Log.e("DeckListActivity", "exception", e);
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }


        /*
        //retrieving the online deck from online
        httpClient.get(SERVER_URL + file + ".txt", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    DeckCollection deckCollection = new DeckCollection();
                    deckCollection.reload(provideStackSRSDir());
                    String deckName = response.getString("name");
                    Deck newDeck = new Deck(deckName, response.getString("back"));
                    JSONArray cardArray = response.getJSONArray("cards");
                    List<Card> cards = new ArrayList<>();
                    //retrieving the cards from the JSON file
                    for(int i = 0; i < cardArray.length(); ++i) {
                        JSONObject cardObject = cardArray.getJSONObject(i);
                        Card c = new Card(cardObject.getString("front"),
                                cardObject.getString("back"), level);
                        cards.add(c);
                    }
                    newDeck.fillWithCards(cards);


                    Log.i("DeckListActivity deck:", deckName);
                } catch(JSONException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.could_not_load_deck),
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Toast.makeText(getApplicationContext(), getString(R.string.downloading_deck_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    /**
     * Prints important deck info to confirm that the preloaded decks have been created
     */
    public void printDeckCollectionInfo(){
        try{
            DeckCollection deckCollection = new DeckCollection();
            deckCollection.reload(provideStackSRSDir());

            Log.i("Deck collection directory", DeckCollection.stackSRSDir.getPath());
            Log.i("Deck Collection size", Integer.toString(deckCollection.getDeckInfos().size()));

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);

            int size = is.available();
            Log.i("Deck Size", Integer.toString(size));

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }


    /**
     * Identifies where the deck directory is
     * @return, a file that contains the directory of where the decks are stored
     */
    private File provideStackSRSDir(){
        // if there is (possibly emulated) external storage available, we use it
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return getApplicationContext().getExternalFilesDir(null);
        } else { // otherwise we use an internal directory without access from the outside
            return getApplicationContext().getDir("StackSRS", MODE_PRIVATE);
        }
    }




}

