package com.sorokinuladzimir.homebarassistant.ui.fragments;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import java.util.Objects;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.ui.adapters.IngredientsItemAdapter;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider;
import com.sorokinuladzimir.homebarassistant.viewmodel.IngredientListViewModel;

public class IngredientsListFragment extends Fragment {

    private static final String EXTRA_NAME = "ilf_extra_name";

    private IngredientsItemAdapter mAdapter;
    private IngredientListViewModel mViewModel;
    private SearchView searchView;
    private ImageView mIvEmptyState;
    private TextView mTvEmptyStateMessage;

    public static IngredientsListFragment getNewInstance(String name) {
        IngredientsListFragment fragment = new IngredientsListFragment();
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
        mViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(IngredientListViewModel.class);
        mViewModel.restoreSources();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(IngredientListViewModel model) {
        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setIngredients(ingredients);
                if (ingredients.isEmpty()) {
                    setEmptyState(R.drawable.list_empty_state, getString(R.string.no_items_ingredients), true);
                } else {
                    setEmptyState(0, null, false);
                }
            }
        });
    }

    private void initViews(View rootView) {
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));
        mIvEmptyState = rootView.findViewById(R.id.iv_empty_state);
        mTvEmptyStateMessage = rootView.findViewById(R.id.tv_empty_state_message);
    }

    private void setEmptyState(int imageResId, String message, boolean isVisible) {
        if (isVisible) {
            mIvEmptyState.setVisibility(View.VISIBLE);
            mTvEmptyStateMessage.setVisibility(View.VISIBLE);
            mIvEmptyState.setImageResource(imageResId);
            mTvEmptyStateMessage.setText(message);
        } else {
            mIvEmptyState.setVisibility(View.GONE);
            mTvEmptyStateMessage.setVisibility(View.GONE);
        }
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
            mToolbar.setTitle(R.string.ingredients_list_toolbar_title);
        }
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsItemAdapter(ingredient -> {
            if (getParentFragment() != null) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.Extra.EXTRA_ID, ingredient.getId());
                bundle.putBoolean(Constants.Extra.EDITABLE, true);
                ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.LOCAL_INGREDIENT, bundle);
            }
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        ((DragScrollBar) rootView.findViewById(R.id.dragScrollBar))
                .setIndicator(new AlphabetIndicator(getContext()), true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu);
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
                mViewModel.restoreSources();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about && getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Found drinks fragment anbout text");
        }
        if (item.getItemId() == R.id.action_settings && getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.SETTINGS);
        }

        return super.onOptionsItemSelected(item);
    }

}