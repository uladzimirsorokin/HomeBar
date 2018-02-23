package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.ui.adapters.LocalDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.adapters.LocalDrinksListAdapter;
import sorokinuladzimir.com.homebarassistant.ui.adapters.SingleDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.DrinkListViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.DrinkViewModel;


public class LocalDrinkFragment extends Fragment implements BackButtonListener {

    private final String TAG = "LocalDrinkFragment";

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    private Long mDrinkId = 0L;

    private ImageView mDrinkImage;
    private LocalDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private DrinkViewModel mViewModel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);

        initToolbar(rootView);
        initFAB(rootView);
        initViews(rootView);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Long drinkId = getArguments().getLong(EXTRA_ID);
        if((drinkId != null)){
            mDrinkId = drinkId;
        }

        DrinkViewModel.Factory factory = new DrinkViewModel.Factory(
                getActivity().getApplication(), mDrinkId);

        mViewModel = ViewModelProviders.of(this, factory).get(DrinkViewModel.class);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(DrinkViewModel model) {

        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
            } else {

            }
        });

        model.getDrink().observe(this, drink -> {
            if (drink != null) {
                Glide.with(getContext())
                        .load(drink.image != null ? drink.image : R.drawable.camera_placeholder)
                        .into(mDrinkImage);
                if(drink.description != null) mDescriptionText.setText(drink.description);
                if(drink.name != null) mCollapsingToolbarLayout.setTitle(drink.name);
            }
        });

    }

    public static LocalDrinkFragment getNewInstance(String name, Long drinkId) {
        LocalDrinkFragment fragment = new LocalDrinkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putLong(EXTRA_ID, drinkId);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.singleDrinkToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null)
            mToolbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initViews(View rootView) {
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsingToolbarLayout);

        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);

        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LocalDrinkIngredientItemAdapter(ingredientItem -> Toast.makeText(getContext(),
                ingredientItem.ingredientName, Toast.LENGTH_LONG).show());
        rvIngredients.setAdapter(mAdapter);
    }

    private void initFAB(View view){
        FloatingActionButton mFab = view.findViewById(R.id.single_drink_fab);
        mFab.setOnClickListener(view1 -> {

            //TODO: jump to edit(add) cocktail fragment on click
            Toast.makeText(getContext(),"edit pressed",Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            ((RouterProvider)getParentFragment()).getRouter().exit();
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
