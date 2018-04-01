package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.R;


public class AddTastesDialogFragment extends DialogFragment {


    private final ArrayList<Integer> mSelection = new ArrayList<>();

    public interface AddTastesDialogFragmentCallback{
        void addTastesDialogCallback(List<Integer> tastes);
    }

    public static AddTastesDialogFragment newInstance(String title, ArrayList<Integer> mSelection) {
        AddTastesDialogFragment frag = new AddTastesDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putIntegerArrayList("selection", mSelection);
        frag.setArguments(args);
        return frag;
    }

   @NonNull
   @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       String title = null;
       if (getArguments() != null) {
           title = getArguments().getString("title");
       }

       if (savedInstanceState != null) {
            mSelection.clear();
            mSelection.addAll(savedInstanceState.getIntegerArrayList("selection"));
        } else {
            mSelection.clear();
           if (getArguments() != null) {
               mSelection.addAll(getArguments().getIntegerArrayList("selection"));
           }
       }

        AddTastesDialogFragmentCallback callback = (AddTastesDialogFragmentCallback) getTargetFragment();

       String[] tastes = getResources().getStringArray(R.array.taste_name);
       boolean[] selection = new boolean[tastes.length];
        Arrays.fill(selection, false);
        if (mSelection.size() != 0) {
           for (int i : mSelection) {
               selection[i] = true;
           }
        }

        return new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(title)
                .setMultiChoiceItems(tastes, selection, (dialogInterface, item, state) -> {
                    if (!state) {
                        mSelection.remove(mSelection.indexOf(item));
                    } else {
                        mSelection.add(item);
                    }
                }).setNegativeButton(getString(R.string.negative_button),
                        (dialog, whichButton) -> dialog.dismiss()
                ).setPositiveButton(getString(R.string.positive_button),
                        (dialog, whichButton) -> {
                            if (callback != null) {
                                callback.addTastesDialogCallback(mSelection);
                            }
                            dialog.dismiss();
                })
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("selection", mSelection);
    }
}