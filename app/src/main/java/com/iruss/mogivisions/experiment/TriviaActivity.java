package com.iruss.mogivisions.experiment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TriviaActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    private ArrayList<TriviaQuestion> triviaQuestions;

    private TriviaQuestion triviaQuestion;

    private TextView questionView;
    //Response buttons with questions
    private Button questionResponse1;
    private Button questionResponse2;
    private Button questionResponse3;
    private Button questionResponse4;


    //Number of trials possible
    private static final int trials = 3;

    private TextView trialsView;

    //Number of trials remaining
    private int attemptsMade = 0;




    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            /*
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trivia);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        questionView = findViewById(R.id.questionText);
        //Response buttons with questions
        questionResponse1 = findViewById(R.id.questionResponse1);
        questionResponse2 = findViewById(R.id.questionResponse2);
        questionResponse3 = findViewById(R.id.questionResponse3);
        questionResponse4 = findViewById(R.id.questionResponse4);

        //make the buttons not visible until the buttons are ready
        questionView.setVisibility(View.GONE);
        questionResponse1.setVisibility(View.GONE);
        questionResponse2.setVisibility(View.GONE);
        questionResponse3.setVisibility(View.GONE);
        questionResponse4.setVisibility(View.GONE);

        //Gets the text that shows how many trials you have
        trialsView = findViewById(R.id.trialsRemaining);

        //call the trivia Api
        //TriviaAPI triviaAPI = new TriviaAPI(this);
        new TriviaAPI(this);


    }





    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
        responses.add(randomposition, triviaQuestion.getCorrectAnswer());

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
            questionResponse3.setVisibility(View.GONE);
        }else if (responses.size() == 4) {
            questionResponse3.setText(responses.get(2));
            questionResponse4.setText(responses.get(3));
            questionResponse3.setVisibility(View.VISIBLE);
            questionResponse4.setVisibility(View.VISIBLE);

        }
        //sets the Clicklistener for all the buttons
        questionResponse1.setOnClickListener(myClickListener);
        questionResponse2.setOnClickListener(myClickListener);
        questionResponse3.setOnClickListener(myClickListener);
        questionResponse4.setOnClickListener(myClickListener);

        return true;
    }

    //checks score at the end if it does not reach a certain point then person has to redo

    //method for showing how many trials remaining someone has and how many they have used

    //Button click Listener for check the response to a users click
    // TODO: Make a test for this method
    private final View.OnClickListener myClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Button tempButton = findViewById(view.getId());
            if(tempButton.getText().equals(triviaQuestion.getCorrectAnswer())){
                Log.d("Test", "Correct response chosen");
            }
            else {
                Log.d("Test", "Incorrect response chosen");
                if(attemptsMade == trials){
                    //call kill

                    //display a message to user that they are out of attempts and go back to KioskActivity
                    Log.d("Test", "You are out of attempts");
                    //finishActivity();
                    TriviaActivity.super.onBackPressed();
                }else{
                    displayQuestions(triviaQuestions);
                    attemptsMade += 1;
                    String trialsStr = "Trials remaining: ";
                    trialsStr.concat(Integer.toString(trials - attemptsMade));
                    trialsView.setText(trialsStr);
                }
            }
        }
    };


}