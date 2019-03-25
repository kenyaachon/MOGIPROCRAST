package view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iruss.mogivisions.procrastimatev1.R;

import java.io.IOException;
import java.util.Locale;

import model.Card;
import model.Deck;
import model.DeckCollection;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * The ReviewActivity is the activity where the actual learning takes place.
 * The procedure is the following: First, the front of the card is shown and the user has to figure
 * out the answer. Then he clicks on the "Show Answer" button, the answer is shown and the user can
 * decide for himself if his answer was correct enough. After that, the card is put back in the deck
 * and the next card is shown.
 */
public class ReviewActivity extends AppCompatActivity {

    private TextView frontText;
    private TextView backText;
    private Button wrongButton;
    private Button answerButton;
    private Button speakButton;
    private Button correctButton;

    private String deckName;
    private Deck deck;

    private TextToSpeech tts;

    private SharedPreferences mprefs;

    private TextView warning;
    private TextView warningsymbol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        //firstTimeUser();
        warning = findViewById(R.id.warning);
        warningsymbol = findViewById(R.id.warningSymbol);

        frontText = findViewById(R.id.text_front);
        backText = findViewById(R.id.text_back);
        wrongButton = findViewById(R.id.button_wrong);
        answerButton = findViewById(R.id.button_answer);
        speakButton = findViewById(R.id.button_speak);
        correctButton = findViewById(R.id.button_correct);

        wrongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deck.putReviewedCardBack(false);
                showNextCard();
            }
        });
        correctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deck.putReviewedCardBack(true);
                showNextCard();
            }
        });
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBack();
            }
        });
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakWord(backText.getText().toString());
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        deckName = getIntent().getStringExtra("deck name");

        customTitle();

    }

    public void customTitle(){

        //Allows for a custome title to be used
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.flashcardstitle);

        //Goes back to home activity
        ImageButton settingsButton = findViewById(R.id.returnHome);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(ReviewActivity.this, DeckListActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Displays first time User
     */
    public void firstTimeUser(){
        mprefs = this.getSharedPreferences("Flashcard", Context.MODE_PRIVATE);


        SharedPreferences.Editor editor = mprefs.edit();
        //make sure the firstTime preference is only created once
        if(mprefs.getBoolean("firstTimeReview", true)) {
            Log.i("Tutorial", "Creating first time setting" );
            editor.putBoolean("firstTimeReview", true).apply();
        }

        Log.i("Tutorial", Boolean.toString(mprefs.getBoolean("firstTimeReview", true)));

        if(mprefs.getBoolean("firstTimeReview", true)){
            //turn off the tutorial after it is displayed
            editor.putBoolean("firstTimeReview", false).apply();
            new MaterialTapTargetPrompt.Builder(ReviewActivity.this)
                    .setTarget(R.id.settings)
                    .setBackgroundColour(getResources().getColor(R.color.settingTitleBackgroundcolor))
                    .setFocalColour(getResources().getColor(R.color.tutorialFocalColor))
                    .setPrimaryText("Add more cards")
                    .setSecondaryText("Tap the button to open the menu so you can add more cards")
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                // User has pressed the prompt target
                                Log.i("Tutorial", "User has pressed the prompt target");
                            }
                            //userStats();

                        }
                    })
                    .show();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        reloadDeck();
    }


    private void reloadDeck(){
        setTitle(deckName);

        deck = Deck.loadDeck(deckName);
        if(!deck.isUsingTTS() && !deck.getLanguage().equals("") && deck.isNew())
            askForTTSActivation();
        if(deck.isUsingTTS())
            initTTS();
        showNextCard();
        deckStatus();

        /*
        try {

            deck = Deck.loadDeck(deckName);
            if(!deck.isUsingTTS() && !deck.getLanguage().equals("") && deck.isNew())
                askForTTSActivation();
            if(deck.isUsingTTS())
                initTTS();
            showNextCard();

            /*
            if(deck.getDeckStackSize() >= 20){
                warning.setText("DECK is ELIGIBLE for being used in trivia challenge");
                warningsymbol.setBackground(getDrawable(R.drawable.ic_check_circle_black_24dp));
            }
            deckStatus();

        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }*/

    }

    public void deckStatus(){
        if(deck.getDeckStackSize() >= 20){
            warning.setText("DECK is ELIGIBLE for being used in trivia challenge");
            warning.setGravity(Gravity.CENTER);
            warningsymbol.setBackground(getDrawable(R.drawable.ic_check_circle_black_24dp));
        } else{
            int left = 20 - deck.getDeckStackSize();
            warning.setText("You need at least " + left + " cards more for this deck to be a valid deck");
        }
    }

    private void askForTTSActivation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FlashcardDialogStyle);
        builder.setTitle(getString(R.string.activate_tts_new_deck));
        builder.setMessage(getString(R.string.want_activate_tts));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deck.activateTTS();
                initTTS();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initTTS(){
        final Locale locale = getLocaleForTTS();
        if(locale != null){
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(locale);
                    }
                }
            });
        }
    }

    private Locale getLocaleForTTS(){
        String lang = deck.getLanguage();
        if(lang == null || lang.equals(""))
            return null;
        String country = deck.getAccent();
        if(country == null || country.equals(""))
            return new Locale(lang);
        return new Locale(lang, country);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(deck != null){
            deck.saveDeck();
        }
        if(tts != null){
            tts.shutdown();
            tts = null;
        }
    }

    private void showNextCard(){
        frontText.setText(deck.getNextCardToReview().getFront());
        backText.setText("");
        wrongButton.setVisibility(View.GONE);
        correctButton.setVisibility(View.GONE);
        answerButton.setVisibility(View.VISIBLE);
        speakButton.setVisibility(View.GONE);
    }

    private void showBack(){
        String back = deck.getNextCardToReview().getBack();
        backText.setText(deck.getNextCardToReview().getBack());
        wrongButton.setVisibility(View.VISIBLE);
        correctButton.setVisibility(View.VISIBLE);
        answerButton.setVisibility(View.GONE);

        if(deck.isUsingTTS()) {
            speakButton.setVisibility(View.VISIBLE);
            speakWord(back);
        }
    }

    private void speakWord(String text){
        if(tts == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reviewactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add){
            final Dialog dialog = new Dialog(ReviewActivity.this);
            dialog.setContentView(R.layout.card_dialog);
            dialog.setTitle(getString(R.string.add_new_card));
            final EditText frontEdit = dialog.findViewById(R.id.edit_front);
            final EditText backEdit = dialog.findViewById(R.id.edit_back);
            Button cancelButton = dialog.findViewById(R.id.button_cancel);
            Button okButton = dialog.findViewById(R.id.button_ok);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String front = frontEdit.getText().toString().trim();
                    String back = backEdit.getText().toString().trim();
                    if (front.length() == 0)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                    else if(back.length() == 0)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                    else {
                        deckStatus();

                        deck.addNewCard(new Card(front, back));
                        showNextCard();
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        } else if(item.getItemId() == R.id.action_edit){
            final Dialog dialog = new Dialog(ReviewActivity.this);
            dialog.setContentView(R.layout.card_dialog);
            dialog.setTitle(getString(R.string.edit_current_card));
            final EditText frontEdit = dialog.findViewById(R.id.edit_front);
            frontEdit.setText(deck.getNextCardToReview().getFront());
            final EditText backEdit = dialog.findViewById(R.id.edit_back);
            backEdit.setText(deck.getNextCardToReview().getBack());
            Button cancelButton = dialog.findViewById(R.id.button_cancel);
            Button okButton = dialog.findViewById(R.id.button_ok);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String front = frontEdit.getText().toString().trim();
                    String back = backEdit.getText().toString().trim();
                    if (front.length() == 0)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                    else if(back.length() == 0)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                    else {
                        deck.editCurrentCard(front, back);
                        showNextCard();
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        } else if(item.getItemId() == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this, R.style.FlashcardDialogStyle);
            builder.setTitle(getString(R.string.delete_current_card));
            builder.setMessage(getString(R.string.really_delete_card));
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    boolean successful = deck.deleteCurrentCard();
                    if(!successful)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.cannot_delete_last_card), Toast.LENGTH_SHORT)
                                .show();
                    showNextCard();
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if(item.getItemId() == R.id.action_browser){
            Intent intent = new Intent(getApplicationContext(), DeckBrowserActivity.class);
            intent.putExtra("deck name", deck.getName());
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_options) {

            final Dialog dialog = new Dialog(ReviewActivity.this);
            dialog.setContentView(R.layout.deck_dialog);
            dialog.setTitle(getString(R.string.deck_options));

            final EditText editDeckName = dialog.findViewById(R.id.edit_deck_name);
            editDeckName.setText(deckName);
            final EditText editLanguage = dialog.findViewById(R.id.edit_language);
            editLanguage.setText(deck.getLanguage());
            final EditText editAccent = dialog.findViewById(R.id.edit_accent);
            editAccent.setText(deck.getAccent());
            final CheckBox checkBoxTTS = dialog.findViewById(R.id.checkbox_tts);
            checkBoxTTS.setChecked(deck.isUsingTTS());
            Button cancelButton = dialog.findViewById(R.id.button_cancel);
            Button okButton = dialog.findViewById(R.id.button_ok);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeckCollection deckCollection = new DeckCollection();
                    try {
                        deckCollection.reload(DeckCollection.stackSRSDir);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    String newDeckName = editDeckName.getText().toString().trim();
                    boolean deckNameChanged = !newDeckName.equals(deckName);
                    if (deckCollection.isIllegalDeckName(newDeckName)) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.illegal_deck_name), Toast.LENGTH_SHORT).show();
                    } else if(deckNameChanged && deckCollection.deckWithNameExists(newDeckName)){
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.deck_already_exists, newDeckName), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        if(deckNameChanged) {
                            deck.changeName(newDeckName);
                            deckName = newDeckName;
                        }
                        deck.setLanguage(editLanguage.getText().toString().trim().toLowerCase());
                        deck.setAccent(editAccent.getText().toString().trim().toUpperCase());
                        if(checkBoxTTS.isChecked())
                            deck.activateTTS();
                        else
                            deck.deactivateTTS();
                        deck.saveDeck();

                        reloadDeck();
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            dialog.getWindow().setLayout((6 * width)/7, (4 * height)/9);
        }
        return super.onOptionsItemSelected(item);
    }


}
