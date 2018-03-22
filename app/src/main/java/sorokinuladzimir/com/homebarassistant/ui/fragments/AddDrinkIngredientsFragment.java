package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.adapters.IngredientsListItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddDrinkIngredientsViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;
import android.support.v7.widget.SearchView;

import java.util.ArrayList;


/**
 * Created by sorok on 17.10.2017.
 */

public class AddDrinkIngredientsFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "adif_extra_name";

    private IngredientsListItemAdapter mAdapter;
    private ActionBar mToolbar;
    private FloatingActionButton mFab;
    private AddDrinkIngredientsViewModel mViewModel;
    private SharedViewModel mSharedIngredientsViewModel;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);

        mSharedIngredientsViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        mViewModel = ViewModelProviders.of(this).get(AddDrinkIngredientsViewModel.class);
        subscribeUi(mViewModel, mSharedIngredientsViewModel);

        return rootView;
    }


    private void subscribeUi(AddDrinkIngredientsViewModel model, SharedViewModel sharedIngredients) {

        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setIngredients(ingredients);
            }
        });

        model.getQuery().observe(this, query -> {
            if(query != null) mAdapter.getFilter().filter(query);
        });

        sharedIngredients.getSelectedIds().observe(this, list -> {
            if (list != null && list.size() != 0) {
                if (model.getLocalSelection().getValue() == null || model.getLocalSelection().getValue().size() == 0) {
                    model.setLocalSelection(list);
                }
            }
        });

        model.getLocalSelection().observe(this, list -> {
            if (list != null && list.size() != 0) {
                mAdapter.setSelectedIds(list);
            }
        });
    }

    public static AddDrinkIngredientsFragment getNewInstance(String name) {
        AddDrinkIngredientsFragment fragment = new AddDrinkIngredientsFragment();

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
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitle(R.string.title_choose_ingredients);
        }
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsListItemAdapter(getContext(),getArguments().getString(EXTRA_NAME) , ingredient -> {
            toggleSelection(ingredient);
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void toggleSelection(Ingredient item) {
        mAdapter.toggleSelection(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate( R.menu.add_drink_ingredient_menu, menu);

        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchIngredients(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                mViewModel.searchIngredients(s);
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.setLocalSelection(mAdapter.getSelectedIds());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Drink ingredientrs fragment about text");
        }
        if(item.getItemId() == R.id.ab_add){
            mSharedIngredientsViewModel.selectIds(mAdapter.getSelectedIds());
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }

}
