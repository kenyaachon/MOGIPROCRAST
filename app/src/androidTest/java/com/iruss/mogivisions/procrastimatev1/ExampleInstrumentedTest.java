package com.iruss.mogivisions.procrastimatev1;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

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