package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iruss.mogivisions.procrastimatev1.R;

import java.util.List;

import model.DeckInfo;

/**
 * Adapter class to display deck info (name and statistics) in the list view of DeckListActivity
 */
public class DeckInfoAdapter extends ArrayAdapter<DeckInfo> {

    private List<DeckInfo> deckInfoList;
    private Context context;

    public DeckInfoAdapter(Context context, List<DeckInfo> deckInfoList){
        super(context, R.layout.item_deck_info, deckInfoList);
        this.context = context;
        this.deckInfoList = deckInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeckInfo deckInfo = deckInfoList.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View deckInfoView = inflater.inflate(R.layout.item_deck_info, parent, false);

        TextView viewName = deckInfoView.findViewById(R.id.view_name);
        viewName.setText(deckInfo.getName());
        TextView viewNumCards = deckInfoView.findViewById(R.id.view_num_cards);
        viewNumCards.setText(deckInfo.getNumCards());
        TextView viewNumHotCards = deckInfoView.findViewById(R.id.view_num_hot_cards);
        viewNumHotCards.setText(deckInfo.getNumHotCards());
        TextView viewNumKnownCards = deckInfoView.findViewById(R.id.view_num_known_cards);
        viewNumKnownCards.setText(deckInfo.getNumKnownCards());

        return deckInfoView;
    }
}
