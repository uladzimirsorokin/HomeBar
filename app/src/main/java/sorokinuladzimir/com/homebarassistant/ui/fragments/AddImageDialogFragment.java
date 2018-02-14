package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import sorokinuladzimir.com.homebarassistant.R;

public class AddImageDialogFragment extends DialogFragment {

    public interface AddImageDialogFragmentCallback{
        void addImageDialogCallback(int item);
    }

    public static AddImageDialogFragment newInstance(String title, Boolean allowDelete) {
        AddImageDialogFragment frag = new AddImageDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBoolean("allowDelete", allowDelete);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        Boolean allowDelete = getArguments().getBoolean("allowDelete");

        final CharSequence[] items = allowDelete ? new CharSequence[] { "Take Photo", "Choose from Library", "Remove picture"} :
                new CharSequence[]{ "Take Photo", "Choose from Library"};

        AddImageDialogFragmentCallback callback =
                (AddImageDialogFragmentCallback) getTargetFragment();

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setItems(items, (dialog, item) -> {
                    callback.addImageDialogCallback(item);
                }).setNegativeButton("Close",
                        (dialog, whichButton) -> dialog.dismiss()
                )
                .create();
    }
}