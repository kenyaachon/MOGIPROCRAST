package com.iruss.mogivisions.experiment;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.iruss.mogivisions.experiment", appContext.getPackageName());
    }



    // settings button under home
    @Test
    public void displaySettings() {
        onView(withId(R.id.settings))            // withId(R.id.settings) is a ViewMatcher
                .perform(click())               // click() is a ViewAction
                .check(matches(isDisplayed())); // matches(isDisplayed()) is a ViewAssertion
        // not sure how isDisplayed works or if that is a thing I can just call like that
    }



    // questions are displayed, in TriviaActivity.java but also in TriviaFragment.java, theyre the same
    @Test
    public void displayQuestions() {

    }

    // not sure what to do for this one
    @Test
    public void myClickListener() {

    }

    // checks that camera opens
    @Test
    public void cameraOpens(){

    }

    // checks that dialer opens
    @Test
    public void dialerOpens() {

    }
}
