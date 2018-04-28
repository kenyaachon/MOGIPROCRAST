package com.iruss.mogivisions.experiment;

/**
 * Created by Moses on 3/16/2018.
 */


import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;



// EVERYTIME I TRY TO RUN THIS: Error running 'TriviaTest': The activity 'TriviaTest' is not declared in AndroidManifest.xml
public class TriviaTest extends TriviaActivity {
    ArrayList<TriviaQuestion> triviaQuestions;

    @Test
    public void displayQuestionsOfflineDbTest() {
        // not sure where stuff is added to triviaQuestions yet
        // also triviaQuestions is private, not sure how to interact with it yet

        // need to add to triviaQuestions
        triviaQuestions = new ArrayList<>();

        //
        triviaQuestions = TriviaQuestion.createQuestionsFromJSON(TriviaAPI.OFFLINE_TRIVIA_JSON);

        assertEquals(true, displayQuestions(triviaQuestions));
    }

    @Test
    public void displayQuestionsOnlineDbTest() {
        // do same as above but get it to work with the online version of questions

        triviaQuestions = new ArrayList<>();

        // add questions from online db
        HttpHandler sh = new HttpHandler();
        String openTDBURL = "https://opentdb.com/api.php?amount=10";
        String requestedDB = sh.makeServiceCall(openTDBURL);
        triviaQuestions = TriviaQuestion.createQuestionsFromJSON(requestedDB);

        assertEquals(true, displayQuestions(triviaQuestions));
    }
}

