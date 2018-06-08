package com.iruss.mogivisions.experiment;

/**
 * Created by Moses on 3/16/2018.
 */




import com.iruss.mogivisions.kiosk.KioskActivity;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;


// GET THIS TO ACTUALLY PUSH PROPERLY
// ALSO EVERY TIME I TRY TO RUN : Error running 'TriviaTest': The activity 'TriviaTest' is not declared in AndroidManifest.xml
// THIS HAPPENS FOR ALL TESTS
public class KioskTest extends KioskActivity {
    //Test for making sure the phone app or phone activity is called properly
    //Test for making sure the camera app is called properly

    @Test
    public void cameraPermissionCheckTest() {

        // cameraCheck should return false to begin with because presumably permission hasn't been asked for
        assertEquals(false, cameraCheck());

        // do something here to give permission to make cameraCheck return true
        // Assert.assertEquals(cameraCheck(), true);

    }

    // kiosk button under home
    @Test
    public void displaySettings() throws Exception {
        onView(withId(R.id.kiosk))
                .perform(click())
                .check(matches(isDisplayed()));
    }



}
