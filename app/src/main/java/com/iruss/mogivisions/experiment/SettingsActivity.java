// Copyright 2017 SDK Bridge
package com.iruss.mogivisions.experiment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity to show the settings
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new com.iruss.mogivisions.experiment.SettingsFragment())
                .commit();
    }
}
