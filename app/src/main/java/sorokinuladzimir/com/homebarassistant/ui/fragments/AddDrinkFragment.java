package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.ui.adapters.AddDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddDrinkViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;


/**
 * Created by sorok on 04.11.2017.
 */

public class AddDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    private ActionBar mToolbar;
    private RecyclerView mRvIngredients;
    private AddDrinkIngredientItemAdapter mAdapter;
    private TextView mGlass;
    private Long mDrinkId = -1L;
    private AddDrinkViewModel mViewModel;
    private SharedViewModel mSharedIngredientsViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_drink, container, false);

        initToolbar(rootView);
        initViews(rootView);

        mDrinkId = getArguments().getLong(EXTRA_ID);

        AddDrinkViewModel.Factory factory = new AddDrinkViewModel.Factory(getActivity().getApplication(), mDrinkId);

        mViewModel = ViewModelProviders.of(this, factory).get(AddDrinkViewModel.class);

        mSharedIngredientsViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel, mSharedIngredientsViewModel);
    }

    private void subscribeUi(AddDrinkViewModel drinkModel, SharedViewModel ingredientsModel) {
        drinkModel.getDrink().observe(this, drink -> {
            Log.d(this.getClass().getSimpleName(), AddDrinkFragment.this.toString());
        });

        drinkModel.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
            }
        });

        ingredientsModel.getSelectedIds().observe(this, ingredientsId -> {
            if (ingredientsId != null && ingredientsId.size() > 0) {
                mViewModel.setSelectedIds(ingredientsId);
            }
        });

    }

    private void initViews(View view) {
        mRvIngredients = view.findViewById(R.id.rv_ingredients);
        mRvIngredients.setHasFixedSize(true);
        mRvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AddDrinkIngredientItemAdapter((position, cocktail) -> {
            mAdapter.deleteItem(position);
            mViewModel.removeIngredient(cocktail);
        });
        mRvIngredients.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRvIngredients.addItemDecoration(itemDecoration);



        mGlass = view.findViewById(R.id.tvGlass);
        mGlass.setOnClickListener(view1 -> {
            mSharedIngredientsViewModel.getSelectedIds().setValue(mViewModel.getIngredientIds());
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK_INGREDIENTS);
        });
    }

    public static AddDrinkFragment getNewInstance(String name, Long drinkId) {
        AddDrinkFragment fragment = new AddDrinkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putLong(EXTRA_ID, drinkId);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setTitle("Новый коктейль");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        mSharedIngredientsViewModel.selectIds(null);
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }
}
