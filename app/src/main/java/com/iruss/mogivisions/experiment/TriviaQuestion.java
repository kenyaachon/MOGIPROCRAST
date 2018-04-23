package com.iruss.mogivisions.experiment;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;

/**
 * Created by Peter on 3/23/18.
 */

// Holds data about a trivia question
public class TriviaQuestion {
    private String question = "";
    private String correctAnswer = "";
    private ArrayList<String> incorrectAnswers = new ArrayList<String>();

    public String getQuestion() {
        return question;
    }


    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public ArrayList<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    // Factory to create Trivia Question array from a string of JSON
    static public ArrayList<TriviaQuestion> createQuestionsFromJSON(String jsonString) {
        ArrayList<TriviaQuestion> questions = new ArrayList<TriviaQuestion>();

        try {
            // Turn string into a JSON object
            JSONObject topLevelObject = new JSONObject(jsonString);

            // Loop through all results
            JSONArray results = topLevelObject.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);

                // Create a new trivia question, converting everything from HTML
                TriviaQuestion triviaQuestion = new TriviaQuestion();
                triviaQuestion.question =  Jsoup.parse(result.getString("question")).text();
                triviaQuestion.correctAnswer = Jsoup.parse(result.getString("correct_answer")).text();

                // Loop through all incorrect answers
                JSONArray incorrectAnswers = result.getJSONArray("incorrect_answers");
                for (int j=0; j < incorrectAnswers.length(); j++) {
                    String incorrectAnswer = incorrectAnswers.getString(j);
                    String decodedIncorrectAnswer = Jsoup.parse(incorrectAnswer).text();
                    triviaQuestion.incorrectAnswers.add(decodedIncorrectAnswer);
                }

                // Add it to your array
                questions.add(triviaQuestion);
            }


        } catch (JSONException ex) {
            Log.d("TrivaQuestion", "Invalid JSON: " + ex.getMessage());
        }

        return questions;
    }
}
