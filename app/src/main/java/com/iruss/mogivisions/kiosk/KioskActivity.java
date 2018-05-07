package com.iruss.mogivisions.kiosk;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.iruss.mogivisions.experiment.R;
import com.iruss.mogivisions.experiment.TriviaFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Page where you choose whether or not to activate the challenge activity
 */
public class KioskActivity extends AppCompatActivity
        implements KioskFragment.OnFragmentInteractionListener,
        TriviaFragment.OnFragmentInteractionListener{

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    public static Boolean isBeingRestored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_kiosk);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        KioskFragment fragment = new KioskFragment();
        fragmentTransaction.add(R.id.kioskFrame, fragment);
        fragmentTransaction.commit();

        // every time someone enters the kiosk mode, set the flag true
        PrefUtils.setKioskModeActive(true, getApplicationContext());

    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        } else if (isBeingRestored) {
            isBeingRestored = false;
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
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Load the trivia fragment
    public void loadTrivia() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        TriviaFragment fragment = new TriviaFragment();
        fragmentTransaction.replace(R.id.kioskFrame, fragment);
        fragmentTransaction.commit();
    }

    // Load the kiosk fragment
    public void loadKiosk() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        KioskFragment fragment = new KioskFragment();
        fragmentTransaction.replace(R.id.kioskFrame, fragment);
        fragmentTransaction.commit();
    }
}