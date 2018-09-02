package com.iruss.mogivisions.statistics;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.iruss.mogivisions.procrastimate.R;
import com.iruss.mogivisions.statistics.MyMarkerView;
import com.iruss.mogivisions.statistics.SimpleFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Displays a BarChart graph
 */
public class FragmentOne extends SimpleFragment implements OnChartGestureListener {
    //Grapht that is going to be displayed
    private BarChart mChart;

    Spinner mSpinner;



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
        View v = inflater.inflate(R.layout.fragmenttab_two, container, false);

        getUsageInterval(v);

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
    }






    public void getUsageInterval(View v){
        final View v2 = v;
        mSpinner = (Spinner) v.findViewById(R.id.spinner_time_span);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);

        BarData data;
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
        });
    }

    /**
     * Enum represents the intervals for {@link android.app.usage.UsageStatsManager} so that
     * values for intervals can be found by a String representation.
     *
     */
    //VisibleForTesting
    static enum StatsUsageInterval {
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
     *Gets the usage data of the users device
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
            SortedMap<String, Long> mySortedMap = new TreeMap<>();
            if (stats != null) {
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getPackageName(), (usageStats.getTotalTimeInForeground() / 60000));
                    //mySortedMap.put(usageStats.getPackageName(),(usageStats.getTotalTimeInForeground() / 1000));
                }
            }

            //Going through the map to put it into a list to format the data for display in the bar chart
            SortedSet<Map.Entry<String, Long>> sortedMap = entriesSortedByValues(mySortedMap);
            Iterator it = sortedMap.iterator();

            //Formating the data as BarEntry's
            for (int i = 0; i < mySortedMap.size(); i++) {
                Map.Entry<Long, Long> pair = (Map.Entry<Long, Long>) it.next();
                entries.add(new BarEntry(i, pair.getValue()));
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
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
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

