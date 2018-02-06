package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkCardItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.adapters.LocalDrinksListAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.LocalCiceroneHolder;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.DrinkListViewModel;


/**
 * Created by sorok on 17.10.2017.
 */

public class DrinksListFragment extends Fragment {

    private static final String EXTRA_NAME = "dlf_extra_name";
    private FloatingActionButton mFab;
    private LocalDrinksListAdapter mAdapter;
    private DrinkListViewModel mViewModel;
    private ActionBar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);

        mViewModel = ViewModelProviders.of(getActivity()).get(DrinkListViewModel.class);
        subscribeUi(mViewModel);

        return rootView;
    }


    private void subscribeUi(DrinkListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getDrinks().observe(this, drinks -> {
            if (drinks != null) {
                ArrayList<Drink> newDrinks = new ArrayList<>();
                newDrinks.addAll(drinks);
                mAdapter.setData(newDrinks);
            } else {

            }
        });
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LocalDrinksListAdapter(drink -> {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.LOCAL_DRINK, drink.id);
        });
        recyclerView.setAdapter(mAdapter);
    }

    public static DrinksListFragment getNewInstance(String name) {
        DrinksListFragment fragment = new DrinksListFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.drinks_list_fragment_title);
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(view1 ->
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, null));
    }


}
