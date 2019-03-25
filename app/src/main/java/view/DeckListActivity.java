package view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.iruss.mogivisions.procrastimatev1.HomeActivity;
import com.iruss.mogivisions.procrastimatev1.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapter.DeckInfoAdapter;
import cz.msebera.android.httpclient.Header;
import model.Card;
import model.Deck;
import model.DeckCollection;
import model.DownloadableDeckInfo;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * The DeckListActivity is the entry screen of StackSRS and shows all decks in a list view. Next to
 * the name of each deck, the total number of cards (blue), the number of remaining cards to learn
 * (red) and the number of cards already mastered (green) is displayed.
 */
public class DeckListActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private DeckInfoAdapter deckListAdapter;
    private DeckCollection deckCollection = new DeckCollection();

    private FloatingActionButton newButton;
    //private Button downloadButton;

    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    //Item position
    private int listItemPosition;
    private Deck deck;


    //Download extra decks
    private final String SERVER_URL = "https://stacksrs.droppages.com/";

    private List<DownloadableDeckInfo> deckNames = new ArrayList<>();

    private AsyncHttpClient httpClient = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_list);

        customTitle();


        mprefs = this.getSharedPreferences("Flashcard", Context.MODE_PRIVATE);
        editor = mprefs.edit();
        firstTimeUser();



        final ListView deckListView = findViewById(R.id.deck_list);
        deckListAdapter = new DeckInfoAdapter(this, deckCollection.getDeckInfos());
        deckListView.setAdapter(deckListAdapter);


        // normal click: open deck

        deckListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // switch to download activity
                Log.i("Deck", Integer.toString(position));
                Log.i("Deck collection", Integer.toString(deckListAdapter.getCount()));

                String deckName = deckListAdapter.getItem(position).getName();
                editor.putString("DeckName", deckName).apply();

                Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
                intent.putExtra("deck name", deckName);
                startActivity(intent);
            }
        });

        // long click: delete deck
        deckListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPopupMenu(view, position);
                return true;
            }
        });

        newButton = findViewById(R.id.button_new);
        //downloadButton = (Button) findViewById(R.id.button_download);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewDeckDialog();
            }
        });

        /*
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switch to download activity
                Intent intent = new Intent(getApplicationContext(), DeckDownloadActivity.class);
                startActivity(intent);
            }
        });*/

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Displays first time User
     */
    public void firstTimeUser(){

        SharedPreferences.Editor editor = mprefs.edit();
        //make sure the firstTime preference is only created once
        if(mprefs.getBoolean("firstTimeFlashcards", true)) {
            Log.i("Tutorial", "Creating first time setting" );
            editor.putBoolean("firstTimeFlashcards", true).apply();
        }

        //Call Usage statistics
        Log.i("Tutorial", Boolean.toString(mprefs.getBoolean("firstTimeFlashcards", true)));

        if(mprefs.getBoolean("firstTimeFlashcards", true)){
            //downloadDeckList();


            //turn off the tutorial after it is displayed
            editor.putBoolean("firstTimeFlashcards", false).apply();
            new MaterialTapTargetPrompt.Builder(DeckListActivity.this)
                    .setTarget(R.id.button_new)
                    .setBackgroundColour(getResources().getColor(R.color.settingTitleBackgroundcolor))
                    .setFocalColour(getResources().getColor(R.color.tutorialFocalColor))
                    .setPrimaryText("Create a new deck")
                    .setSecondaryText("Lets create a new deck")
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

    public void deleteDeck(int position){
        final String deckName = deckListAdapter.getItem(position).getName();
        AlertDialog.Builder dialog = new AlertDialog.Builder(DeckListActivity.this, R.style.FlashcardDialogStyle);
        dialog.setTitle(getString(R.string.delete_deck));
        dialog.setMessage(getString(R.string.really_delete_deck, deckName));
        dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deckCollection.deleteDeckFile(deckName);
                reloadDeckList();
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    public void customTitle(){

        //Allows for a custome title to be used
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.flashcardstitle);

        //Goes back to home activity
        ImageButton settingsButton = findViewById(R.id.returnHome);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(DeckListActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        reloadDeckList();
    }

    public void reloadDeckList() {
        try {
            File stackSRSDir = provideStackSRSDir();
            deckCollection.reload(stackSRSDir);
        } catch(IOException e){
            Toast.makeText(this, getString(R.string.collection_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
        }
        deckListAdapter.notifyDataSetChanged();
    }


    private File provideStackSRSDir(){
        // if there is (possibly emulated) external storage available, we use it
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return getApplicationContext().getExternalFilesDir(null);
        } else { // otherwise we use an internal directory without access from the outside
            return getApplicationContext().getDir("StackSRS", MODE_PRIVATE);
        }
    }

    public void showNewDeckDialog(){
        final Dialog dialog = new Dialog(DeckListActivity.this);
        dialog.setContentView(R.layout.deck_dialog);
        dialog.setTitle(getString(R.string.new_deck));
        final EditText editDeckName = dialog.findViewById(R.id.edit_deck_name);
        final EditText editLanguage = dialog.findViewById(R.id.edit_language);
        final EditText editAccent = dialog.findViewById(R.id.edit_accent);
        final CheckBox checkBoxTTS = dialog.findViewById(R.id.checkbox_tts);
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
                String deckName = editDeckName.getText().toString().trim();
                if (deckCollection.isIllegalDeckName(deckName)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.illegal_deck_name), Toast.LENGTH_SHORT).show();
                } else if(deckCollection.deckWithNameExists(deckName)){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.deck_already_exists, deckName), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Deck newDeck = new Deck(deckName, "");
                    newDeck.addNewCard(new Card("default", "default", 2));
                    newDeck.setLanguage(editLanguage.getText().toString().trim().toLowerCase());
                    newDeck.setAccent(editAccent.getText().toString().trim().toUpperCase());
                    if(checkBoxTTS.isChecked())
                        newDeck.activateTTS();
                    newDeck.saveDeck();
                    reloadDeckList();
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

    /**
     * If deck is chosen for trivia, its names is saved and will be retrieved later when flashcard page is created
     * @param position, the item in the listView that was chosen
     */
    public void selectDeckForTrivia(final int position){
        final String deckName = deckListAdapter.getItem(position).getName();
        Log.i("DeckListActivity", deckName);
        AlertDialog.Builder dialog = new AlertDialog.Builder(DeckListActivity.this, R.style.FlashcardDialogStyle);
        dialog.setTitle(getString(R.string.delete_deck));
        dialog.setMessage(getString(R.string.really_delete_deck, deckName));
        dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //deckCollection.deleteDeckFile(deckName);
                //reloadDeckList();
                if(Integer.parseInt(deckListAdapter.getItem(position).getNumCards()) >= 20){
                    editor.putString("DeckName", deckName).apply();
                } else{
                    editor.putString("DeckName", "Nothing").apply();
                    Toast.makeText(DeckListActivity.this.getApplicationContext(),
                            "Cannot use this deck because it has less than 20 cards",
                            Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    public void renameDeck(int position){

        final String deckName = deckListAdapter.getItem(position).getName();

        /*
        try{
            deck = Deck.loadDeck(deckName);

        }catch (IOException e){
            e.printStackTrace();
        }*/
        deck = Deck.loadDeck(deckName);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.deck_dialog);
        dialog.setTitle(getString(R.string.deck_options));
        final EditText editDeckName = dialog.findViewById(R.id.edit_deck_name);
        editDeckName.setText(deckName);
        final EditText editLanguage = dialog.findViewById(R.id.edit_language);
        editLanguage.setVisibility(View.GONE);
        final EditText editAccent =  dialog.findViewById(R.id.edit_accent);
        editAccent.setVisibility(View.GONE);
        final TextView labelAccent = dialog.findViewById(R.id.label_accent);
        labelAccent.setVisibility(View.GONE);
        final TextView labelLanguage = dialog.findViewById(R.id.label_language);
        labelLanguage.setVisibility(View.GONE);

        final CheckBox checkBoxTTS = dialog.findViewById(R.id.checkbox_tts);
        checkBoxTTS.setVisibility(View.GONE);
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
                        //deckName = newDeckName;
                    }

                    reloadDeckList();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dialog.getWindow().setLayout((6 * width)/7, (2 * height)/9);

    }

    public void showPopupMenu(View v, int position){
        listItemPosition = position;

        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.deck_study_options);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){

        switch (item.getItemId()){
            case R.id.deleteMenuItem:
                deleteDeck(listItemPosition);
                return true;
            case R.id.renameMenuItem:
                renameDeck(listItemPosition);
                return true;
            case R.id.selectForTriviaMenuItem:
                selectDeckForTrivia(listItemPosition);
                return true;
            default:
                return false;
        }
    }


    public void downloadDeckList() {
        httpClient.get(SERVER_URL + "decks.txt", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                deckNames.clear();
                try {
                    JSONArray deckListArray = response.getJSONArray("decks");
                    for (int i = 0; i < deckListArray.length(); ++i) {
                        DownloadableDeckInfo deckInfo = new DownloadableDeckInfo(
                                deckListArray.getJSONObject(i));
                        //deckNames.add(deckInfo);
                        downloadDeck(deckInfo.getFile(), 2);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.could_not_load_deck_list_from_server),
                            Toast.LENGTH_SHORT).show();
                }
                deckListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.could_not_connect_to_server),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadDeck(final String file, final int level) {
        httpClient.get(SERVER_URL + file + ".txt", null, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String deckName = response.getString("name");
                    Deck newDeck = new Deck(deckName, response.getString("back"));
                    JSONArray cardArray = response.getJSONArray("cards");
                    List<Card> cards = new ArrayList<>();
                    for(int i = 0; i < cardArray.length(); ++i) {
                        JSONObject cardObject = cardArray.getJSONObject(i);
                        Card c = new Card(cardObject.getString("front"),
                                cardObject.getString("back"), level);
                        cards.add(c);
                    }
                    newDeck.fillWithCards(cards);
                    newDeck.saveDeck();

                    Log.i("DeckListActivity deck:", deckName);
                } catch(JSONException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.could_not_load_deck),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                Toast.makeText(getApplicationContext(), getString(R.string.downloading_deck_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
