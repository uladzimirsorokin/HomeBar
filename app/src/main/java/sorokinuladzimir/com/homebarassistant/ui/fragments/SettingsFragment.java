package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.content.Context;
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
import sorokinuladzimir.com.homebarassistant.ui.utils.ThemeUtils;

public class SettingsFragment extends Fragment implements BackButtonListener, View.OnClickListener {

    private static final String EXTRA_NAME = "extra_name";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_settings, container, false);


        initToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    private void initViews(View view) {
        View first = view.findViewById(R.id.first_theme);
        View second = view.findViewById(R.id.second_theme);
        View third = view.findViewById(R.id.third_theme);
        first.setOnClickListener(this);
        second.setOnClickListener(this);
        third.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int theme = -1;
        switch (v.getId()) {
            case R.id.first_theme:
                theme = 0;
                break;
            case R.id.second_theme:
                theme = 1;
                break;
            case R.id.third_theme:
                theme = 2;
                break;
        }

        swapTheme(theme);
    }

    private void swapTheme(int theme) {
        int currentTheme = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE).getInt("currentTheme", 0);
        if (currentTheme != theme && theme != -1) ThemeUtils.changeToTheme(getActivity(), theme);
    }


    public static SettingsFragment getNewInstance(String name) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
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
            mToolbar.setTitle(R.string.settings_fragment_title);
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
