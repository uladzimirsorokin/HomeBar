package com.sorokinuladzimir.homebarassistant.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.ui.ThemeItem;
import com.sorokinuladzimir.homebarassistant.ui.adapters.ThemeItemAdapter;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider;
import com.sorokinuladzimir.homebarassistant.ui.utils.ThemeUtils;

public class SettingsFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";

    public static SettingsFragment getNewInstance(String name) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_settings, container, false);
        initToolbar(rootView);
        initViews(rootView);
        return rootView;
    }

    private void initViews(View view) {
        final RecyclerView recyclerView = view.findViewById(R.id.rv_theme);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ThemeItemAdapter adapter = new ThemeItemAdapter(item -> swapTheme(item.getId()));
        adapter.setData(initThemes(Objects.requireNonNull(getActivity())
                .getPreferences(Context.MODE_PRIVATE).getInt(Constants.Extra.APP_THEME, 0)));
        recyclerView.setAdapter(adapter);
    }

    private List<ThemeItem> initThemes(int currentTheme) {
        List<ThemeItem> themes = new ArrayList<>();
        ThemeItem themeItem1 = new ThemeItem(0, getResources().getColor(R.color.indigoDark),
                currentTheme == 0, getString(R.string.main_theme_name));
        ThemeItem themeItem2 = new ThemeItem(1, getResources().getColor(R.color.purpleDark),
                currentTheme == 1, getString(R.string.second_theme_name));
        ThemeItem themeItem3 = new ThemeItem(2, getResources().getColor(R.color.blackDark),
                currentTheme == 2, getString(R.string.third_theme_name));
        themes.add(themeItem1);
        themes.add(themeItem2);
        themes.add(themeItem3);
        return themes;
    }

    private void swapTheme(int theme) {
        int currentTheme = Objects.requireNonNull(getActivity())
                .getPreferences(Context.MODE_PRIVATE).getInt(Constants.Extra.APP_THEME, 0);
        if (currentTheme != theme && theme != -1) ThemeUtils.changeToTheme(getActivity(), theme);
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitle(R.string.settings_fragment_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().exit();
        }
        return true;
    }

}
