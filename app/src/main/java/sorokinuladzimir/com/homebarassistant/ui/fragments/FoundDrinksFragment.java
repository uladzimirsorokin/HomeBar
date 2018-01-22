package sorokinuladzimir.com.homebarassistant.ui.fragments;

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

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksApi;
import sorokinuladzimir.com.homebarassistant.net.AbsolutDrinksResult;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkCardItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.ui.utils.DrinksPathManager;


/**
 * Created by sorok on 17.10.2017.
 */

public class FoundDrinksFragment extends Fragment {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private ArrayList<Drink> mCocktailList;
    private DrinkCardItemAdapter mAdapter;
    private ActionBar mToolbar;
    private FloatingActionButton mFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        if(savedInstanceState != null) {
            mCocktailList = (ArrayList<Drink>) savedInstanceState.getSerializable("cocktailList");
        }

        initFAB(rootView);
        initToolbar(rootView);
        loadDrinks();
        initRecyclerView(rootView);

        return rootView;
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
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.search_result_toolbar_title);
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DrinkCardItemAdapter(new DrinkCardItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Drink drink) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.Extra.COCKTAIL, drink);
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SINGLE_DRINK, bundle);
            }
        });

        if(mCocktailList != null){
            mAdapter.setData(mCocktailList);
        }
        recyclerView.setAdapter(mAdapter);
    }

    private void loadDrinks(){
        Bundle args = getArguments().getBundle(EXTRA_BUNDLE);
        if((args != null)&& !args.isEmpty()){
            OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("apiKey", Constants.Keys.ABSOLUT_API_KEY)
                            .addQueryParameter("pageSize", String.valueOf(40))
                            .addQueryParameter("lang", "ru")
                            .build();
                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url);
                    Request request = requestBuilder.build();

                    return chain.proceed(request);
                }
            }).build();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(Constants.Uri.ABSOLUT_DRINKS_ROOT)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();

            AbsolutDrinksApi client =  retrofit.create(AbsolutDrinksApi.class);

            // Fetch a list of the Github repositories.
            Call<AbsolutDrinksResult> call = client.getAllMatchedDrinks(getPathManager(args).getPath());

            // Execute the call asynchronously. Get a positive or negative callback.
            call.enqueue(new Callback<AbsolutDrinksResult>() {
                @Override
                public void onResponse(Call<AbsolutDrinksResult> call, Response<AbsolutDrinksResult> response) {
                    // The network call was a success and we got a response
                    // TODO: use the repository list and display it
                    ArrayList<Drink> cocktails = response.body().getResult();
                    mCocktailList = cocktails;

                    if(mCocktailList != null){
                        mAdapter.setData(mCocktailList);
                    }
                }

                @Override
                public void onFailure(Call<AbsolutDrinksResult> call, Throwable t) {
                    // the network call was a failure
                    // TODO: handle error
                }
            });
        }
    }

    private DrinksPathManager getPathManager(Bundle args) {
        int minRating = args.getInt(Constants.Extra.MIN_RATING);
        int maxRating = args.getInt(Constants.Extra.MAX_RATING);
        int glassId = args.getInt(Constants.Extra.GLASS_ID);
        int tasteId = args.getInt(Constants.Extra.TASTE_ID);
        int ingredientId = args.getInt(Constants.Extra.INGREDIENT_ID);
        int skill = args.getInt(Constants.Extra.SKILL);
        int color = args.getInt(Constants.Extra.COLOR);
        boolean isCarbonated = args.getBoolean(Constants.Extra.CARBONATED);
        boolean isAlcoholic = args.getBoolean(Constants.Extra.ALCOHOLIC);

        args.clear();
        args = null;

        String glass = getResources().getStringArray(R.array.glass_id)[glassId];
        if(glassId == 0) glass = null;
        String taste = getResources().getStringArray(R.array.taste_id)[tasteId];
        String ingredient = getResources().getStringArray(R.array.ingridient_type)[ingredientId];
        if(ingredientId == 0) ingredient = null;
        String[] tastes = {taste};
        if(tasteId == 0) tastes = null;
        String[] ingredients = {ingredient};
        if(ingredientId == 0) ingredients = null;
        String[] skills = new String[skill+1];
        for (int i = 0; i < skill + 1; i++)
            skills[i] = String.valueOf(i+1);
        int[] colors = getResources().getIntArray(R.array.colors);
        int colorId = 0;
        for (int i = 0; i < colors.length; i++) {
            if(colors[i] == color) {
                colorId = i;
            }
        }
        String colorResult = null;
        if(colorId > 0) colorResult = getResources().getStringArray(R.array.colors_name)[colorId];

        final DrinksPathManager pathManager = new DrinksPathManager.Builder()
                .setGlassType(glass)
                .setIngredient(ingredients)
                .setTaste(tastes)
                .setRating(minRating, maxRating)
                .setSkill(skills)
                .setCarbonated(isCarbonated)
                .setAlcoholic(isAlcoholic)
                .setColor(colorResult)
                .build();

        return pathManager;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("cocktailList", mCocktailList);
    }

}
