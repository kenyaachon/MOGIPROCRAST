package com.iruss.mogivisions.kiosk;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


// Based on http://stackoverflow.com/a/7484289/922168

public class TimePreference extends DialogPreference {
    private int mHour = 0;
    private int mMinute = 0;
    private TimePicker picker = null;
    private final String DEFAULT_VALUE = "00:00";

    /**
     * Gets current saved Hour
     * @param time, saved time listing string
     * @return, returns numerical value of hour
     */
    public static int getHour(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    /**
     * Gets current saved Minute
     * @param time, saved time listing string
     * @return, returns numerical value of minute
     */
    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    /**
     * Changes values for hour and minute
     * @param hour, value to set hour
     * @param minute, value to set minute
     */
    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
        String time = toTime(mHour, mMinute);
        Log.i("TimeSetTime", time);
        persistString(time);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();


    }

    /**
     * Returns formatted version of giving hour and minute
     * @param hour, value representing hour
     * @param minute, value representing minute
     * @return,
     */
    public String toTime(int hour, int minute) {

        return Integer.valueOf(hour) + ":" + Integer.valueOf(minute);
    }

    /**
     * Updates time preference summary listing
     */
    public void updateSummary() {
        String time = Integer.toString(mHour) + ":" + Integer.toString(mMinute);

        Log.i("Time", time);
        //setSummary(time24to12(time));
        setSummary(toDate(time));
    }
    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        //picker.setBackground(getContext().getResources().getDrawable(R.drawable.settingsbackground));
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(mHour);
        picker.setCurrentMinute(mMinute);
    }

    /**
     * Changes TimePreference listing when time preference is closed
     * @param positiveResult, indicates whether or not new value was saved
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        //Change preference if save button was pressed
        if (positiveResult) {
            int currHour = picker.getCurrentHour();
            int currMinute = picker.getCurrentMinute();

            if (!callChangeListener(toTime(currHour, currMinute))) {
                return;
            }

            // persist
            setTime(currHour, currMinute);
            updateSummary();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /**
     * Sets the displayed value of the TimePreference listing
     * @param restorePersistedValue
     * @param defaultValue
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String time = null;

        if (restorePersistedValue) {
            if (defaultValue == null) {
                time = getPersistedString(DEFAULT_VALUE);
            }
            else {
                time = getPersistedString(DEFAULT_VALUE);
            }
        }
        else {
            time = defaultValue.toString();
        }

        Log.i("TimeSet", time);
        int currHour = getHour(time);
        int currMinute = getMinute(time);

        Log.i("TimeHour", Integer.toString(currHour));
        Log.i("TimeMinute", Integer.toString(currMinute));

        // need to persist here for default value to work
        setTime(currHour, currMinute);
        updateSummary();
    }


    /**
     * Formats time
     * @param inTime
     * @return
     */
    public static String toDate(String inTime) {
        try {
            DateFormat inTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
            Date inDate = inTimeFormat.parse(inTime);
            return inTimeFormat.format(inDate);
        } catch(ParseException e) {
            return null;
        }
    }


/*
    public static String time24to12(String inTime) {
        Date inDate = toDate(inTime);
        if(inDate != null) {
            DateFormat outTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            return outTimeFormat.format(inDate);
        } else {
            return inTime;
        }
    }*/
}