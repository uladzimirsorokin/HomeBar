package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;

public class AboutFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_TEXT = "extra_text";
    private String mAboutText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_about, container, false);

        if (getArguments() != null) {
            mAboutText = getArguments().getString(EXTRA_TEXT);
        }

        initToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    private void initViews(View view) {
        TextView mTvAbout = view.findViewById(R.id.tv_about);
        if (mAboutText != null) mTvAbout.setText(mAboutText);
    }

    public static AboutFragment getNewInstance(String name, String aboutText) {
        AboutFragment fragment = new AboutFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putString(EXTRA_TEXT, aboutText);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitle(R.string.menu_about);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider)getParentFragment()).getRouter().exit();
        }
        return true;
    }

}
