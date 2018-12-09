package anki.dialogs;

import android.content.res.Resources;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.iruss.mogivisions.procrastimatev1.R;

import anki.BackupManager;
import anki.CollectionHelper;
import anki.DeckPicker;
import anki.analytics.AnalyticsDialogFragment;

public class DeckPickerBackupNoSpaceLeftDialog extends AnalyticsDialogFragment {
    public static DeckPickerBackupNoSpaceLeftDialog newInstance() {
        DeckPickerBackupNoSpaceLeftDialog f = new DeckPickerBackupNoSpaceLeftDialog();
        return f;        
    }
    
    @Override
    public MaterialDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        long space = BackupManager.getFreeDiscSpace(CollectionHelper.getCollectionPath(getActivity()));
        return new MaterialDialog.Builder(getActivity())
                .title(res.getString(R.string.sd_card_almost_full_title))
                .content(res.getString(R.string.sd_space_warning, space/1024/1024))
                .positiveText(res.getString(R.string.dialog_ok))
                .onPositive((dialog, which) -> ((DeckPicker) getActivity()).finishWithoutAnimation())
                .cancelable(true)
                .cancelListener(dialog -> ((DeckPicker) getActivity()).finishWithoutAnimation())
                .show();
    }
}