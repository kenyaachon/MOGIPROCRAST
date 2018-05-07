package com.iruss.mogivisions.kiosk;

/**
 * Created by Moses on 4/28/2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iruss.mogivisions.experiment.R;
import com.iruss.mogivisions.experiment.SettingsActivity;

import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KioskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KioskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KioskFragment extends Fragment {
    // The fragment's view
    private View fragmentView;
    private TextView timeView;


    private OnFragmentInteractionListener mListener;

    public KioskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment KioskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KioskFragment newInstance() {
        KioskFragment fragment = new KioskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_kiosk, container, false);

        timeView = fragmentView.findViewById(R.id.timeView);
        unlockPhone();
        response();
        call();
        camera();
        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Need a button for going to the Challenge_Activity
     * Reponse activated when user presses unlock phone button
     */
    public void response(){
        if (fragmentView != null) {
            Button unlock = fragmentView.findViewById(R.id.unlockPhone);

            unlock.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Code here executes on main thread after user presses button
                    trivia();
                }
            });
        }

    }
    //Calls the Trivia Activity
    public void trivia(){
        if (mListener != null && mListener instanceof KioskActivity) {
            KioskActivity kioskActivity = (KioskActivity) mListener;
            kioskActivity.loadTrivia();
        }

    }


    /*
     * For the necessary features of the phone,
     * we need a phone button, that opens the phone for emergency calls
     * we need a camera button, that opens the camera app
     */

    /**.
     *When the phone button is pressed the user will able to do emergency calls
     */
    public void call(){
        if (fragmentView != null) {
            Button callApp = fragmentView.findViewById(R.id.phone);
            callApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri number = Uri.parse("tel:5551234");
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
                }
            });
        }
    }

    /**
     * camera() calls  the camera App when the User press the camera button
     */
    public void camera(){
        if (fragmentView != null) {
            Button cameraApp = fragmentView.findViewById(R.id.camera);
            cameraApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cameraCheck()) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(cameraIntent);
                    }
                }
            });
        }
    }


    /**
     * Checks if app has user permission to device camera
     * returns true if Permission to use Camera is allowed
     */
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public boolean cameraCheck(){
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return true;
            }
        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    /**
     *
     */

    /**
     * Carries the timer for how long the phone is locked
     * Reads settings and changes how long the phone will be locked for
     *
     */
    public void unlockPhone(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String syncConnPref = sharedPref.getString("lockout_time", "12");
        Log.d("Settings", syncConnPref );
        int time = Integer.parseInt(syncConnPref) * 3600000;

        //Delays the reveal of the exit button
        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                String hms = String.format("Time remaining: %02dH:%02dM:%02dS", TimeUnit.MILLISECONDS.toHours(millisUntilFinished), TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                timeView.setText(hms);
            }

            public void onFinish() {
                timeView.setText("No more time remaing");
                unLock();

            }
        }.start();
    }

    /**
     *
     * Exit way for when timer runs out
     * Butt on is revealed for unlocking Kiosk Mode
     */
    public void unLock(){
        Button hiddenExit = fragmentView.findViewById(R.id.exitButton);
        hiddenExit.setVisibility(View.VISIBLE);
        hiddenExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Break out!
                PrefUtils.setKioskModeActive(false, getActivity().getApplicationContext());
                Toast.makeText(getActivity().getApplicationContext(),"You can leave the app now!", Toast.LENGTH_LONG).show();
                //Makes the home package no longer
                //PackageManager pm = getActivity().getPackageManager();
                //pm.clearPackagePreferredActivities ("com.iruss.mogivisions.kiosk");
            }
        });
    }
}
