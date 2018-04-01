package sorokinuladzimir.com.homebarassistant.ui.fragments;

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
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.net.NoConnectivityException;
import sorokinuladzimir.com.homebarassistant.net.RetrofitInstance;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksApi;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksResult;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkCardItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;


/**
 * Created by sorok on 17.10.2017.
 */

public class FoundDrinksFragment extends Fragment {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private DrinkCardItemAdapter mAdapter;

    private ArrayList<DrinkEntity> mCocktailList = new ArrayList<>();
    private int mTotalResult = 0;
    private String mNextLink;
    private String mRequestConditions;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;

    private int mCurrentSearchType = -1;

    private static final int SEARCH_BY_NAME = 0;

    private static final int SEARCH_BY_CONDITIONS = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        if(savedInstanceState != null) {
            mCocktailList = (ArrayList<DrinkEntity>) savedInstanceState.getSerializable("cocktailList");
            mTotalResult = savedInstanceState.getInt("total");
            mCurrentSearchType = savedInstanceState.getInt("searchType");
            mNextLink = savedInstanceState.getString("next");
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
            mCurrentSearchType = SEARCH_BY_CONDITIONS;
            searchDrinks(0, Constants.Values.DEFAULT_ITEM_AMOUNT, mRequestConditions, mCurrentSearchType, true);
            args.clear();
        }

        return rootView;
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
        if (mRequestConditions != null){
            searchDrinks(0,
                    Constants.Values.DEFAULT_ITEM_AMOUNT,
                    mRequestConditions,
                    mCurrentSearchType,
                    true);
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
            if (mCocktailList != null && mCocktailList.size() < mTotalResult) {
                searchDrinks(mCocktailList.size(),
                        Constants.Values.DEFAULT_ITEM_AMOUNT,
                        mRequestConditions,
                        mCurrentSearchType,
                        false);
            }
        });

        if(mCocktailList != null){
            mAdapter.setData(mCocktailList);
        }
        recyclerView.setAdapter(mAdapter);
    }

    private void searchDrinks(int start, int pageSize, String query, int searchType, boolean clearDrinkList) {

        if (clearDrinkList && mCocktailList != null) mCocktailList.clear();

        AbsolutDrinksApi client =  RetrofitInstance
                .getRetrofitInstance(getContext(), "ru")
                .create(AbsolutDrinksApi.class);

        Call<AbsolutDrinksResult> call;
        switch (searchType) {
            case SEARCH_BY_CONDITIONS:
                call = client.getAllMatchedDrinks(query, start, pageSize);
                break;
            case SEARCH_BY_NAME:
                call = client.searchDrinks(query, start, pageSize);
                break;
            default:
                call = null;
        }


        if (call != null) {
            loadDrinks(call);
        } else {
            Toast.makeText(getContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadDrinks(Call<AbsolutDrinksResult> call){

        call.enqueue(new Callback<AbsolutDrinksResult>() {
            @Override
            public void onResponse(@NonNull Call<AbsolutDrinksResult> call, @NonNull Response<AbsolutDrinksResult> response) {
                if (response.isSuccessful()) {
                    // The network call was a success and we got a response
                    setDrinksResult(response);
                } else {
                    Toast.makeText(getContext(), "server returned error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AbsolutDrinksResult> call, @NonNull Throwable t) {
                // the network call was a failure
                if (t instanceof NoConnectivityException) {
                    // No internet connection
                    Toast.makeText(getContext(), "no internet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "conversion issue! big problems :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setDrinksResult(Response<AbsolutDrinksResult> response){
        mTotalResult = Objects.requireNonNull(response.body()).getTotalResult();

        if (mCocktailList != null && mNextLink != null && !mNextLink.equals(Objects.requireNonNull(response.body()).getNext())) {
            mCocktailList.addAll(Objects.requireNonNull(response.body()).getResult());
        } else {
            mCocktailList = Objects.requireNonNull(response.body()).getResult();
        }

        mNextLink = Objects.requireNonNull(response.body()).getNext();

        if(mCocktailList != null){
            mAdapter.setData(mCocktailList);
        }
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
                mCurrentSearchType = SEARCH_BY_NAME;
                searchDrinks(0, Constants.Values.DEFAULT_ITEM_AMOUNT, mRequestConditions, mCurrentSearchType, true);
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
        outState.putSerializable("cocktailList", mCocktailList);
        outState.putString("conditions", mRequestConditions);
        outState.putInt("total", mTotalResult);
        outState.putString("next", mNextLink);
        outState.putInt("searchType", mCurrentSearchType);
    }

}
