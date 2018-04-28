package com.iruss.mogivisions.experiment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Page where you choose whether or not to activate the challenge activity
 */
public class KioskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiosk);

        response();
        call();
        camera();
        settings();
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
        Intent intent = new Intent(this, TriviaActivity.class);
        startActivity(intent);
    }





    /*
     * Need to have a timer for how long, the phone stays locked
     *
     *
     */

    /*
     * For the necessary features of the phone,
     * we need a phone button, that opens the phone for emergency calls
     * we need a camera button, that opens the camera app
     */

    /**.
     *When the phone button is pressed the user will able to do emergency calls
     */
    public void call(){
        Button callApp = findViewById(R.id.phone);
        callApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:5551234");
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
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