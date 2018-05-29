package com.iruss.mogivisions.experiment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.kiosk.KioskService;

public class HomeActivity extends AppCompatActivity  {
    //Ads
    private AdView mAdView;

    private boolean shouldBeInKioskMode = false;
    public final static int Overlay_REQUEST_CODE = 251;

    public boolean isShouldBeInKioskMode() {
        return shouldBeInKioskMode;
    }

    public void setShouldBeInKioskMode(boolean shouldBeInKioskMode) {
        this.shouldBeInKioskMode = shouldBeInKioskMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        shouldBeInKioskMode = false;

        initializeKiosk();
        initializeSettings();
        //sample AddMob Id
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }



    public void initializeSettings(){
        Button settingsButton = findViewById(R.id.settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    public void initializeKiosk(){
        Button kioskButton = findViewById(R.id.kiosk);

        kioskButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // If version >= 23, then need to ask for overlay permission
                if (Build.VERSION.SDK_INT >= 23) {
                    // Check if you have permission already. If not, then ask
                    if (!Settings.canDrawOverlays(HomeActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, Overlay_REQUEST_CODE);
                    } else {
                        startKiosk();
                    }
                } else {
                    startKiosk();
                }
            }
        });
    }

    // Starts the kiosk service to overlay on top
    private void startKiosk() {
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

    // Called when permission is granted
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Overlay_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= 23) {
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
}
