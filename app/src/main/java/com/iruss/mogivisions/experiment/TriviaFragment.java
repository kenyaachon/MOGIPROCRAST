package com.iruss.mogivisions.experiment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TriviaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TriviaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TriviaFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private View fragmentView;

    private ArrayList<TriviaQuestion> triviaQuestions;

    private TriviaQuestion triviaQuestion;

    private TextView questionView;
    //Response buttons with questions
    private Button questionResponse1;
    private Button questionResponse2;
    private Button questionResponse3;
    private Button questionResponse4;


    //Number of trials possible
    private static final int trials = 3;

    private TextView trialsView;

    //Number of trials remaining
    private int attemptsMade = 0;

    public TriviaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ActivityFragment.
     */
    public static TriviaFragment newInstance() {
        TriviaFragment fragment = new TriviaFragment();
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
        fragmentView = inflater.inflate(R.layout.fragment_trivia, container, false);

//        fragmentView.findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        questionView = fragmentView.findViewById(R.id.questionText);
        //Response buttons with questions
        questionResponse1 = fragmentView.findViewById(R.id.questionResponse1);
        questionResponse2 = fragmentView.findViewById(R.id.questionResponse2);
        questionResponse3 = fragmentView.findViewById(R.id.questionResponse3);
        questionResponse4 = fragmentView.findViewById(R.id.questionResponse4);

        //make the buttons not visible until the buttons are ready
        questionView.setVisibility(View.GONE);
        questionResponse1.setVisibility(View.GONE);
        questionResponse2.setVisibility(View.GONE);
        questionResponse3.setVisibility(View.GONE);
        questionResponse4.setVisibility(View.GONE);

        //Gets the text that shows how many trials you have
        trialsView = fragmentView.findViewById(R.id.trialsRemaining);

        //call the trivia Api
        //TriviaAPI triviaAPI = new TriviaAPI(this);
        new TriviaAPI(this);
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

    /*
     * A method to create the challenge
     * display the question and possible resonses in the TriviaActivity
     */
    // TODO: Make a test for this method
    public boolean displayQuestions(ArrayList<TriviaQuestion> triviaQuestions){
        this.triviaQuestions = triviaQuestions;
        //Randomly select question
        Random randomizer = new Random();
        //randomly gets the next question
        triviaQuestion = triviaQuestions.get(randomizer.nextInt(triviaQuestions.size()));

        //settings the question to be displayed
        questionView.setText(triviaQuestion.getQuestion());
        questionView.setVisibility(View.VISIBLE);
        ArrayList<String> responses = new ArrayList<>();
        responses.addAll(triviaQuestion.getIncorrectAnswers());

        //randomly adding the correct answer into the list of possible answers
        int randomposition = randomizer.nextInt(responses.size() + 1);
        responses.add(randomposition, triviaQuestion.getCorrectAnswer());

        //always sets the responses for the first 2 buttons
        questionResponse1.setText(responses.get(0));
        questionResponse2.setText(responses.get(1));


        //Sets the texts of the button to the possible responses for the user to choose
        //Checks how many possible responses there are and then displays the same amount of response buttons
        if(responses.size() == 2){
            questionResponse1.setVisibility(View.VISIBLE);
            questionResponse2.setVisibility(View.VISIBLE);
        }else if (responses.size() == 3){
            questionResponse3.setText(responses.get(2));
            questionResponse3.setVisibility(View.GONE);
        }else if (responses.size() == 4) {
            questionResponse3.setText(responses.get(2));
            questionResponse4.setText(responses.get(3));
            questionResponse3.setVisibility(View.VISIBLE);
            questionResponse4.setVisibility(View.VISIBLE);

        }
        //sets the Clicklistener for all the buttons
        questionResponse1.setOnClickListener(myClickListener);
        questionResponse2.setOnClickListener(myClickListener);
        questionResponse3.setOnClickListener(myClickListener);
        questionResponse4.setOnClickListener(myClickListener);

        return true;
    }

    //Button click Listener for check the response to a users click
    // TODO: Make a test for this method
    private final View.OnClickListener myClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Button tempButton = fragmentView.findViewById(view.getId());
            if(tempButton.getText().equals(triviaQuestion.getCorrectAnswer())){
                Log.d("Test", "Correct response chosen");
            }
            else {
                Log.d("Test", "Incorrect response chosen");
                if(attemptsMade == trials){
                    //call kill

                    //display a message to user that they are out of attempts and go back to KioskActivity
                    Log.d("Test", "You are out of attempts");
                    if (mListener != null && mListener instanceof KioskActivity) {
                        KioskActivity kioskActivity = (KioskActivity) mListener;
                        kioskActivity.loadKiosk();
                    }
                }else{
                    displayQuestions(triviaQuestions);
                    attemptsMade += 1;
                    String trialsStr = "Trials remaining: ";
                    int displayTrials = trials - attemptsMade;
                    trialsStr.concat(Integer.toString(displayTrials));
                    trialsView.setText(trialsStr);
                }
            }
        }
    };

}
