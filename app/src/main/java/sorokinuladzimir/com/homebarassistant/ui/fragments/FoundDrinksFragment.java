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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sorokinuladzimir.com.homebarassistant.BarDataRepository;
import sorokinuladzimir.com.homebarassistant.BarDataRepository.QueryType;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.net.NoConnectivityException;
import sorokinuladzimir.com.homebarassistant.net.RetrofitInstance;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksApi;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksResult;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkCardItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.FoundDrinksViewModel;


/**
 * Created by sorok on 17.10.2017.
 */

public class FoundDrinksFragment extends Fragment {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private DrinkCardItemAdapter mAdapter;

    private String mRequestConditions;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;
    private FoundDrinksViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

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
        mViewModel.getDrinks().observe(this, drinkEntities -> {
            if (drinkEntities != null) mAdapter.setData(drinkEntities);
        });
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
        if (mRequestConditions != null){
            searchDrinks(mRequestConditions, QueryType.CURRENT, true);
        }
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    public static FoundDrinksFragment getNewInstance(String name, Bundle bundle) {
        FoundDrinksFragment fragment = new FoundDrinksFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putBundle(EXTRA_BUNDLE,bundle);
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
        }, () -> {
                searchDrinks(mRequestConditions, QueryType.CURRENT, false);
        });

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("conditions", mRequestConditions);
    }

}
