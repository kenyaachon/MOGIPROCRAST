package com.iruss.mogivisions.statistics;

import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.iruss.mogivisions.procrastimate.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A simple {@link Fragment} subclass.
 * Displays a BarChart graph
 */
public class FragmentOne extends SimpleFragment implements OnChartGestureListener, Spinner.OnItemSelectedListener, OnChartValueSelectedListener {
    //Grapht that is going to be displayed
    private BarChart mChart;

    private View v;
    private Spinner mSpinner;

    private Dialog myDialog;

    private DateFormat mDateFormat = new SimpleDateFormat();



    public FragmentOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragmenttab_two, container, false);
        v = inflater.inflate(R.layout.fragmenttab_two, container, true);
        myDialog = new Dialog(getActivity());


        getUsageInterval(v);


        setHelp(v);

        /*
        mChart = v.findViewById(R.id.barchart);

        mChart.getDescription().setEnabled(false);
        mChart.setOnChartGestureListener(this);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        //Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),"OpenSans-Light.ttf");

        //BarData data = generateBarData(1, 20000, 50);

        //Getting the Usage data
        BarData data = getUsageData();
        mChart.setData(data);
        mChart.invalidate();

        //Legend l = mChart.getLegend();
        //l.setTypeface(tf);


        //Defining the YAxis
        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(tf);

        //Formating the values on the YAXIS
        leftAxis.setValueFormatter(new MyYAxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);*/


        return v;
    }

    /**
     * Displays the Bar Chart
     * @param v, the View where the BarChart will be created
     * @param data, the formatted BarData needed to display the BarChart
     */
    public void displayChart(View v, BarData data){
        mChart = v.findViewById(R.id.barchart);

        mChart.getDescription().setEnabled(false);
        mChart.setOnChartGestureListener(this);

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);

        //Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),"OpenSans-Light.ttf");

        //BarData data = generateBarData(1, 20000, 50);

        //Getting the Usage data
        //BarData data = getUsageData();
        mChart.setData(data);
        mChart.invalidate();

        //Legend l = mChart.getLegend();
        //l.setTypeface(tf);


        //Defining the YAxis
        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(tf);

        //Formating the values on the YAXIS
        leftAxis.setValueFormatter(new MyYAxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);

        mChart.setOnChartValueSelectedListener(this);

    }


    /**
     * Accesses the selected time interval on a spinner,
     * afterwards calls getUsageData to get usage statistics and then calls displayChart to display a bar chart
     * @param v, the View where the Spinner is located
     */
    public void getUsageInterval(View v){
        //final View v2 = v;
        mSpinner = (Spinner) v.findViewById(R.id.time_selector);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);

        String position = mSpinner.getSelectedItem().toString();
        FragmentOne.StatsUsageInterval statsUsageInterval = FragmentOne.StatsUsageInterval
                .getValue(position);

        if (statsUsageInterval != null) {
            BarData data = getUsageData(statsUsageInterval.mInterval);
            displayChart(v, data);
        }


        mSpinner.setOnItemSelectedListener(this);


        /*
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            String[] strings = getResources().getStringArray(R.array.action_list);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FragmentOne.StatsUsageInterval statsUsageInterval = FragmentOne.StatsUsageInterval
                        .getValue(strings[position]);


                if (statsUsageInterval != null) {
                    //List<UsageStats> usageStatsList = getUsageData(statsUsageInterval.mInterval);
                    //data = getUsageData();
                    BarData data = getUsageData(statsUsageInterval.mInterval);
                    displayChart(v2, data);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }

    private StringBuilder text = new StringBuilder();

    /**
     * Reads the content of a text file that contains a help guide about the BarChart,
     * writes the contents of the text file onto a dialog
     * @param dialog, the dialog that is going to be edited
     */
    public void getHelpText(Dialog dialog){
        BufferedReader reader = null;

        //Attempting to read the file in Assets
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getActivity().getAssets().open("guide.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            //if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                //log the exception
            }
            //}

            TextView output = dialog.findViewById(R.id.helpText);
            output.setText(text);
        }
    }

    //Help Dialog that is used for display help content about bar chart
    private Dialog helpDialog;

    /**
     * Sets a help button to display a dialog showing a guide of how to use the barchart
     * @param view, the layout to be searched for a help button
     */
    public void setHelp(View view){
        helpDialog = new Dialog(view.getContext());
        ImageButton help = view.findViewById(R.id.help);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog.setContentView(R.layout.helpdialog);

                TextView txtclose = helpDialog.findViewById(R.id.txtclose);
                txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        helpDialog.dismiss();
                    }
                });

                getHelpText(helpDialog);
                helpDialog.setCancelable(true);
                helpDialog.show();

            }
        });
    }

    /**
     * OnItemSelected is called when an item in a Spinner is selected
     * @param parent, the layout of where the spinner is
     * @param arg1
     * @param position
     * @param id
     */
    public void onItemSelected(AdapterView<?> parent, View arg1, int position,long id) {
        String[] strings = getResources().getStringArray(R.array.action_list);

        FragmentOne.StatsUsageInterval statsUsageInterval = FragmentOne.StatsUsageInterval
                .getValue(strings[position]);


        if (statsUsageInterval != null) {
            //List<UsageStats> usageStatsList = getUsageData(statsUsageInterval.mInterval);
            //data = getUsageData();
            Log.i("Stats Interval", Integer.toString(statsUsageInterval.mInterval));
            BarData data = getUsageData(statsUsageInterval.mInterval);
            displayChart(v, data);
        }
    }

    /**
     * Called if no item on a Spinner is Selected
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * OnValueSelected is called when a bar on the BarChart is touched,
     * @param e, the specific entry in the BarChart that was touched
     * @param h,
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //fire up event
        BarEntry b1 = (BarEntry) e;
        float barNum = b1.getX();
        Log.i("Bar Selected", Float.toString(barNum));
        UsageStats stats = (UsageStats) b1.getData();
        Log.i("Bar Selected", stats.getPackageName());

        /*
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(stats.getPackageName());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();*/

        TextView txtclose;
        myDialog.setContentView(R.layout.custompopup);
        txtclose =myDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        TextView appName = myDialog.findViewById(R.id.popupAppName);
        appName.setText(stats.getPackageName());

        TextView appTime = myDialog.findViewById(R.id.popupTime);
        String time = Long.toString(stats.getTotalTimeInForeground()/ 60000) + "min(s)";
        appTime.setText(time);

        TextView appLastUsed = myDialog.findViewById(R.id.popupLastUsed);
        Long lastTime = stats.getLastTimeUsed();
        appLastUsed.setText(mDateFormat.format(lastTime));

        ImageView appIcon = myDialog.findViewById(R.id.popupIcon);
        try {
            appIcon.setImageDrawable(getActivity().getPackageManager()
                    .getApplicationIcon(stats.getPackageName()));
        } catch (PackageManager.NameNotFoundException e1) {
            Log.w("App Icon", String.format("App Icon is not found for %s", stats.getPackageName()));
            //customUsageStats.appIcon = getActivity()
            //        .getDrawable(R.drawable.ic_default_app_launcher);
        }

        appIcon.getLayoutParams().height = 150;
        appIcon.getLayoutParams().width = 150;

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

    }

    /**
     * Called if no specific bar on a bar chart is selected
     */
    @Override
    public void onNothingSelected() {
    }



    /**
     * Enum represents the intervals for {@link android.app.usage.UsageStatsManager} so that
     * values for intervals can be found by a String representation.
     *
     */
    //VisibleForTesting
    enum StatsUsageInterval {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

        private int mInterval;
        private String mStringRepresentation;

        StatsUsageInterval(String stringRepresentation, int interval) {
            mStringRepresentation = stringRepresentation;
            mInterval = interval;
        }


        static FragmentOne.StatsUsageInterval getValue(String stringRepresentation) {
            for (FragmentOne.StatsUsageInterval statsUsageInterval : values()) {
                if (statsUsageInterval.mStringRepresentation.equals(stringRepresentation)) {
                    return statsUsageInterval;
                }
            }
            return null;
        }
    }

    /**
     *
     * @param intervalType, gets the interval Type that is desired
     * @return BarData of the list of UsageStats
     */
    public BarData getUsageData(int intervalType) {
        ArrayList<IBarDataSet> sets = new ArrayList<>();

        //Uses the UsageStatsManger to get the recent device usage statistics
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);

            long time = System.currentTimeMillis();
            long totalPhoneTime = 0;

            ArrayList<BarEntry> entries = new ArrayList<>();
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(intervalType, time - 1000 * 10, time);
            //List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

            //Puts the data into a sorted map
            SortedMap<String, UsageStats> mySortedMap = new TreeMap<>();
            if (stats != null) {
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getPackageName(), usageStats);
                    //mySortedMap.put(usageStats.getPackageName(), (usageStats.getTotalTimeInForeground() / 60000));
                    //mySortedMap.put(usageStats.getPackageName(),(usageStats.getTotalTimeInForeground() / 1000));
                }
            }

            //Going through the map to put it into a list to format the data for display in the bar chart
            SortedSet<Map.Entry<String, UsageStats>> sortedMap = entriesSortedByValues(mySortedMap);
            Iterator it = sortedMap.iterator();

            //Formating the data as BarEntry's
            for (int i = 0; i < mySortedMap.size(); i++) {
                Map.Entry<String, UsageStats> pair = (Map.Entry<String, UsageStats>) it.next();
                //restricts the amount of apps shown to be ones with good values bigger than 0 minutes
                Long timeForeground = pair.getValue().getTotalTimeInForeground() / 60000;
                /*
                if(pair.getValue() > 0.00){
                    entries.add(new BarEntry(i, pair.getValue()));
                }*/
                if(timeForeground > 0.00){
                    entries.add(new BarEntry(i, timeForeground, pair.getValue()));
                }
            }
            Log.i("Usage Data", Integer.toString(entries.size()));

            //Creating the BarChart
            BarDataSet ds = new BarDataSet(entries, getLabel(0));
            ds.setColors(ColorTemplate.VORDIPLOM_COLORS);
            sets.add(ds);

        }
        /*
        else{
            ActivityManager tasksManger = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> usageStats = tasksManger.getRecentTasks(MAX_RECENT_TASKS, 0);

            SortedMap<String,Long> mySortedMap = new TreeMap<>();
            for(int i = 0; i < usageStats.size(); i++){
                ComponentName app = usageStats.get(i).origActivity;
                mySortedMap.put(app.getPackageName(), usageStats.get(i));

            }

    }*/

        BarData d = new BarData(sets);
        //d.setValueTypeface(tf);
        return d;

    }

    //Sorts a map by its Values
    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,UsageStats>> entriesSortedByValues(Map<K,UsageStats> map) {
        SortedSet<Map.Entry<K,UsageStats>> sortedEntries = new TreeSet<Map.Entry<K,UsageStats>>(
                new Comparator<Map.Entry<K,UsageStats>>() {
                    @Override public int compare(Map.Entry<K,UsageStats> e1, Map.Entry<K,UsageStats> e2) {
                        //int res = e1.getValue().compareTo(e2.getValue());
                        int res = Long.compare(e1.getValue().getTotalTimeInForeground(), e2.getValue().getTotalTimeInForeground());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    private String[] mLabels = new String[] { "Applications", "Company B", "Company C", "Company D", "Company E", "Company F" };
//    private String[] mXVals = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec" };

    /**
     * Gets the desired Label for the bar chart graph
     * @param i, the id of the label in the labels list
     * @return
     */
    private String getLabel(int i) {
        return mLabels[i];
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END");
        mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

}


