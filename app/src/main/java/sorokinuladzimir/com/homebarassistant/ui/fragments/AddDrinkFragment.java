package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.db.entity.Glass;
import sorokinuladzimir.com.homebarassistant.ui.adapters.AddDrinkGlassAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;


/**
 * Created by sorok on 04.11.2017.
 */

public class AddDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_DRINK = "extra_drink";

    private ActionBar mToolbar;
    private DrinkEntity mDrink;

    String[] glassNames = { "Шот", "Олд фешн", "Харрикейн", "Коллинз",
            "Маргаритка", "Коктейльный бокал", "Снифтер" };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_drink, container, false);

        if(savedInstanceState != null) {
            mDrink = (DrinkEntity) savedInstanceState.getSerializable(Constants.Extra.COCKTAIL);
        }

        DrinkEntity drink = (DrinkEntity) getArguments().getSerializable(EXTRA_DRINK);

        if(drink != null){
            mDrink = drink;
            getArguments().clear();
        }

        initToolbar(rootView);

        List glasses = new ArrayList<Glass>();
        Glass glass1 = new Glass();
        glass1.setName("Old fashioned");
        glass1.setImage(R.drawable.bottles);
        Glass glass2 = new Glass();
        glass2.setName("Collins");
        glass2.setImage(R.drawable.cocktail);
        Glass glass3 = new Glass();
        glass3.setName("Rocks");
        glass3.setImage(R.drawable.alco);
        glasses.add(glass1);
        glasses.add(glass1);
        glasses.add(glass2);
        glasses.add(glass2);
        glasses.add(glass3);
        glasses.add(glass3);

        final Spinner spinner =  rootView.findViewById(R.id.spinnerGlass);
        // Подключаем свой шаблон с разными значками
        AddDrinkGlassAdapter adapter = new AddDrinkGlassAdapter(getContext(),
                R.layout.spinner_glass_item, glasses);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        return rootView;
    }

    public static AddDrinkFragment getNewInstance(String name, DrinkEntity drink) {
        AddDrinkFragment fragment = new AddDrinkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putSerializable(EXTRA_DRINK, drink);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null)
            mToolbar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle("Новый коктейль");
        if(mDrink != null) mToolbar.setTitle(mDrink.getName());
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.Extra.COCKTAIL, mDrink);
    }
}
