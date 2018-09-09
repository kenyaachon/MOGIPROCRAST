package com.iruss.mogivisions.statistics;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.iruss.mogivisions.procrastimate.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTwo extends Fragment {

    private TextView textView;
    private StringBuilder text = new StringBuilder();
    public FragmentTwo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragmenttab_three, container, false);
       createText(view);
        return view;
    }

    /**
     * Sets the text of TextView
     * @param text, the text element in the layout
     */
    public void setTextSize(TextView text){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String syncConnPref = sharedPref.getString("text_size", "11");

        float textsize = Float.parseFloat(syncConnPref);
        text.setTextSize(textsize);
    }


    /**
     * Reads text from a file and displays it
     * @param view, root element of the layout
     */
    public void createText(View view) {


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

            TextView output = view.findViewById(R.id.summtext);
            setTextSize(output);
            output.setText(text);
        }
    }



}
