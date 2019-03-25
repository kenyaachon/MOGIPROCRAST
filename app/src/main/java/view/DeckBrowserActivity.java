package view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.iruss.mogivisions.procrastimatev1.R;

import java.util.ArrayList;
import java.util.List;

import model.Card;
import model.Deck;


/**
 * The deck browser is a tool to search for specific cards and to modify/delete them easily. All
 * cards are presented in a list view. It is also possible to shuffle the deck and to reset the
 * levels of the cards.
 */
public class DeckBrowserActivity extends AppCompatActivity {

    private static final int MAX_RESULTS = 200;

    private String deckName;
    private Deck deck;

    private ArrayAdapter<Card> cardAdapter;
    private List<Card> cards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_browser);

        ListView cardList = findViewById(R.id.card_list);
        cardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cards);
        cardList.setAdapter(cardAdapter);

        customTitle();
        // normal click: edit
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cards.get(position);
                final Dialog dialog = new Dialog(DeckBrowserActivity.this);
                dialog.setContentView(R.layout.card_dialog);
                dialog.setTitle(getString(R.string.edit_card));
                final EditText frontEdit = dialog.findViewById(R.id.edit_front);
                frontEdit.setText(card.getFront());
                final EditText backEdit = dialog.findViewById(R.id.edit_back);
                backEdit.setText(card.getBack());
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
                            card.edit(front, back);
                            deck.saveDeck();
                            cardAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        // long click: delete
        cardList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cardAdapter.getItem(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(DeckBrowserActivity.this,  R.style.FlashcardDialogStyle);
                dialog.setTitle(getString(R.string.delete_card));
                dialog.setMessage(getString(R.string.really_delete_card));
                dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(deck.deleteCard(card)) {
                            cards.remove(position);
                            cardAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.cannot_delete_last_card),
                                    Toast.LENGTH_SHORT).show();
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
                return true;
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        deckName = getIntent().getStringExtra("deck name");
        setTitle(deckName);
    }

    public void customTitle(){

        //Allows for a custome title to be used
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.flashcardstitle);

        //Goes back to home activity
        ImageButton settingsButton = findViewById(R.id.returnHome);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(DeckBrowserActivity.this, DeckListActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        /*
        try {
            deck = Deck.loadDeck(deckName);
        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }*/
        deck = Deck.loadDeck(deckName);


        displayCardList("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deckbrowseractivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FlashcardDialogStyle);
            builder.setTitle(getString(R.string.search_card));
            final EditText searchTermInput = new EditText(this);
            searchTermInput.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(searchTermInput);
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    displayCardList(searchTermInput.getText().toString().trim());
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (item.getItemId() == R.id.action_add) {
            final Dialog dialog = new Dialog(DeckBrowserActivity.this);
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
                    else if (back.length() == 0)
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                    else {
                        Card card = new Card(front, back);
                        deck.addNewCard(card);
                        displayCardList("");
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        } else if(item.getItemId() == R.id.action_shuffle){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FlashcardDialogStyle);
            builder.setTitle(getString(R.string.shuffle_deck));
            builder.setMessage(getString(R.string.really_shuffle_deck));
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deck.shuffleDeck();
                    displayCardList("");
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if(item.getItemId() == R.id.action_reverse){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FlashcardDialogStyle);
            builder.setTitle(getString(R.string.reverse_deck));
            builder.setMessage(getString(R.string.really_reverse_deck));
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deck.reverseDeck();
                    displayCardList("");
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if(item.getItemId() == R.id.action_reset){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.FlashcardDialogStyle);
            builder.setTitle(getString(R.string.reset_card_strength));
            builder.setMessage(getString(R.string.really_reset_strength));
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNeutralButton(getString(R.string.yes_level_0), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deck.resetStrength(0);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(getString(R.string.yes_level_2), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    deck.resetStrength(2);
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if(item.getItemId() == R.id.action_share){
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TITLE, R.string.share_deck);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getResources().getString(R.string.shared_stacksrs_deck, deck.getName()));
            sharingIntent.putExtra(Intent.EXTRA_TEXT,
                    deck.getDeckContentAsCsvString());
            startActivity(Intent.createChooser(sharingIntent,
                    getResources().getString(R.string.share_deck)));
        }
        return true;
    }

    private void displayCardList(String searchTerm){
        cards.clear();
        cards.addAll(deck.searchCards(searchTerm, MAX_RESULTS));
        cardAdapter.notifyDataSetChanged();
    }
}
