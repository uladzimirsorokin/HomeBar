package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
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
    private ActionBar mToolbar;
    private FloatingActionButton mFab;

    private ArrayList<DrinkEntity> mCocktailList;
    private int mTotalResult = 0;
    private String mNextLink;
    private String mRequestConditions;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        if(savedInstanceState != null) {
            mCocktailList = (ArrayList<DrinkEntity>) savedInstanceState.getSerializable("cocktailList");
            mTotalResult = savedInstanceState.getInt("total");
            mNextLink = savedInstanceState.getString("next");
            mRequestConditions = savedInstanceState.getString("conditions");
        }

        initViews(rootView);
        initFAB(rootView);
        initToolbar(rootView);
        initRecyclerView(rootView);

        Bundle args = getArguments().getBundle(EXTRA_BUNDLE);
        if (args != null && !args.isEmpty()) {
            mRequestConditions = args.getString(Constants.Extra.REQUEST_CONDITIONS);
            loadDrinks(0, Constants.Values.DEFAULT_ITEM_AMOUNT, mRequestConditions);
            args.clear();
        }

        return rootView;
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
        if (mRequestConditions != null){
            mCocktailList.clear();
            loadDrinks(0, Constants.Values.DEFAULT_ITEM_AMOUNT, mRequestConditions);
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
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_search));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SEARCH_DRINKS);
            }
        });
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.search_cocktails_toolbar_title);
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DrinkCardItemAdapter(drink -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.Extra.COCKTAIL, drink);
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.SINGLE_DRINK, bundle);
        }, () -> {
            if (mCocktailList != null && mCocktailList.size() < mTotalResult) {
                loadDrinks(mCocktailList.size(), Constants.Values.DEFAULT_ITEM_AMOUNT, mRequestConditions);
            }
        });

        if(mCocktailList != null){
            mAdapter.setData(mCocktailList);
        }
        recyclerView.setAdapter(mAdapter);
    }

    private void loadDrinks(int start, int pageSize, String conditions){
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("apiKey", Constants.Keys.ABSOLUT_API_KEY)
                    .addQueryParameter("start", String.valueOf(start))
                    .addQueryParameter("pageSize", String.valueOf(pageSize))
                    .addQueryParameter("lang", "ru")
                    .build();
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);
            Request request = requestBuilder.build();

            return chain.proceed(request);
        }).build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(Constants.Uri.ABSOLUT_DRINKS_ROOT)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        AbsolutDrinksApi client =  retrofit.create(AbsolutDrinksApi.class);

        Call<AbsolutDrinksResult> call = client.getAllMatchedDrinks(conditions);

        call.enqueue(new Callback<AbsolutDrinksResult>() {
            @Override
            public void onResponse(Call<AbsolutDrinksResult> call, Response<AbsolutDrinksResult> response) {
                // The network call was a success and we got a response
                // TODO: use the repository list and display it
                mTotalResult = response.body().getTotalResult();

                if (mCocktailList != null && mNextLink != null && !mNextLink.equals(response.body().getNext())) {
                    mCocktailList.addAll(response.body().getResult());
                } else {
                    mCocktailList = response.body().getResult();
                }

                mNextLink = response.body().getNext();

                if(mCocktailList != null){
                    mAdapter.setData(mCocktailList);
                }
            }

            @Override
            public void onFailure(Call<AbsolutDrinksResult> call, Throwable t) {
                // the network call was a failure
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_without_search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Found drinks fragment anbout text");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("cocktailList", mCocktailList);
        outState.putString("conditions", mRequestConditions);
        outState.putInt("total", mTotalResult);
        outState.putString("next", mNextLink);
    }

}
