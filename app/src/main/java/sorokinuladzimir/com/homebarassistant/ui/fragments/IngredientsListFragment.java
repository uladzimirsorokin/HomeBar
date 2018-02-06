package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;
import sorokinuladzimir.com.homebarassistant.ui.adapters.IngredientsListItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientListViewModel;


/**
 * Created by sorok on 17.10.2017.
 */

public class IngredientsListFragment extends Fragment {

    private static final String EXTRA_NAME = "ilf_extra_name";

    private IngredientsListItemAdapter mAdapter;
    private ActionBar mToolbar;
    private FloatingActionButton mFab;
    private IngredientListViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);


        mViewModel = ViewModelProviders.of(this).get(IngredientListViewModel.class);
        subscribeUi(mViewModel);

        return rootView;
    }


    private void subscribeUi(IngredientListViewModel model) {

        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
            } else {

            }
        });
    }

    public static IngredientsListFragment getNewInstance(String name) {
        IngredientsListFragment fragment = new IngredientsListFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(view1 -> {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT,null);
        });
    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.ingredients_list_toolbar_title);
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsListItemAdapter(ingredient ->
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.LOCAL_INGREDIENT, ingredient.id));
        recyclerView.setAdapter(mAdapter);
    }

}
