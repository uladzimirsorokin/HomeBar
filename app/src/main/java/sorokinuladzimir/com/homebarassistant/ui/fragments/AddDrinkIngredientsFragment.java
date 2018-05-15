package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.adapters.IngredientsListItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddDrinkIngredientsViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;


public class AddDrinkIngredientsFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "adif_extra_name";

    private IngredientsListItemAdapter mAdapter;
    private AddDrinkIngredientsViewModel mViewModel;
    private SharedViewModel mSharedIngredientsViewModel;
    private SearchView searchView;

    public static AddDrinkIngredientsFragment getNewInstance(String name) {
        AddDrinkIngredientsFragment fragment = new AddDrinkIngredientsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_items_list, container, false);
        initViews(rootView);
        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);
        mSharedIngredientsViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SharedViewModel.class);
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
            if (query != null) mAdapter.getFilter().filter(query);
        });
        sharedIngredients.getSelectedIds().observe(this, list -> {
            if (list != null && !list.isEmpty() && (model.getLocalSelection().getValue() == null
                    || model.getLocalSelection().getValue().isEmpty())) {
                model.setLocalSelection(list);
            }
        });
        model.getLocalSelection().observe(this, list -> {
            if (list != null && !list.isEmpty()) {
                mAdapter.setSelectedIds(list);
            }
        });
    }

    private void initFAB(View view) {
        FloatingActionButton mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_add));
        mFab.setOnClickListener(view1 -> {
            if (getParentFragment() != null) {
                ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, null);
            }
        });
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitle(R.string.title_choose_ingredients);
        }
    }


    private void initViews(View rootView) {
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsListItemAdapter(getContext(), this::toggleSelection);
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()),
                DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void toggleSelection(Ingredient item) {
        mAdapter.toggleSelection(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_drink_ingredient_menu, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchIngredients(query);
                if (!searchView.isIconified()) {
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_about && getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "");
        }
        if (item.getItemId() == R.id.ab_add) {
            mSharedIngredientsViewModel.selectIds(mAdapter.getSelectedIds());
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
