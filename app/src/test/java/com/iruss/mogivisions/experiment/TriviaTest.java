package com.iruss.mogivisions.experiment;

import android.util.Log;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Moses on 3/16/2018.
 */


public class TriviaTest  {

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
