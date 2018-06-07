package com.iruss.mogivisions.experiment;

import org.junit.Test;

/**
 * Created by Moses on 3/16/2018.
 */

public class KioskTest extends KioskActivity {

    // kiosk button under home
    @Test
    public void displaySettings() throws Exception {
        onView(withId(R.id.kiosk))
                .perform(click())
                .check(matches(isDisplayed()));
    }
}
