package com.iruss.mogivisions.experiment;

/*
 * Created by Moses on 3/18/2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;

/**
 * Trivia API object created
 *
 */
public class TriviaAPI {

    //File name where offline TRIVIA database is stored
    String fileName = "";

    //Number of questions in the trivia challenge created
    private int numberOfQuestions;

    //Topic of the trivia questions
    private String category;

    //com.iruss.mogivisions.experiment.Question type: multiple choice (0) or true/false (1)
    private int questionType;

    //Level of difficulty for the questions
    private int questionDifficulty;

    //TriviaActivity
    private TriviaActivity triviaActivity = null;

    // Constant with JSON to be used offline
    static final String OFFLINE_TRIVIA_JSON = "{\"response_code\":0,\"results\":[{\"category\":\"Science: Computers\",\"type\":\"boolean\",\"difficulty\":\"medium\",\"question\":\"The HTML5 standard was published in 2014.\",\"correct_answer\":\"True\",\"incorrect_answers\":[\"False\"]},{\"category\":\"Entertainment: Music\",\"type\":\"multiple\",\"difficulty\":\"medium\",\"question\":\"Who wrote the musical composition, &quot;Rhapsody In Blue&quot;?\",\"correct_answer\":\"George Gershwin\",\"incorrect_answers\":[\"Irving Berlin\",\"Duke Ellington\",\"Johnny Mandel\"]},{\"category\":\"Animals\",\"type\":\"multiple\",\"difficulty\":\"medium\",\"question\":\"What is the scientific name for the &quot;Polar Bear&quot;?\",\"correct_answer\":\"Ursus Maritimus\",\"incorrect_answers\":[\"Polar Bear\",\"Ursus Spelaeus\",\"Ursus Arctos\"]},{\"category\":\"Animals\",\"type\":\"multiple\",\"difficulty\":\"hard\",\"question\":\"What scientific family does the Aardwolf belong to?\",\"correct_answer\":\"Hyaenidae\",\"incorrect_answers\":[\"Canidae\",\"Felidae\",\"Eupleridae\"]},{\"category\":\"Science: Computers\",\"type\":\"multiple\",\"difficulty\":\"medium\",\"question\":\"In the server hosting industry IaaS stands for...\",\"correct_answer\":\"Infrastructure as a Service\",\"incorrect_answers\":[\"Internet as a Service\",\"Internet and a Server\",\"Infrastructure as a Server\"]},{\"category\":\"Entertainment: Video Games\",\"type\":\"multiple\",\"difficulty\":\"medium\",\"question\":\"In the Portal series of games, who was the founder of Aperture Science?\",\"correct_answer\":\"Cave Johnson\",\"incorrect_answers\":[\"GLaDOs\",\"Wallace Breen\",\"Gordon Freeman\"]},{\"category\":\"Entertainment: Video Games\",\"type\":\"multiple\",\"difficulty\":\"easy\",\"question\":\"When was Left 4 Dead 2 released?\",\"correct_answer\":\"November 17, 2009\",\"incorrect_answers\":[\"May 3, 2008\",\"November 30, 2009\",\"June 30, 2010\"]},{\"category\":\"Entertainment: Television\",\"type\":\"boolean\",\"difficulty\":\"medium\",\"question\":\"Klingons respect their disabled comrades, and those who are old, injuried, and helpless.\",\"correct_answer\":\"False\",\"incorrect_answers\":[\"True\"]},{\"category\":\"Entertainment: Film\",\"type\":\"multiple\",\"difficulty\":\"medium\",\"question\":\"Leonardo Di Caprio won his first Best Actor Oscar for his performance in which film?\",\"correct_answer\":\"The Revenant\",\"incorrect_answers\":[\"The Wolf Of Wall Street\",\"Shutter Island\",\"Inception\"]},{\"category\":\"Entertainment: Television\",\"type\":\"multiple\",\"difficulty\":\"hard\",\"question\":\"Which of the following actors portrayed the Ninth Doctor in the British television show &quot;Doctor Who&quot;?\",\"correct_answer\":\"Christopher Eccleston\",\"incorrect_answers\":[\"David Tennant\",\"Matt Smith\",\"Tom Baker\"]}]}";

