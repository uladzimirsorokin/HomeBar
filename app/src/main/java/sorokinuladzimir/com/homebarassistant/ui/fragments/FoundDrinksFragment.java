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

import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.BarDataRepository.QueryType;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkCardItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.FoundDrinksViewModel;

public class FoundDrinksFragment extends Fragment {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private DrinkCardItemAdapter mAdapter;

    private String mRequestConditions;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;
    private FoundDrinksViewModel mViewModel;
    private ImageView mIvEmptyState;
    private TextView mTvEmptyStateMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_items_list, container, false);

        mViewModel = ViewModelProviders.of(this).get(FoundDrinksViewModel.class);

        if(savedInstanceState != null) {
            mRequestConditions = savedInstanceState.getString("conditions");
        }

        initViews(rootView);
        initFAB(rootView);
        initToolbar(rootView);
        initRecyclerView(rootView);

        Bundle args = null;
        if (getArguments() != null) {
            args = getArguments().getBundle(EXTRA_BUNDLE);
        }
        if (args != null && !args.isEmpty()) {
            mRequestConditions = args.getString(Constants.Extra.REQUEST_CONDITIONS);
            mSwipeRefreshLayout.setRefreshing(true);
            searchDrinks(mRequestConditions, QueryType.SEARCH_BY_CONDITIONS, true);
            args.clear();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi();
    }

    private void subscribeUi() {
        setEmptyState(R.drawable.list_empty_state, getString(R.string.no_items_founddrinks), true);

        mViewModel.getDrinks().observe(this, drinkEntities -> {
            if (drinkEntities != null) {
                mSwipeRefreshLayout.setRefreshing(false);
                mAdapter.setData(drinkEntities);
                if (drinkEntities.size() == 0) {
                    setEmptyState(R.drawable.list_empty_state, getString(R.string.no_items_found), true);
                } else {
                    setEmptyState(0, null, false);
                }
            }
        });
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
        if (mRequestConditions != null){
            mSwipeRefreshLayout.setRefreshing(true);
            searchDrinks(mRequestConditions, QueryType.CURRENT, true);
        }
            mSwipeRefreshLayout.setRefreshing(false);
        });

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

    public static FoundDrinksFragment getNewInstance(String name, Bundle bundle) {
        FoundDrinksFragment fragment = new FoundDrinksFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putBundle(EXTRA_BUNDLE, bundle);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initFAB(View view){
        FloatingActionButton mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_search));
        mFab.setOnClickListener(view1 -> {
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SEARCH_DRINKS);
            }
        });
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.search_cocktails_toolbar_title);
        }
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DrinkCardItemAdapter(drink -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.COCKTAIL, drink);
            if (getParentFragment() != null) {
                ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.SINGLE_DRINK, bundle);
            }
        }, () -> searchDrinks(mRequestConditions, QueryType.CURRENT, false));

        recyclerView.setAdapter(mAdapter);
    }

    private void searchDrinks(String query, QueryType searchType, boolean clearList) {
        mViewModel.searchDrinks(query, searchType, clearList);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mRequestConditions = query;
                mSwipeRefreshLayout.setRefreshing(true);
                searchDrinks(mRequestConditions, QueryType.SEARCH_BY_NAME, true);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Found drinks fragment anbout text");
            }
        }
        if (item.getItemId() == R.id.action_settings) {
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SETTINGS);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("conditions", mRequestConditions);
    }

}
