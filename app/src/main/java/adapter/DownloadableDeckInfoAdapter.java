package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iruss.mogivisions.procrastimatev1.R;

import java.util.List;

import model.DownloadableDeckInfo;


/**
 * Adapter class to display deck info (name, description) in the list view of DeckDownloadActivity
 */
public class DownloadableDeckInfoAdapter extends ArrayAdapter<DownloadableDeckInfo> {

    private List<DownloadableDeckInfo> deckInfoList;
    private Context context;

    public DownloadableDeckInfoAdapter(Context context, List<DownloadableDeckInfo> deckInfoList){
        super(context, R.layout.item_downloadable_deck_info, deckInfoList);
        this.context = context;
        this.deckInfoList = deckInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DownloadableDeckInfo deckInfo = deckInfoList.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View deckInfoView = inflater.inflate(R.layout.item_downloadable_deck_info, parent, false);

        TextView viewName = deckInfoView.findViewById(R.id.view_name);
        viewName.setText(deckInfo.getName());
        TextView viewDescription = deckInfoView.findViewById(R.id.view_description);
        viewDescription.setText(deckInfo.getDescription());

        return deckInfoView;
    }
}
