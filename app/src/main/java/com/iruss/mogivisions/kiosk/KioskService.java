package com.iruss.mogivisions.kiosk;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.experiment.HomeActivity;
import com.iruss.mogivisions.experiment.R;
import com.iruss.mogivisions.experiment.TriviaAPI;
import com.iruss.mogivisions.experiment.TriviaQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class KioskService extends Service implements MyTimer.TimerRunning {

    //contains the ads
    private AdView mAdView;

    private WindowManager mWindowManager;
    WindowManager.LayoutParams mWindowsParams;
    private View mView;
    private TextView timeView;

    // For trivia
    private ArrayList<TriviaQuestion> triviaQuestions;

    private TriviaQuestion triviaQuestion;

    private TextView questionView;
    //Response buttons with questions
    private Button questionResponse1;
    private Button questionResponse2;
    private Button questionResponse3;
    private Button questionResponse4;


    //Button list for finding the button with the correct answer
    private ArrayList<Button> buttons = new ArrayList<>();

    //Number of trials possible
    private static final int trials = 2;

    private TextView trialsView;

    //Number of trials remaining
    private int attemptsMade = 0;

    // This is terrible architecture, but since it is mostly used to ask for permission,
    // anything else was way more complicated. PG
    public static HomeActivity homeActivity;

    //Correct button
    private Button correctButton;

    // Constants
    private final String CAMERA_PACKAGE = "com.android.camera";
    private final String DIALER_PACKAGE = "com.android.dialer";

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
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        loadKiosk();
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Sets the layout of the app to fullscreen
     */
    private void hideStatusBar(){
    // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        mView.setSystemUiVisibility(uiOptions);

    }


    @Override
    public void onDestroy() {

        if (mView != null) {
            mWindowManager.removeView(mView);
        }
        super.onDestroy();
    }

    /**
     * Makes sure the app is on top of other apps
     */
    private void displayView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.7f);
        int height = (int) (metrics.heightPixels * 0.45f);

        mWindowsParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.TYPE_PHONE,
                //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Not displaying keyboard on bg activity's EditText
                //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //Not work with EditText on keyboard
                PixelFormat.TRANSLUCENT);


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
        // Remove existing
        if (mView != null) {
            mWindowManager.removeViewImmediate(mView);
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.fragment_kiosk, null);

        timeView = mView.findViewById(R.id.timeView);
        MyTimer.getInstance().setTimerRuningListener(this);

        setKioskTextSize();


        unlockPhone();
        response();
        call();
        camera();
        loadAds();
        hideStatusBar();
        // Now that it's loaded, display it
        displayView();
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
     * @param button
     */
    public void setKioskButtonTextSize(Button button){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        //Convert string to a float value
        float textSize = Float.parseFloat(syncConnPref);
        button.setTextSize(textSize);
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
                Log.d("Network", "Network connection available");
                //Loading unique ad id
                MobileAds.initialize(homeActivity, "ca-app-pub-3940256099942544~3347511713");

                //displaying the ads
                mAdView = mView.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Carries the timer for how long the phone is locked
     * Reads settings and changes how long the phone will be locked for
     *
     */
    private void unlockPhone(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("lockout_time", "12");
        Log.d("Settings", syncConnPref );
        //converts an hours into seconds
        int time = Integer.parseInt(syncConnPref) * 3600;
        MyTimer.getInstance().startTimer(time);
    }

    /**
     *
     * Exit way for when timer runs out
     * Button is revealed for unlocking Kiosk Mode
     */
    private void unLock(){
        Button hiddenExit = mView.findViewById(R.id.exitButton);
        hiddenExit.setVisibility(View.VISIBLE);
        hiddenExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Break out!
                stopSelf(-1);

                // Take out of KioskMode
                homeActivity.setShouldBeInKioskMode(false);
            }
        });
        setKioskButtonTextSize(hiddenExit);
    }

    /**
     * Need a button for going to the Challenge_Activity
     * Reponse activated when user presses unlock phone button
     */
    public void response(){
        if (mView != null) {
            Button unlock = mView.findViewById(R.id.unlockPhone);

            unlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Code here executes on main thread after user presses button
                    loadTrivia();
                }
            });
            setKioskButtonTextSize(unlock);
        }

    }


    /*
     * For the necessary features of the phone,
     * we need a phone button, that opens the phone for emergency calls
     * we need a camera button, that opens the camera app
     */

    /**.
     *When the phone button is pressed the user will able to do emergency calls
     */
    private void call(){
        Button callApp = mView.findViewById(R.id.phone);
        callApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make view invisible
                if (mView != null) {
                    mView.setVisibility(View.GONE);
                }

                Uri number = Uri.parse("tel:5551234");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });
        setKioskButtonTextSize(callApp);
    }

    /**
     * camera() calls  the camera App when the User press the camera button
     */
    private void camera(){
        Button cameraApp = mView.findViewById(R.id.camera);
        cameraApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraCheck()) {
                    // Make view invisible
                    if (mView != null) {
                        mView.setVisibility(View.GONE);
                    }

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(cameraIntent);
                }
            }
        });
        setKioskButtonTextSize(cameraApp);
    }

    /**
     * Checks if app has user permission to device camera
     * returns true if Permission to use Camera is allowed
     */
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private boolean cameraCheck(){
        // TODO: Figure this out
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(homeActivity,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("Permission", "Showing rationale for permission request");
                Toast.makeText(this, "Showing rationale for permission request", Toast.LENGTH_SHORT).show();

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(homeActivity,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                Log.d("Permission", "Permission has been granted");
                Toast.makeText(this, "Permission has been granted", Toast.LENGTH_SHORT).show();

                return true;
            }
        } else {
            // Permission has already been granted
            Log.d("Permission", "Permission has already been granted");
            Toast.makeText(this, "Permission has already been granted", Toast.LENGTH_SHORT).show();

            return true;
        }
        return false;
    }




    // Load the trivia fragment
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


        //add the buttons to the button list for finding the correct answer
        buttons.add(questionResponse1);
        buttons.add(questionResponse2);
        buttons.add(questionResponse3);
        buttons.add(questionResponse4);

        //make the buttons not visible until the buttons are ready
        questionView.setVisibility(View.GONE);
        questionResponse1.setVisibility(View.GONE);
        questionResponse2.setVisibility(View.GONE);
        questionResponse3.setVisibility(View.GONE);
        questionResponse4.setVisibility(View.GONE);

        //Gets the text that shows how many trials you have
        trialsView = mView.findViewById(R.id.trialsRemaining);
        String trialsStr = "Trials remaining: " + Integer.toString(trials) ;
        trialsView.setText(trialsStr);

        setTriviaTextSize();
        //call the trivia Api
        //TriviaAPI triviaAPI = new TriviaAPI(this);
        new TriviaAPI(this);

        // Now that it's loaded, display it
        displayView();
        hideStatusBar();
    }

    public void setTriviaTextSize(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncConnPref = sharedPref.getString("text_size", "11");

        float textSize = Float.parseFloat(syncConnPref);
        trialsView.setTextSize(textSize);
        questionView.setTextSize(textSize);
        questionResponse1.setTextSize(textSize);
        questionResponse2.setTextSize(textSize);
        questionResponse3.setTextSize(textSize);
        questionResponse4.setTextSize(textSize);


    }

    @Override
    public void onTimerChange(String remainSec) {
        timeView.setText(remainSec);
        showViewIfNecessary();
    }

    @Override
    public void onTimerStopped() {
        timeView.setText("No more time remaining");
        unLock();
    }

    /***********************
     * Trivia
     */

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


        //Sets the texts of the button to the possible responses for the user to choose
        //Checks how many possible responses there are and then displays the same amount of response buttons
        if(responses.size() == 2){
            questionResponse1.setVisibility(View.VISIBLE);
            questionResponse2.setVisibility(View.VISIBLE);
        }else if (responses.size() == 3){
            questionResponse3.setText(responses.get(2));
            questionResponse3.setVisibility(View.VISIBLE);
        }else if (responses.size() == 4) {
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
     * @param randomposition
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
                correctButton = new Button(homeActivity.getApplicationContext());
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
            Button tempButton = mView.findViewById(view.getId());

            //Checks to see if the answer the user is the correct one
            if(tempButton.getText().equals(triviaQuestion.getCorrectAnswer())){
                Log.d("Test", "Correct response chosen");
                tempButton.setBackgroundColor(Color.GREEN);

                homeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(homeActivity.getApplicationContext(),
                                "Trivia challenge solved successfully, phone is unlocked",
                                Toast.LENGTH_LONG).show();
                    }
                });
                temporaryUnlock();

            }
            else {

                //Code that shows the correct answer
                Log.d("Test", "This is the correct response ");
                correctButton.setBackgroundColor(Color.GREEN);
                Log.d("Test", "Incorrect response chosen");
                tempButton.setBackgroundColor(Color.RED);

                if(attemptsMade == trials){
                    //call kill
                    Handler myHandler = new Handler();
                    attemptsMade = 0;
                    //display a message to user that they are out of attempts and go back to KioskActivity
                    Log.d("Test", "You are out of attempts");
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(homeActivity.getApplicationContext(),
                                    "You failed to solve the trivia challenge, phone will not be unlocked",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    loadKiosk();
                }else{
                    //continues the trivia but with a delay
                    continueTrivia();
                }
            }
        }
    };

    /**
     * Creates a temporary break for the user to use their phone after they have solved a challenge
     */
    private void temporaryUnlock(){
        stopSelf(-1);
        // Take out of KioskMode
        homeActivity.setShouldBeInKioskMode(false);
        Handler myhandler = new Handler();

        int lockbreak = 300000;
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                homeActivity.setShouldBeInKioskMode(true);
                //resumes the KioskService
                Intent intent = new Intent(homeActivity, KioskService.class);
                stopService(intent);
                startService(intent);

                //Tells the user that their phone break is over
                Toast.makeText(homeActivity.getApplicationContext(),
                        "Your break is over, phone lock will continue",
                        Toast.LENGTH_LONG).show();

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
                String trialsStr = "Trials remaining: " + Integer.toString(displayTrials) ;
                //changes how many trials there are left
                trialsView.setText(trialsStr);
                displayQuestions(triviaQuestions);
            }
        }, 1000);
    }



    /**
     * Resets the buttons colors back to default after they have been changed
     */
    private void resetButtons(){
        questionResponse1.setBackgroundResource(android.R.drawable.btn_default);
        questionResponse2.setBackgroundResource(android.R.drawable.btn_default);
        questionResponse3.setBackgroundResource(android.R.drawable.btn_default);
        questionResponse4.setBackgroundResource(android.R.drawable.btn_default);

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

        // If top activity is not this, phone, or dialer, then make visible
        String foregroundPackage = getForegroundTask();
        if (!(foregroundPackage.equals(getApplicationContext().getPackageName()) ||
                foregroundPackage.equals(DIALER_PACKAGE) ||
                foregroundPackage.equals(CAMERA_PACKAGE))) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    // Gets the foreground task. From https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag
    private String getForegroundTask() {
        String currentApp = "NULL";
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

//        Log.d("KioskService", "Current App in foreground is: " + currentApp);
        return currentApp;
    }


    public Activity getActivity(){
        return homeActivity;
    }

}