package com.iruss.mogivisions.experiment;

/*
 * Created by Moses on 3/18/2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Trivia API object created
 *
 */
public class TriviaAPI {

    //File name where offline TRIVIA database is stored
    String fileName = "";

    //Number of questions in the trivia challenge created
    int numberOfQuestions;

    //Topic of the trivia questions
    String category;

    //Question type: multiple choice (0) or true/false (1)
    int questionType;

    //Level of difficulty for the questions
    int questionDifficulty;

    /*
      *Method tests for internet connectivity
      * if internet connectivity, then triviaAPI object calls the triviaAPI
      * else the triviaAPI gets the offline database app
     */

    public boolean decision(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get Permission to read network status

        //Gets network state
        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            //checks if there is internet
            if(isConnected){
                callDB();
                return true;
            }
            else{
                getOfflineDB();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    /*
     * Checks if app has user permission to device camera
     * returns true if Permission to use Camera is allowed
     */

    /*
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public boolean networkCheck(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
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
    }**/


    /**
     * Possiblity: might create method that makes the offline trivia file the first time the app is installed
     */

    /*
     *How JSON data from openTDB is parsed
     */

    /*
     *method to call the openTDB database
     * returns true if able to successfully call the Database
     */
    public boolean callDB(){
        return true;
    }


    /*
      *method to get the offline trivia file
     */
    public void getOfflineDB(){

    }


}
