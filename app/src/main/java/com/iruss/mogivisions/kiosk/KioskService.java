package com.iruss.mogivisions.kiosk;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iruss.mogivisions.procrastimatev1.HomeActivity;
import com.iruss.mogivisions.procrastimatev1.R;
import com.iruss.mogivisions.procrastimatev1.TriviaAPI;
import com.iruss.mogivisions.procrastimatev1.TriviaQuestion;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import model.Deck;
import model.DeckCollection;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class KioskService extends Service  {

    //contains the ads
    private AdView mAdView;

    public final String ANDROID_CHANNEL_ID = "KioskService";

    private WindowManager mWindowManager;
    WindowManager.LayoutParams mWindowsParams;
    private View mView;
    private TextView timeView;

    private int JOB_ID = 200260;



    public static final String BROADCAST_ACTION = "com.iruss.mogivisions.broadcastreceiver";


    private SharedPreferences mprefs;

    private boolean isBroadCastRegistered = false;

    // For trivia
    private ArrayList<TriviaQuestion> triviaQuestions;

    private TriviaQuestion triviaQuestion;

    private Button unlock;

    private TextView questionView;
    private TextView questionSubView;
    //Response buttons with questions
    private Button questionResponse1;
    private Button questionResponse2;
    private Button questionResponse3;
    private Button questionResponse4;

    private TextView deckTitle;
    private Button submit;
    private Deck deck;
    private TextToSpeech tts;
    private TextView correctAnswer;
    private EditText editText;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();



    //Number of trials possible
    private static final int trials = 4 ;
    private static final int flashcardTrials = 30;

    private TextView trialsView;

    private TextView successView;

    //Number of trials remaining
    private int attemptsMade = 0;

    // This is terrible architecture, but since it is mostly used to ask for permission,
    // anything else was way more complicated. PG
    public static HomeActivity homeActivity;

    //Correct button
    private Button correctButton;


    //Number of successes
    private int success = 0;


    //Display of success
    private RatingBar successBar;
    private TextView successBarFlashcards;


    //Display of trials
    private RatingBar trialBar;
    private TextView trialBarFlashcards;

    // Whether waiting for launcher to launch phone or camera
    private boolean waitingForLauncher = false;
    // For phone calls
    private SimplePhoneStateListener phoneStateListener = new SimplePhoneStateListener();

    //Trivia style chosen
    private String trivaStyle = "";


    // Constants
    private final String[] ACCEPTABLE_PACKAGES = {"dialer", "camera", "contacts", "incallui"};
    private final String LAUNCHER = "launcher";
    private final int LAUNCHER_DELAY = 3000; // msec


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("KioskMode restarted")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);

        }*/

        //initChannels(this);
        startForeground(this);



        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        mprefs = this.getSharedPreferences("Timer", Context.MODE_PRIVATE);

        //setBroadCastStatus(false, "startCommand: false");
        loadKiosk();


        // Start listening for phone calls
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        trivaStyle = sharedPref.getString("trivia_style", "Trivia");

        Log.i("Trivia Style", trivaStyle);
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {

        if (mView != null) {
            mWindowManager.removeView(mView);
        }

        // Stop listening for phone calls
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        //if(isBroadCastRegistered){

        try{
            setBroadCastStatus(false, "onDestroy(): false");
            unregisterReceiver(br);
        }catch (IllegalArgumentException e){
            Log.e("KioskStatus", e.toString());
            Log.i("KioskStatus", "KioskService is destroyed");
            super.onDestroy();
        }
        //}
        Log.i("KioskStatus", "KioskService is destroyed");
        super.onDestroy();
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
            //if(isConnected && homeActivity != null){
            if(isConnected){
                Log.d("Network", "Network connection available");
                //Loading unique ad id
                MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
                //Test id

                //MobileAds.initialize(homeActivity, "ca-app-pub-3940256099942544~3347511713");
                //Real id
                //MobileAds.initialize(getApplicationContext(), "ca-app-pub-5475955576463045~8715927181");


                //displaying the ads
                mAdView = mView.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }





    private void startForeground(Context context) {
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(context);

        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(101, notification);
    }


    private String createNotificationChannel(Context context){
        String channelId = "KioskService";
        String channelName = "My Background Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            //NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            //chan.lightColor = Color.BLUE;
            //chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE;
            NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
        }
        return channelId;
    }


    private void hideStatusBar(){
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        mView.setSystemUiVisibility(uiOptions);

    }

    /**
     * Makes sure the app is on top of other apps
     */
    private void displayView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.7f);
        int height = (int) (metrics.heightPixels * 0.45f);

        //Allows the app to be at the top
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mWindowsParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Not displaying keyboard on bg activity's EditText
                    //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //Not work with EditText on keyboard
                    PixelFormat.TRANSLUCENT);
        }  else{
            mWindowsParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.TYPE_PHONE,

                    //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Not displaying keyboard on bg activity's EditText
                    //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //Not work with EditText on keyboard
                    PixelFormat.TRANSLUCENT);
        }



        mWindowsParams.gravity = Gravity.TOP | Gravity.LEFT;
        //params.x = 0;
        mWindowsParams.y = 100;
        mWindowManager.addView(mView, mWindowsParams);

        mView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            long startTime = System.currentTimeMillis();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (System.currentTimeMillis() - startTime <= 300) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = mWindowsParams.x;
                        initialY = mWindowsParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mWindowsParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        mWindowsParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mView, mWindowsParams);
                        break;
                }
                return false;
            }
        });
    }




    // Load the kiosk fragment
    public void loadKiosk() {
        Log.i("SetBroadCastAttempt", "true");
        // Remove existing
        if (mView != null) {
            mWindowManager.removeViewImmediate(mView);
        }


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.fragment_kiosk, null);

        timeView = mView.findViewById(R.id.timeView);

        //setKioskTextSize();


        unlockPhone();
        response();
        call();
        camera();
        loadAds();
        hideStatusBar();
        // Now that it's loaded, display it
        displayView();
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_LONG).show();

        /*
        Toast.makeText(homeActivity.getApplicationContext(),
                message,
                Toast.LENGTH_LONG).show();*/
    }


    /**
     * changes the text size of a button
     * Reads the text size from the settings page
     */
    public void setKioskTextSize(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        //Convert string to a float value
        float textSize = Float.parseFloat(syncConnPref);
        timeView.setTextSize(textSize);
    }


    /**
     * changes the text size of a button
     * Reads the text size from the settings page
     * @param button, button to change the text size off
     */
    public void setKioskButtonTextSize(Button button){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        //Convert string to a float value
        float textSize = Float.parseFloat(syncConnPref);
        button.setTextSize(textSize);
    }


    /**
     * Carries the timer for how long the phone is locked
     * Reads settings and changes how long the phone will be locked for
     *
     */
    private void unlockPhone() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String syncConnPref = sharedPref.getString("lockout_time", "1");
        //Log.d("Settings", syncConnPref );
        //converts an hours into seconds

        String timePick = sharedPref.getString("time_picker", "00:30");
        Log.d("Settings", timePick);
        String[] timeSec = timePick.split(":");
        int timeLock = (Integer.parseInt(timeSec[0]) * 3600) + (Integer.parseInt(timeSec[1]) * 60);
        Log.d("Settings", Integer.toString(timeLock));


        //Starts new clock that updates automatically KioskPage is reloaded


        //Start the background timer

        mprefs = this.getSharedPreferences("Timer", Context.MODE_PRIVATE);
        Boolean isRegistered = mprefs.getBoolean("isBroadCastRegistered", false);

        if (!isRegistered) {

            //if the api is above then API 28, then we will
            //use a different method for calling the
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                callLifeLongBroadCast("lockTime", timeLock);
                registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));

            } else {
                startService(new Intent(this, BroadcastService.class).putExtra("lockTime", timeLock));
                registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
            }


            Log.i("isBroadCastRegistered", Boolean.toString(isRegistered));
            setBroadCastStatus(true, "unlockPhone(): true");

        /*
        startService(new Intent(this, BroadcastService.class).putExtra("lockTime", timeLock));
        registerReceiver(br, new IntentFilter(BroadcastService.COUNTDOWN_BR));
        Log.i("BroadCastRegistered", "BroadCast has been registered");
        */
            isBroadCastRegistered = true;
        }
    }

    public void callLifeLongBroadCast(String extraID, int extraValue) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            //Create the job of calling the usage limit analyzer
            ComponentName serviceName = new ComponentName(this, LongLifeBroadCastService.class);
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
                Log.i("KioskService", "Notification Job scheduled successfully");
            }

            //start the usage limit analyzer
            Intent service = new Intent(this, LongLifeBroadCastService.class).putExtra(extraID, extraValue);
            startService(service);
        }
    }

    public void setBroadCastStatus(boolean status, String origin){
        final SharedPreferences.Editor editor = mprefs.edit();
        Log.i("SetBroadCastRegistered", origin);
        editor.putBoolean("isBroadCastRegistered", status).apply();
    }


    /**
     * Receiver for TimeService, updates time clock when receives message from BroadcastService
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };



    /**
     * Updates the clock, according to message from BroadcastService
     * @param intent, the intent that was used to call KioskService
     */
    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            String millisUntilFinished = intent.getStringExtra("countdown");
            Log.i("KioskStatus", "Countdown seconds remaining: " +  millisUntilFinished);
            Log.i("BroadCastRegistered", "Hello Bitch");

            //Checks to make sure remaining time is not zero before updating timeView
            if(!millisUntilFinished.trim().equalsIgnoreCase("No more time remaining".trim())){
                timeView.setText(millisUntilFinished);
                showViewIfNecessary();
            }else{
                timeView.setText(millisUntilFinished);
                //stopService(new Intent(this, BroadcastService.class));
                Log.i("KioskStatus", "Stopped service");
                unLock();
            }


        }
    }



    /**
     *
     * Exit way for when timer runs out
     * Button is revealed for unlocking Kiosk Mode
     */
    private void unLock(){
        setBroadCastStatus(false, "unlock(): false");

        Button hiddenExit = mView.findViewById(R.id.exitButton);
        hiddenExit.setVisibility(View.VISIBLE);
        unlock.setVisibility(View.GONE);

        TextView unlockText = mView.findViewById(R.id.kioskView);
        unlockText.setText("PHONE IS UNLOCKED");
        hiddenExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Break out!
                stopSelf(-1);
                // Take out of KioskMode
                homeActivity.setShouldBeInKioskMode(false);
                //Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                //startActivity(intent);
            }
        });


        setKioskButtonTextSize(hiddenExit);
        //Unregisters time broadcast receiver
        unregisterReceiver(br);
        stopService(new Intent(this, BroadcastService.class));


    }



    /*******************************
     * Handle background apps
     */

    // Checks if you have an unauthorized app on top, and then
    public void showViewIfNecessary() {
        // If not created or already visible, don't need to do anything
        if (mView == null || mView.getVisibility() == View.VISIBLE) {
            return;
        }

        // Same for if you are just waiting for the launcher timer
        if (waitingForLauncher) {
            return;
        }
        // Get foreground package
        String foregroundPackage = getForegroundTask();

        // Handle special case for launcher, where it takes about 3 seconds for the phone or camera to appear
        if (foregroundPackage.toLowerCase().contains(LAUNCHER)) {
            waitingForLauncher = true;
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            waitingForLauncher = false;
                            Log.i("KioskService", "Timer fired");
                            String foregroundPackage = getForegroundTask();
                            if(!isForegroundPackageAcceptable(foregroundPackage)){
                                // Change visibility in the UI thread
                                /*
                                mView.getHandler().post(new Runnable() {
                                    public void run() {

                                        mView.setVisibility(View.VISIBLE);
                                    }
                                });*/
                                //updated for API 28+
                                mView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    },
                    LAUNCHER_DELAY
            );
            return;
        }

        // If top activity is not this, phone, or dialer, then make visible
        if(!isForegroundPackageAcceptable(foregroundPackage)){
            mView.setVisibility(View.VISIBLE);
        }

    }

    // Returns whether the foreground package is valid or not
    private boolean isForegroundPackageAcceptable(String foregroundPackage) {
        Log.i("Package on top", foregroundPackage);
        // Acceptable if it's this app
        if (foregroundPackage.equals(getApplicationContext().getPackageName())) {
            return true;
        }

        if(foregroundPackage.equalsIgnoreCase("NULL")){
            //Notify user why they can't use their phone or camera app

            Toast.makeText(getApplicationContext(),
                    "Can't access phone or camera app because usage statistics permission have not been granted," +
                            "which are necessary for keeping phone locked",
                    Toast.LENGTH_LONG).show();
            /*
            Toast.makeText(homeActivity.getApplicationContext(),
                    "Can't access phone or camera app because usage statistics permission have not been granted," +
                            "which are necessary for keeping phone locked",
                    Toast.LENGTH_LONG).show();*/
        }
        for (String acceptablePackage : ACCEPTABLE_PACKAGES) {
            if (foregroundPackage.toLowerCase().contains(acceptablePackage)) {
                return true;
            }
        }
        return false;
    }

    // Gets the foreground task. From https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag


    private String getForegroundTask() {
        String currentApp = "NULL";

        //If permission for Usage statistictics is not granted, then getForegroundTask will return "NULL"
        try {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
            } else {
                ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
                currentApp = tasks.get(0).processName;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

//        Log.d("KioskService", "Current App in foreground is: " + currentApp);
        return currentApp;
    }


    //Pulls up the in callui when there is an coming call
    public void callui(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED){
                Log.i("KioskService", "TelecomManger: showing InCall screen");
                TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                tm.showInCallScreen(false);
            }
        }
    }


    // Class that listens to the phone state
    class SimplePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i("KioskService", "onCallStateChanged: CALL_STATE_IDLE");
                    mView.setVisibility(View.VISIBLE);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i("KioskService", "onCallStateChanged: CALL_STATE_RINGING");
                    // Hide window
                    mView.setVisibility(View.GONE);
                    callui();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i("KioskService", "onCallStateChanged: CALL_STATE_OFFHOOK");
                    // Hide window
                    mView.setVisibility(View.GONE);
                    break;
                default:
                    Log.i("KioskService", "UNKNOWN_STATE: " + state);
                    break;
            }
        }
    }


    /********************************************************************************
     * Extra features of Kiosk Mode: Camera/Phone
     */


    /*
     * For the necessary features of the phone,
     * we need a phone button, that opens the phone for emergency calls
     * we need a camera button, that opens the camera app
     */

    /**.
     *When the phone button is pressed the user will able to do emergency calls
     */
    private void call(){
        ImageButton callApp = mView.findViewById(R.id.phone);
        callApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make view invisible
                if (mView != null) {
                    mView.setVisibility(View.GONE);
                }

                Uri number = Uri.parse("tel:5551234");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                //makes sure the phone is at the top of the activity stack
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(callIntent);
            }
        });
        //setKioskButtonTextSize(callApp);
    }


    /**
     * camera() calls  the camera App when the User press the camera button
     */

    private void camera(){
        ImageButton cameraApp = mView.findViewById(R.id.camera);
        cameraApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (cameraCheck()) {
                //if (homeActivity.cameraCheck()) {


                // Make view invisible
                if (mView != null) {
                    mView.setVisibility(View.GONE);
                }

                Intent cameraIntent = new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);


                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //makes sure the camera is at the top of the activity stack
                //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT);
                cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(cameraIntent);
                //}
            }
        });
        //setKioskButtonTextSize(cameraApp);
    }




    /*********************************************************************
     * Load Trivia Challenges
     */


    /**
     * Need a button for going to the Challenge_Activity
     * Reponse activated when user presses unlock phone button
     */
    public void response(){
        if (mView != null) {
            unlock = mView.findViewById(R.id.unlockPhone);

            unlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Code here executes on main thread after user presses buttont
                    if(trivaStyle.equalsIgnoreCase("Trivia".trim())){
                        loadTrivia();
                    }
                    else {
                        loadFlashCard();
                    }


                }
            });
            setKioskButtonTextSize(unlock);
        }

    }

    /****************************************************************************************************
     * Flashcards Challenge
     *
     */
    //FlashCards Section
    /**
     * Loads the flashcard fragment
     */
    public void loadFlashCard(){
        // Remove existing
        if (mView != null) {
            mWindowManager.removeViewImmediate(mView);
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.fragment_flashcard, null);

        // Set up UI
        questionView = mView.findViewById(R.id.questionText);
        correctAnswer = mView.findViewById(R.id.correctAnswer);
        questionSubView = mView.findViewById(R.id.questionSubText);


        submit = mView.findViewById(R.id.submit);
        editText = mView.findViewById(R.id.answerbox);
        deckTitle = mView.findViewById(R.id.deckName);


        //Gets the text that shows how many trials you have
        trialsView = mView.findViewById(R.id.trialsRemaining);

        //Gets the Successful Attempts text
        successView = mView.findViewById(R.id.successes);

        trialBarFlashcards = mView.findViewById(R.id.trialBar);
        trialBarFlashcards.setText(Integer.toString(flashcardTrials));

        SharedPreferences mprefs = this.getSharedPreferences("Flashcard", Context.MODE_PRIVATE);

        String name = mprefs.getString("DeckName", "Nothing");
        Log.i("Deck", name);
        if(name.trim().equalsIgnoreCase("Nothing")){
            reloadRandomDeck(name);
        }else{
            reloadDeck(name);
        }

        /*
        try {
            DeckCollection deckCollectionTemp = new DeckCollection();
            deckCollectionTemp.reload(provideStackSRSDir());
            Log.i("Deck collection", Integer.toString(deckCollectionTemp.getDeckInfos().size()));
        } catch (IOException e){
            e.printStackTrace();
        }*/

        setFlashcardTextSize();

        studyingIsOn();
        // Now that it's loaded, display it
        //loadAds();

        displayView();
        hideStatusBar();
    }

    private File provideStackSRSDir(){
        // if there is (possibly emulated) external storage available, we use it
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return getApplicationContext().getExternalFilesDir(null);
        } else { // otherwise we use an internal directory without access from the outside
            return getApplicationContext().getDir("StackSRS", MODE_PRIVATE);
        }
    }

    public void studyingIsOn(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(editText.getText().toString());
            }
        });

    }

    /**
     * Sets the text size of the trivia page when text size is changed in settings
     */
    public void setFlashcardTextSize(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        float textSize = Float.parseFloat(syncConnPref);
        //trialsView.setTextSize(textSize);
        //successView.setTextSize(textSize);
        questionView.setTextSize(textSize);
        questionSubView.setTextSize(textSize);
        submit.setTextSize(textSize);
        deckTitle.setTextSize(textSize);

    }

    /**
     * Checks the answer given from the flashcards
     * @param response, the users response to a flashcard
     */
    public void checkAnswer(String response){
        if(response.trim().equalsIgnoreCase(correctAnswer.toString())){
            Log.d("Test", "Correct response chosen");
            success += 1;
            successBarFlashcards.setText(Integer.toString(success));

            editText.setBackgroundColor(Color.GREEN);
            Log.i("Checkup", Integer.toString(success));
            Log.i("Checkup", Integer.toString(attemptsMade));

            /*
            if(success >= 10) {
                Toast.makeText(getApplicationContext(),
                        "Flashcard challenge solved successfully, phone is unlocked for 5 mins",
                        Toast.LENGTH_LONG).show();
                /*
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(homeActivity.getApplicationContext(),
                                "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                                Toast.LENGTH_LONG).show();
                    }
                });*/
            /*
                deck.saveDeck();
                temporaryUnlock();
            }else{
                continueStudying();
            }*/

            continueStudying();

    }
            else {
            Log.i("Checkup", Integer.toString(success));
            Log.i("Checkup", Integer.toString(attemptsMade));

            //Code that shows the correct answer
            Log.d("Test", "Incorrect response chosen");
            editText.setBackgroundResource(R.drawable.rounded_button_red);
            correctAnswer.setVisibility(View.VISIBLE);

            /*
            if(attemptsMade >= flashcardsTrials){
                //call kill
                success = 0;
                attemptsMade = 0;
                //display a message to user that they are out of attempts and go back to KioskActivity
                Log.d("Test", "You are out of attempts");

                Toast.makeText(getApplicationContext(),
                        "You failed to solve at least 10 deck cards, phone will not be unlocked",
                        Toast.LENGTH_LONG).show();
                /*
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(homeActivity.getApplicationContext(),
                                "You failed to solve the trivia challenge, phone will not be unlocked",
                                Toast.LENGTH_LONG).show();
                    }
                });*/
            /*
                deck.saveDeck();
                loadKiosk();
            }else{
                //continues the trivia but with a delay
                continueStudying();
            }*/

            continueStudying();

        }

        finishedFlashcards();
    }

    /**
     * Once the flashcards are done, checks to see if user has earned right to unlock their phone
     */
    public void finishedFlashcards(){

        if(attemptsMade >= flashcardTrials){
            if(success >= 10) {
                Toast.makeText(getApplicationContext(),
                        "Flashcard challenge solved successfully, phone is unlocked for 5 mins",
                        Toast.LENGTH_LONG).show();
                /*
                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(homeActivity.getApplicationContext(),
                                "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                                Toast.LENGTH_LONG).show();
                    }
                });*/
                deck.saveDeck();
                temporaryUnlock();
            }
            else {
                //call kill
                success = 0;
                attemptsMade = 0;
                //display a message to user that they are out of attempts and go back to KioskActivity
                Log.d("Test", "You are out of attempts");

                Toast.makeText(getApplicationContext(),
                        "You failed to solve at least 10 deck cards, phone will not be unlocked",
                        Toast.LENGTH_LONG).show();
                deck.saveDeck();
                loadKiosk();
            }
        }
    }


    public void continueStudying(){
        Handler myhandler = new Handler();
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Works on displaing the next set of questions
                attemptsMade += 1;
                int displayTrials = flashcardTrials - attemptsMade;
                trialBarFlashcards.setText(Integer.toString(displayTrials));
                //String trialsStr = "Trials remaining: " + Integer.toString(displayTrials) + "    Successes (need 3 to unlock): " + Integer.toString(success);
                //changes how many trials there are left
                //trialsView.setText(trialsStr);
                deck.putReviewedCardBack(true);

                questionView.setText(deck.getNextCardToReview().getFront());
                correctAnswer.setText(deck.getNextCardToReview().getBack());

                 resetFlashcardsButton();

            }
        }, 1000);
    }

    public void resetFlashcardsButton(){
        correctAnswer.setVisibility(View.GONE);
        editText.setBackgroundResource(R.drawable.rounded_button_grey);
    }


    private void reloadDeck(String deckName){
        try {
            DeckCollection deckCollectionTemp = new DeckCollection();
            deckCollectionTemp.reload(provideStackSRSDir());
            Log.i("Deck Chosen", deckName);

            File file = new File(provideStackSRSDir() + "/" + deckName + ".json");
            deckTitle.setText("DECK: " + deckName);
            deck = gson.fromJson(FileUtils.readFileToString(file, Charset.forName("UTF-8")), Deck.class);


            //deck = Deck.loadDeck(deckName);
            if(!deck.isUsingTTS() && !deck.getLanguage().equals("") && deck.isNew())
                askForTTSActivation();
            if(deck.isUsingTTS())
                initTTS();
            //showNextCard();
            questionView.setText(deck.getNextCardToReview().getFront());
            correctAnswer.setText(deck.getNextCardToReview().getBack());
        } catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_LONG).show();
        } catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_LONG).show();
        }
    }



    private void reloadRandomDeck(String deckName){
        try {
            DeckCollection deckCollectionTemp = new DeckCollection();
            deckCollectionTemp.reload(provideStackSRSDir());
            Log.i("Deck collection", Integer.toString(deckCollectionTemp.getDeckInfos().size()));

            boolean noEligbleDeck = true;
            Random rand = new Random();
            /*
            int position = rand.nextInt(deckCollectionTemp.getDeckInfos().size());
            Log.i("Deck position", Integer.toString(position));
            String name = deckCollectionTemp.getDeckInfos().get(position).getName();
            File file = new File(provideStackSRSDir() + "/" + name + ".json");
            deckTitle.setText("DECK: " + name);
            deck = gson.fromJson(FileUtils.readFileToString(file, Charset.forName("UTF-8")), Deck.class);*/
            while(noEligbleDeck){

                Log.i("Deck selection", "Still looking for a suitable deck");
                int position = rand.nextInt(deckCollectionTemp.getDeckInfos().size());
                String name = deckCollectionTemp.getDeckInfos().get(position).getName();
                Log.i("Deck name", name);

                File file = new File(provideStackSRSDir() + "/" + name + ".json");
                deckTitle.setText("DECK: " + name);
                deck = gson.fromJson(FileUtils.readFileToString(file, Charset.forName("UTF-8")), Deck.class);

                if(deck.getDeckStackSize() >= 30){
                    noEligbleDeck = false;
                }

            }

            questionView.setText(deck.getNextCardToReview().getFront());
            correctAnswer.setText(deck.getNextCardToReview().getBack());
        } catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e){
            this.loadKiosk();
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.flashcards_could_not_be_loaded, Toast.LENGTH_LONG).show();
        }
    }

    private void askForTTSActivation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.activate_tts_new_deck));
        builder.setMessage(getString(R.string.want_activate_tts));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deck.activateTTS();
                initTTS();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        //alert.show();
    }

    private void initTTS(){
        final Locale locale = getLocaleForTTS();
        if(locale != null){
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(locale);
                    }
                }
            });
        }
    }

    private Locale getLocaleForTTS(){
        String lang = deck.getLanguage();
        if(lang == null || lang.equals(""))
            return null;
        String country = deck.getAccent();
        if(country == null || country.equals(""))
            return new Locale(lang);
        return new Locale(lang, country);
    }


    /***********************************************************************************************
     * Trivia challenge
     */

    /**
     * Loads the trivia fragment
     */
    public void loadTrivia() {
        // Remove existing
        if (mView != null) {
            mWindowManager.removeViewImmediate(mView);
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.fragment_trivia, null);

        // Set up UI
        questionView = mView.findViewById(R.id.questionText);
        //Response buttons with questions
        questionResponse1 = mView.findViewById(R.id.questionResponse1);
        questionResponse2 = mView.findViewById(R.id.questionResponse2);
        questionResponse3 = mView.findViewById(R.id.questionResponse3);
        questionResponse4 = mView.findViewById(R.id.questionResponse4);


        //make the buttons not visible until the buttons are ready
        questionView.setVisibility(View.GONE);
        questionResponse1.setVisibility(View.GONE);
        questionResponse2.setVisibility(View.GONE);
        questionResponse3.setVisibility(View.GONE);
        questionResponse4.setVisibility(View.GONE);

        //Gets the text that shows how many trials you have
        trialsView = mView.findViewById(R.id.trialsRemaining);
        //String trialsStr = "Trials remaining: " + Integer.toString(trials) ;
        //trialsView.setText(trialsStr);

        //Gets the Successful Attempts text
        successView = mView.findViewById(R.id.successes);

        trialBar = mView.findViewById(R.id.trialBar);
        trialBar.setRating(trials);

        setTriviaTextSize();
        //call the trivia Api
        //TriviaAPI triviaAPI = new TriviaAPI(this);
        //try{
        new TriviaAPI(this);

            /*
        }
        catch (Exception e){
            loadKiosk();
            homeActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(homeActivity.getApplicationContext(),
                            "Not able to load trivia because of poor internet connection, try again later",
                            Toast.LENGTH_LONG).show();
                }
            });
        }*/

        // Now that it's loaded, display it
        //loadAds();

        displayView();
        hideStatusBar();
    }


    /**
     * Sets the text size of the trivia page when text size is changed in settings
     */
    public void setTriviaTextSize(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        float textSize = Float.parseFloat(syncConnPref);
        //trialsView.setTextSize(textSize);
        //successView.setTextSize(textSize);
        questionView.setTextSize(textSize);
        questionResponse1.setTextSize(textSize);
        questionResponse2.setTextSize(textSize);
        questionResponse3.setTextSize(textSize);
        questionResponse4.setTextSize(textSize);

    }

    /*
     * A method to create the challenge
     * display the question and possible resonses in the TriviaActivity
     */

    // TODO: Make a test for this method
    public boolean displayQuestions(ArrayList<TriviaQuestion> triviaQuestions){


        this.triviaQuestions = triviaQuestions;
        //Randomly select question
        Random randomizer = new Random();
        //randomly gets the next question
        triviaQuestion = triviaQuestions.get(randomizer.nextInt(triviaQuestions.size()));

        //settings the question to be displayed
        questionView.setText(triviaQuestion.getQuestion());
        questionView.setVisibility(View.VISIBLE);
        ArrayList<String> responses = new ArrayList<>();
        responses.addAll(triviaQuestion.getIncorrectAnswers());

        //randomly adding the correct answer into the list of possible answers
        int randomposition = randomizer.nextInt(responses.size() + 1);
        Log.i("Array Size", Integer.toString(responses.size()));
        Log.d("Test", "Correct response chosen" + randomposition);
        responses.add(randomposition, triviaQuestion.getCorrectAnswer());
        Log.i("Array Size", Integer.toString(responses.size()));


        //resetbuttons text and color
        resetButtons();

        //always sets the responses for the first 2 buttons
        questionResponse1.setText(responses.get(0));
        questionResponse2.setText(responses.get(1));

        int size = responses.size();

        //Make sure only 2 response appear for True and False questions
        if(triviaQuestion.getCorrectAnswer().equalsIgnoreCase("true")
            || triviaQuestion.getCorrectAnswer().equalsIgnoreCase("false")){
            size = 2;
        }
        //Sets the texts of the button to the possible responses for the user to choose
        //Checks how many possible responses there are and then displays the same amount of response buttons
        if(size == 2 ){
            questionResponse1.setVisibility(View.VISIBLE);
            questionResponse2.setVisibility(View.VISIBLE);
        }else if (size == 3){
            questionResponse3.setText(responses.get(2));
            questionResponse3.setVisibility(View.VISIBLE);
        }else if (size == 4) {
            questionResponse3.setText(responses.get(2));
            questionResponse4.setText(responses.get(3));
            questionResponse1.setVisibility(View.VISIBLE);
            questionResponse2.setVisibility(View.VISIBLE);
            questionResponse3.setVisibility(View.VISIBLE);
            questionResponse4.setVisibility(View.VISIBLE);

        }

        //Sets the correct solution
        setCorrectButton(randomposition);

        //sets the Clicklistener for all the buttons
        questionResponse1.setOnClickListener(myClickListener);
        questionResponse2.setOnClickListener(myClickListener);
        questionResponse3.setOnClickListener(myClickListener);
        questionResponse4.setOnClickListener(myClickListener);



        return true;
    }

    /**
     * Sets the correct button
     * @param randomposition, randomposition is the position of the correct answer for a question
     */
    public void setCorrectButton(int randomposition){
        //Sets what will the correct answer
        switch(randomposition){
            case 0:
                correctButton = questionResponse1;
                break;
            case 1:
                correctButton = questionResponse2;
                break;
            case 2:
                correctButton = questionResponse3;
                break;
            case 3:
                correctButton = questionResponse4;
                break;
            default:
                correctButton = new Button(getApplicationContext());
                //correctButton = new Button(homeActivity.getApplicationContext());
                Log.e("Error", "Not an available buttton");
                break;
        }
    }



    /*
     * A method to create the challenge
     * display the question and possible resonses in the TriviaActivity
     */
    // TODO: Make a test for this method
    private final View.OnClickListener myClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            successBar = mView.findViewById(R.id.successBar);
            //trialBar = (RatingBar) mView.findViewById(R.id.trialBar);
            //trialBar.setRating(trials);



            Button tempButton = mView.findViewById(view.getId());

            //Checks to see if the answer the user is the correct one
            if(tempButton.getText().equals(triviaQuestion.getCorrectAnswer())){
                Log.d("Test", "Correct response chosen");
                tempButton.setBackgroundResource(R.drawable.rounded_button_green);
                success += 1;
                successBar.setRating(success);
                Log.i("Checkup", Integer.toString(success));
                Log.i("Checkup", Integer.toString(attemptsMade));
                if(success == 3) {
                    Toast.makeText(getApplicationContext(),
                            "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                            Toast.LENGTH_LONG).show();
                    /*
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(homeActivity.getApplicationContext(),
                                    "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                                    Toast.LENGTH_LONG).show();
                        }
                    });*/
                    temporaryUnlock();
                }else{
                    continueTrivia();
                }

            }
            else {
                Log.i("Checkup", Integer.toString(success));
                Log.i("Checkup", Integer.toString(attemptsMade));

                //Code that shows the correct answer
                Log.d("Test", "This is the correct response ");
                correctButton.setBackgroundResource(R.drawable.rounded_button_green);
                Log.d("Test", "Incorrect response chosen");
                tempButton.setBackgroundResource(R.drawable.rounded_button_red);

                if(attemptsMade >= trials){
                    //call kill
                    success = 0;
                    attemptsMade = 0;
                    //display a message to user that they are out of attempts and go back to KioskActivity
                    Log.d("Test", "You are out of attempts");

                    Toast.makeText(getApplicationContext(),
                            "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                            Toast.LENGTH_LONG).show();
                    /*
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(homeActivity.getApplicationContext(),
                                    "You failed to solve the trivia challenge, phone will not be unlocked",
                                    Toast.LENGTH_LONG).show();
                        }
                    });*/
                    loadKiosk();
                }else{
                    //continues the trivia but with a delay
                    continueTrivia();
                }
            }
        }
    };


    /************************************************************************************************
     * Temporary Unlock is used by both the Trivia and Flashcards challenge
     */



    /**
     * Creates a temporary break for the user to use their phone after they have solved a challenge
     */
    private void temporaryUnlock(){
        stopSelf(-1);
        int lockbreak = 300000;
        // Take out of KioskMode
        homeActivity.setShouldBeInKioskMode(false);

        Handler myhandler = new Handler();

        //5 minute break
        //int lockbreak = 30000;
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                homeActivity.setShouldBeInKioskMode(true);
                //resumes the KioskService

                //Intent service = new Intent(homeActivity, KioskService.class);

                Intent service = new Intent(KioskService.this, KioskService.class);

                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //ComponentName service = new ComponentName(getApplicationContext(), KioskService.class);
                    //ContextCompat.startForegroundService(homeActivity, service);
                    //homeActivity.startForegroundService(service);
                    startForegroundService(service);
                } else{
                    stopService(service);
                    startService(service);
                }


                //Tells the user that their phone break is over

                Toast.makeText(getApplicationContext(),
                        "Your break is over, phone lock will continue",
                        Toast.LENGTH_LONG).show();
                /*
                Toast.makeText(homeActivity.getApplicationContext(),
                        "Your break is over, phone lock will continue",
                        Toast.LENGTH_LONG).show();*/


            }
        }, lockbreak);
    }


    /**
     * Calls for another trivia question
     */
    private void continueTrivia(){
        Handler myhandler = new Handler();
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Works on displaing the next set of questions
                attemptsMade += 1;
                int displayTrials = trials - attemptsMade;
                trialBar.setRating(displayTrials);
                //String trialsStr = "Trials remaining: " + Integer.toString(displayTrials) + "    Successes (need 3 to unlock): " + Integer.toString(success);
                //changes how many trials there are left
                //trialsView.setText(trialsStr);
                displayQuestions(triviaQuestions);
            }
        }, 1000);
    }



    /**
     * Resets the buttons colors back to default after they have been changed
     */
    private void resetButtons(){
        questionResponse1.setBackgroundResource(R.drawable.rounded_button_grey);
        questionResponse2.setBackgroundResource(R.drawable.rounded_button_grey);
        questionResponse3.setBackgroundResource(R.drawable.rounded_button_grey);
        questionResponse4.setBackgroundResource(R.drawable.rounded_button_grey);

        //Reset the text of the button to make sure True or False questions only have 2 responses
        questionResponse1.setVisibility(View.GONE);
        questionResponse2.setVisibility(View.GONE);
        questionResponse3.setVisibility(View.GONE);
        questionResponse4.setVisibility(View.GONE);
    }

}