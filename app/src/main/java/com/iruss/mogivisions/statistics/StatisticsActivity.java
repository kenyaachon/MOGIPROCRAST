package com.iruss.mogivisions.statistics;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.procrastimate.HomeActivity;
import com.iruss.mogivisions.procrastimate.R;

import java.util.ArrayList;
import java.util.List;

/**
 * StatisticsActivity display the usage statistics of the user
 */
public class StatisticsActivity extends AppCompatActivity {

    //Ads
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stastistics);

        //Allows for a custome title to be used
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.statisticstitle);

        //Goes back to home activity
        ImageButton settingsButton = findViewById(R.id.returnHome);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(StatisticsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });




        // Display the fragment as the main content.
        //getSupportFragmentManager().beginTransaction()
        //        .replace(android.R.id.content, new com.iruss.mogivisions.statistics.FragmentOne())
        //        .commit();




        //Creates the TabLayout
        //setTabLayout();



        loadAds();
    }





    /**
     *
     */

    /*
    public void setTabLayout(){
        //Creates the TabLayout for the app
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentOne(), "GRAPH");
        adapter.addFragment(new FragmentThree(), "APP USAGE");
        //adapter.addFragment(new FragmentTwo(), "GUIDE");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }*/



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
                MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
                //MobileAds.initialize(homeActivity, "ca-app-pub-5475955576463045~8715927181");


                //displaying the ads
                mAdView = findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }





    /**
     * ViewPageAdapter that extends the FragmentPagerAdapter
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
