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

import java.util.ArrayList;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.adapters.IngredientsListItemAdapter;


/**
 * Created by sorok on 17.10.2017.
 */

public class IngredientsListFragment extends Fragment {

    private static final String EXTRA_NAME = "ilf_extra_name";
    private ArrayList<Ingredient> mIngredientList;
    private IngredientsListItemAdapter mAdapter;
    private ActionBar mToolbar;
    private FloatingActionButton mFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        if(savedInstanceState != null) {
            mIngredientList = (ArrayList<Ingredient>) savedInstanceState.getSerializable("ingredientsList");
        }

        initFAB(rootView);
        initToolbar(rootView);

        mIngredientList = new ArrayList<Ingredient>();
        for (int i = 0; i < 20; i++) {
            Ingredient ingredient = new Ingredient();
            ingredient.setName("Vodochka so wkvarochkoi ololololo"+i);
            ingredient.setUrl("https://www.absolut.com/globalassets/images/products/absolut-raspberri/absolut-raspberri-listing.png");
            mIngredientList.add(ingredient);
        }

        initRecyclerView(rootView);

        return rootView;
    }


    public static IngredientsListFragment getNewInstance(String name) {
        IngredientsListFragment fragment = new IngredientsListFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SEARCH_DRINKS);
            }
        });
    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.ingredients_list_toolbar_title);
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsListItemAdapter(new IngredientsListItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ingredient ingredient) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.Extra.INGREDIENT, ingredient);
                //((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.SINGLE_DRINK, bundle);
            }
        });

        if(mIngredientList != null){
            mAdapter.setData(mIngredientList);
        }
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ingredientsList", mIngredientList);
    }

}
