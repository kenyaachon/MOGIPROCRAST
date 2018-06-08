package com.iruss.mogivisions.experiment;

/**
 * Created by Moses on 3/16/2018.
 */




import android.util.Log;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Assert;
import java.util.ArrayList;
import static junit.framework.Assert.assertEquals;



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

    @Test
    public void testQuestions(ArrayList<TriviaQuestion> questions) {
       /*
       String tag = "Test questions";
       Log.d(tag, "Number of questions: " + questions.size());
       Log.d(tag, "First question: " + questions.get(0).getQuestion());
       Log.d(tag, "First answer: " + questions.get(0).getCorrectAnswer());
       Log.d(tag, "Number of incorrect answers: " + questions.get(0).getIncorrectAnswers().size());
       */

        // REPLACE ALL OF THESE EXPECTED VALUES WITH THE REAL EXPECTED VALUES
        assertEquals(1, questions.size());
        assertEquals("a", questions.get(0).getQuestion());
        assertEquals("b", questions.get(0).getCorrectAnswer());
        assertEquals(1, questions.get(0).getIncorrectAnswers().size());

    }
}