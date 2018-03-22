package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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

import com.turingtechnologies.materialscrollbar.AlphabetIndicator;
import com.turingtechnologies.materialscrollbar.DragScrollBar;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.IngredientsListItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientListViewModel;


/**
 * Created by sorok on 17.10.2017.
 */

public class IngredientsListFragment extends Fragment {

    private static final String EXTRA_NAME = "ilf_extra_name";

    private IngredientsListItemAdapter mAdapter;
    private ActionBar mToolbar;
    private FloatingActionButton mFab;
    private IngredientListViewModel mViewModel;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView searchView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_drinks_list, container, false);

        initFAB(rootView);
        initRecyclerView(rootView);
        initToolbar(rootView);
        initViews(rootView);

        mViewModel = ViewModelProviders.of(this).get(IngredientListViewModel.class);

        return rootView;
    }

    private void initViews(View rootView) {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(IngredientListViewModel model) {
        mSwipeRefreshLayout.setRefreshing(true);
        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setIngredients(ingredients);
            } else {

            }
            mSwipeRefreshLayout.setRefreshing(false);
        });
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
        mFab.setOnClickListener(view1 -> {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT,null);
        });
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mToolbar.setTitle(R.string.ingredients_list_toolbar_title);
    }

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new IngredientsListItemAdapter(getContext(), getArguments().getString(EXTRA_NAME), ingredient ->{
            mViewModel.searchIngredients("");
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.LOCAL_INGREDIENT, ingredient.getId());
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        ((DragScrollBar) rootView.findViewById(R.id.dragScrollBar))
                .setIndicator(new AlphabetIndicator(getContext()), true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_with_search_menu, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.searchIngredients(query);
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                mViewModel.searchIngredients(s);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Found drinks fragment anbout text");
        }
        return super.onOptionsItemSelected(item);
    }

}