
package anki.dialogs;

import android.os.Bundle;
import android.os.Message;

import com.afollestad.materialdialogs.MaterialDialog;
import com.iruss.mogivisions.procrastimatev1.R;

import java.io.File;

import anki.DeckPicker;
import compat.CompatHelper;

public class DeckPickerExportCompleteDialog extends AsyncDialogFragment {
    
    public static DeckPickerExportCompleteDialog newInstance(String exportPath) {
        DeckPickerExportCompleteDialog f = new DeckPickerExportCompleteDialog();
        Bundle args = new Bundle();
        args.putString("exportPath", exportPath);
        f.setArguments(args);
        return f;
    }


    @Override
    public MaterialDialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String exportPath = getArguments().getString("exportPath");
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getActivity())
                .title(getNotificationTitle())
                .content(getNotificationMessage())
                .iconAttr(R.attr.dialogSendIcon)
                .positiveText(R.string.export_send_button)
                .negativeText(R.string.dialog_cancel)
                .onPositive((dialog, which) -> {
                    ((DeckPicker) getActivity()).dismissAllDialogFragments();
                    ((DeckPicker) getActivity()).emailFile(exportPath);
                })
                .onNegative((dialog, which) -> ((DeckPicker) getActivity()).dismissAllDialogFragments());

        // If they have the system storage framework, allow for a save option
        if (CompatHelper.getSdkVersion() >= 19) {
            dialogBuilder
                    .neutralText(R.string.dialog_cancel)
                    .onNeutral((dialog, which) -> ((DeckPicker) getActivity()).dismissAllDialogFragments())
                    .negativeText(R.string.export_save_button)
                    .onNegative((dialog, which) -> {
                        ((DeckPicker) getActivity()).dismissAllDialogFragments();
                        ((DeckPicker) getActivity()).saveExportFile(exportPath);
                    });
        }

        return dialogBuilder.show();
    }
    
    public String getNotificationTitle() {
        return res().getString(R.string.export_successful_title);
    }


    public String getNotificationMessage() {
        File exportPath = new File(getArguments().getString("exportPath"));
        return res().getString(R.string.export_successful, exportPath.getName());
    }


    @Override
    public Message getDialogHandlerMessage() {
        Message msg = Message.obtain();
        msg.what = DialogHandler.MSG_SHOW_EXPORT_COMPLETE_DIALOG;
        Bundle b = new Bundle();
        b.putString("exportPath", getArguments().getString("exportPath"));
        msg.setData(b);
        return msg;
    } 
}
