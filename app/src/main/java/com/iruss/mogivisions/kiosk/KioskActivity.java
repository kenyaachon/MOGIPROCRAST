package com.iruss.mogivisions.kiosk;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iruss.mogivisions.experiment.R;
import com.iruss.mogivisions.experiment.SettingsActivity;
import com.iruss.mogivisions.experiment.TriviaActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Page where you choose whether or not to activate the challenge activity
 */
public class KioskActivity extends AppCompatActivity {

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    private Button hiddenExitButton;
    private TextView timeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_kiosk);


        // every time someone enters the kiosk mode, set the flag true
        PrefUtils.setKioskModeActive(true, getApplicationContext());

        hiddenExitButton = findViewById(R.id.exitButton);
        hiddenExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Break out!
                PrefUtils.setKioskModeActive(false, getApplicationContext());
                Toast.makeText(getApplicationContext(),"You can leave the app now!", Toast.LENGTH_SHORT).show();
                PackageManager pm = getPackageManager();
                pm.clearPackagePreferredActivities ("com.iruss.mogivisions.kiosk");
            }
        });


        timeView = findViewById(R.id.timeView);
        unlockPhone(2);

        
        response();
        call();
        camera();
        settings();
    }

    public void unlockPhone(int hours){
        int time = hours * 3600000;

        //Delays the reveal of the exit button
        new CountDownTimer(20000, 1000) {

            public void onTick(long millisUntilFinished) {
                timeView.setText("Time remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                hiddenExitButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }


    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        // Update text about lockout time
        TextView textView = findViewById(R.id.timeView);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int hours = Integer.parseInt(sharedPref.getString("lockout_time", "12"));
        textView.setText("Unlock time: " + hours + " hours");
    }

    /**
     * Need a button for going to the Challenge_Activity
     * Reponse activated when user presses unlock phone button
     */
    public void response(){
        Button unlock = findViewById(R.id.unlockPhone);

        unlock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                //Intent intent = new Intent(KioskActivity.this, TriviaActivity.class);
                //EditText editText = (EditText) findViewById(R.id.editText);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                //startActivity(intent);
                trivia();
            }
        });


    }
    //Calls the Trivia Activity
    public void trivia(){
        startActivity(new Intent(this, TriviaActivity.class));
    }

    /**.
     *When the phone button is pressed the user will able to do emergency calls
     */
    public void call(){
        Button callApp = findViewById(R.id.phone);
        callApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:5551234");
                startActivity(new Intent(Intent.ACTION_DIAL, number));
            }
        });
    }

    /**
     * camera() calls  the camera App when the User press the camera button
     */
    public void camera(){
        Button cameraApp = findViewById(R.id.camera);
        cameraApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( cameraCheck()) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(cameraIntent);
                }
            }
        });
    }

    /**
     * camera() calls  the camera App when the User press the camera button
     */
    public void settings(){
        Button settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(KioskActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Checks if app has user permission to device camera
     * returns true if Permission to use Camera is allowed
     */
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public boolean cameraCheck(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return true;
            }
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

}