package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.MainActivity;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.LocalDrinksListAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.ui.utils.ThemeUtils;
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
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initViews(rootView);
        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);

        mViewModel = ViewModelProviders.of(getActivity()).get(DrinkListViewModel.class);
        subscribeUi(mViewModel);

        return rootView;
    }


    private void subscribeUi(DrinkListViewModel viewModel) {
        mSwipeRefreshLayout.setRefreshing(true);
        mViewModel.searchDrinks("");
        // Update the list when the data changes
        viewModel.getDrinks().observe(this, drinks -> {
            if (drinks != null) {
                mAdapter.setDrinks(drinks);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LocalDrinksListAdapter(drink -> {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.LOCAL_DRINK, drink.getId());
        });
        recyclerView.setAdapter(mAdapter);
        ((DragScrollBar) rootView.findViewById(R.id.dragScrollBar))
                .setIndicator(new AlphabetIndicator(getContext()), true);
    }

    public static DrinksListFragment getNewInstance(String name) {
        DrinksListFragment fragment = new DrinksListFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.drinks_list_fragment_title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchDrinks(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                mViewModel.searchDrinks(s);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Drinks list fragment anbout text");
        }
        if (item.getItemId() == R.id.action_settings) {
            int theme = (getActivity().getPreferences(Context.MODE_PRIVATE).getInt("currentTheme", 0)+1)%3;
            ThemeUtils.changeToTheme(getActivity(), theme);
        }
        return false;
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(view1 ->
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, -1L));
    }


}
