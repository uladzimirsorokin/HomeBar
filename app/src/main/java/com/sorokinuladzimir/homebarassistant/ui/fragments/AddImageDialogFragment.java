package com.sorokinuladzimir.homebarassistant.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;

import java.util.Objects;


public class AddImageDialogFragment extends DialogFragment {

    public static AddImageDialogFragment newInstance(String title, Boolean allowDelete) {
        AddImageDialogFragment frag = new AddImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.Extra.TITLE, title);
        args.putBoolean(Constants.Extra.ALLOW_DELETE, allowDelete);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = Objects.requireNonNull(getArguments()).getString(Constants.Extra.TITLE);
        Boolean allowDelete = getArguments().getBoolean(Constants.Extra.ALLOW_DELETE);

        final CharSequence[] items = allowDelete ? new CharSequence[]{getString(R.string.take_photo),
                getString(R.string.choose_from_library), getString(R.string.remove_picture)} :
                new CharSequence[]{getString(R.string.take_photo), getString(R.string.choose_from_library)};

        AddImageDialogFragmentCallback callback =
                (AddImageDialogFragmentCallback) getTargetFragment();

        return new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(title)
                .setItems(items, (dialog, item) -> {
                    if (callback != null) {
                        callback.addImageDialogCallback(item);
                    }
                }).setNegativeButton(R.string.negative_button,
                        (dialog, whichButton) -> dialog.dismiss()
                )
                .create();
    }

    public interface AddImageDialogFragmentCallback {
        void addImageDialogCallback(int item);
    }
}