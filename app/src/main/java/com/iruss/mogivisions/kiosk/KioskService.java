package com.iruss.mogivisions.kiosk;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.procrastimate.HomeActivity;
import com.iruss.mogivisions.procrastimate.R;
import com.iruss.mogivisions.procrastimate.TriviaAPI;
import com.iruss.mogivisions.procrastimate.TriviaQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;

public class KioskService extends Service implements MyTimer.TimerRunning {

    //contains the ads
    private AdView mAdView;

    public final String ANDROID_CHANNEL_ID = "KioskService";

    private WindowManager mWindowManager;
    WindowManager.LayoutParams mWindowsParams;
    private View mView;
    private TextView timeView;

    // For trivia
    private ArrayList<TriviaQuestion> triviaQuestions;

    private TriviaQuestion triviaQuestion;

    private Button unlock;

    private TextView questionView;
    //Response buttons with questions
    private Button questionResponse1;
    private Button questionResponse2;
    private Button questionResponse3;
    private Button questionResponse4;


    //Number of trials possible
    private static final int trials = 4 ;

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
    //Display of trials
    private RatingBar trialBar;

    // Whether waiting for launcher to launch phone or camera
    private boolean waitingForLauncher = false;
    // For phone calls
    private SimplePhoneStateListener phoneStateListener = new SimplePhoneStateListener();


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

        loadKiosk();

        // Start listening for phone calls
        TelephonyManager telephonyManager = (TelephonyManager) homeActivity.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        return super.onStartCommand(intent, flags, startId);
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
            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            //chan.lightColor = Color.BLUE;
            //chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE;
            NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(chan);
        }
        return channelId;
    }



    public void initChannels(Context context) {
    if (Build.VERSION.SDK_INT < 26) {
        return;
    }
    NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationChannel channel = new NotificationChannel("default",
                                                          "KioskMode",
                                                          NotificationManager.IMPORTANCE_DEFAULT);
    channel.setDescription("KioskService for KioskMode");
    notificationManager.createNotificationChannel(channel);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, ANDROID_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("KioskMode restarted")
            .setAutoCancel(true);
    Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(101, notification);
   }


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

        // Stop listening for phone calls
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        super.onDestroy();
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
        // Remove existing
        if (mView != null) {
            mWindowManager.removeViewImmediate(mView);
        }


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.fragment_kiosk, null);

        timeView = mView.findViewById(R.id.timeView);
        MyTimer.getInstance().setTimerRuningListener(this);

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
            if(isConnected && homeActivity != null){
                Log.d("Network", "Network connection available");
                //Loading unique ad id
                MobileAds.initialize(homeActivity, "ca-app-pub-3940256099942544~3347511713");
                //MobileAds.initialize(homeActivity, "ca-app-pub-5475955576463045~8715927181");


                //displaying the ads
                mAdView = mView.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (NullPointerException e) {
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
        unlock.setVisibility(View.GONE);

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
            unlock = mView.findViewById(R.id.unlockPhone);

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
                    cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(cameraIntent);
                //}
            }
        });
        //setKioskButtonTextSize(cameraApp);
    }



    // Load the trivia fragment

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

        trialBar = (RatingBar) mView.findViewById(R.id.trialBar);
        trialBar.setRating(trials);

        setTriviaTextSize();
        //call the trivia Api
        //TriviaAPI triviaAPI = new TriviaAPI(this);
        new TriviaAPI(this);

        // Now that it's loaded, display it
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
        trialsView.setTextSize(textSize);
        successView.setTextSize(textSize);
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
            successBar = (RatingBar) mView.findViewById(R.id.successBar);
            //trialBar = (RatingBar) mView.findViewById(R.id.trialBar);
            //trialBar.setRating(trials);

            Button tempButton = mView.findViewById(view.getId());

            //Checks to see if the answer the user is the correct one
            if(tempButton.getText().equals(triviaQuestion.getCorrectAnswer())){
                Log.d("Test", "Correct response chosen");
                tempButton.setBackgroundColor(Color.GREEN);
                success += 1;
                successBar.setRating(success);

                if(success == 3) {
                    homeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(homeActivity.getApplicationContext(),
                                    "Trivia challenge solved successfully, phone is unlocked for 5 mins",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    temporaryUnlock();
                }else{
                    continueTrivia();
                }

            }
            else {

                //Code that shows the correct answer
                Log.d("Test", "This is the correct response ");
                correctButton.setBackgroundColor(Color.GREEN);
                Log.d("Test", "Incorrect response chosen");
                tempButton.setBackgroundColor(Color.RED);

                if(attemptsMade == trials){
                    //call kill
                    success = 0;
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

                Intent service = new Intent(homeActivity, KioskService.class);


                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //ComponentName service = new ComponentName(getApplicationContext(), KioskService.class);
                    //ContextCompat.startForegroundService(homeActivity, service);
                    homeActivity.startForegroundService(service);
                } else{
                    stopService(service);
                    startService(service);
                }

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
                                mView.getHandler().post(new Runnable() {
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
}