    public TriviaAPI(TriviaActivity triviaActivity){
        //gets the trivia activity object wanting to use the TriviaAPI class
        this.triviaActivity = triviaActivity;

        networkCheck();

    }


    /*
      *Method tests for internet connectivity
      * if internet connectivity, then triviaAPI object calls the triviaAPI
      * else the triviaAPI gets the offline database app
     */

    //public ArrayList<TriviaQuestion> decision(Context context){
    private boolean decision(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get Permission to read network status

        //Gets network state

        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            //checks if there is internet
            Log.d("attempt", "Attept is working");

            //returns the database questions from online
            if(isConnected){
                Log.d("Test", "Internet Connection is Available");
                //Sends the retrivied from the online TriviaDB to the TriviaActivity
                //triviaActivity.displayQuestions(callDB());
                callDB();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        Log.d("Internet Test", "Going to use ");
        triviaActivity.displayQuestions(getOfflineDB());
        return false;
    }

    /*
     * Checks if app has user permission to check network connection
     * returns true if Permission to check network are allowed
     * and false otherwise
     */
    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private boolean networkCheck(){
        //public boolean networkCheck(){
        Log.d("attempt", "network attempt");

        //if (ContextCompat.checkSelfPermission(this,
        if (ContextCompat.checkSelfPermission(triviaActivity,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            //if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            if (ActivityCompat.shouldShowRequestPermissionRationale(triviaActivity,
                    Manifest.permission.ACCESS_NETWORK_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.


                /*
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                */

            } else {

                // No explanation needed; request the permission
                //ActivityCompat.requestPermissions(this,
                ActivityCompat.requestPermissions(triviaActivity,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                //findouts network status
                decision(triviaActivity);


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return true;
            }
        } else {
            // Permission has already been granted
            decision(triviaActivity);
            return true;
        }
        //false if network check fails
        return false;
    }


    /**
     * Possiblity: might create method that makes the offline trivia file the first time the app is installed
     */

    /*
     *method to call the openTDB database
     * returns true if able to successfully call the Database
     */
    private void callDB(){
        /*
        GetTriviaDB getTriviaDB = new GetTriviaDB();
        getTriviaDB.execute();
        return getTriviaDB.getTriviaQuestions();*/
        new GetTriviaDB().execute();
    }


    /*
      *method to get the offline trivia file
     */
    private ArrayList<TriviaQuestion> getOfflineDB(){
        // Get trivia questions (currently just offline)
        ArrayList<TriviaQuestion> triviaQuestions =
                TriviaQuestion.createQuestionsFromJSON(TriviaAPI.OFFLINE_TRIVIA_JSON);
        testQuestions(triviaQuestions);

        //returns the triviaQuestion for the online database
        return triviaQuestions;
    }

    // TODO: Move this into test suite and make it so that it's pass/fail
    private void testQuestions(ArrayList<TriviaQuestion> questions) {
        String tag = "Test questions";
        Log.d(tag, "Number of questions: " + questions.size());
        Log.d(tag, "First question: " + questions.get(0).getQuestion());
        Log.d(tag, "First answer: " + questions.get(0).getCorrectAnswer());
        Log.d(tag, "Number of incorrect answers: " + questions.get(0).getIncorrectAnswers().size());

    }


    /**
     * Calls the online TriviaDB
     */
    private class GetTriviaDB extends AsyncTask<Void, Void, Void> {
        private TriviaQuestion triviaQuestion;
        private ArrayList<TriviaQuestion> triviaQuestionArrayList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(triviaActivity,"Json Data is downloading"
                    ,Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            //Gets the JSON data from topenTDB
            HttpHandler sh = new HttpHandler();
            String openTDBURL = "https://opentdb.com/api.php?amount=10";
            String requestedDB = sh.makeServiceCall(openTDBURL);

            //Parses the JSON data into a list of questions
            Log.e(TAG, "Response from url: " + requestedDB);
            if (requestedDB != null) {
                triviaQuestion = new TriviaQuestion();
                //parse the json data from the online triviaDB
                triviaQuestionArrayList = triviaQuestion.createQuestionsFromJSON(requestedDB);

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                triviaActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(triviaActivity.getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        /**
         * Once JSON data is retrieved from the database, send it to the TriviaActivity
         * @param result
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            triviaActivity.displayQuestions(triviaQuestionArrayList);

        }

    }





}
