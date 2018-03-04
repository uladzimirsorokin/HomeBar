package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;


public class AddTastesDialogFragment extends DialogFragment {

    private SharedViewModel mSharedViewModel;

    private final ArrayList<Integer> mSelection = new ArrayList<>();
    private boolean[] selection;
    private String[] tastes;

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

   @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        if (savedInstanceState != null) {
            mSelection.clear();
            mSelection.addAll(savedInstanceState.getIntegerArrayList("selection"));
        } else {
            mSelection.clear();
            mSelection.addAll(getArguments().getIntegerArrayList("selection"));
        }



        AddTastesDialogFragmentCallback callback = (AddTastesDialogFragmentCallback) getTargetFragment();

        tastes = getResources().getStringArray(R.array.taste_name);
        selection = new boolean[tastes.length];
        Arrays.fill(selection, false);
       if (mSelection.size() != 0) {
           for (int i : mSelection) {
               selection[i] = true;
           }
       }

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMultiChoiceItems(tastes, selection, (dialogInterface, item, state) -> {
                    if (state == false) {
                        mSelection.remove(mSelection.indexOf(item));
                    } else {
                        mSelection.add(item);
                    }
                }).setNegativeButton("Close",
                        (dialog, whichButton) -> dialog.dismiss()
                ).setPositiveButton("Ok",
                        (dialog, whichButton) -> {
                    callback.addTastesDialogCallback(mSelection);
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