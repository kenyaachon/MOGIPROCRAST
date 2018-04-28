package com.iruss.mogivisions.experiment;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


/**
 * Page where you choose whether or not to activate the challenge activity
 */
public class KioskActivity extends AppCompatActivity
        implements KioskFragment.OnFragmentInteractionListener,
            TriviaFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiosk);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        KioskFragment fragment = new KioskFragment();
        fragmentTransaction.add(R.id.kioskFrame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update text about lockout time
        TextView textView = findViewById(R.id.textView);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int hours = Integer.parseInt(sharedPref.getString("lockout_time", "12"));
        textView.setText("Unlock time: " + hours + " hours");
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
