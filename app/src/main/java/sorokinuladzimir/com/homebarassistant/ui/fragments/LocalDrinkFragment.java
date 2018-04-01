package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.LocalDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.DrinkViewModel;


public class LocalDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    private Long mDrinkId = 0L;

    private AppBarLayout mAppBarLayout;
    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    private ImageView mDrinkImage;
    private LocalDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);

        initToolbar(rootView);
        initFAB(rootView);
        initViews(rootView);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            mDrinkId= getArguments().getLong(EXTRA_ID);
        }

        DrinkViewModel.Factory factory = new DrinkViewModel.Factory(
                Objects.requireNonNull(getActivity()).getApplication(), mDrinkId);

        DrinkViewModel mViewModel = ViewModelProviders.of(this, factory).get(DrinkViewModel.class);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(DrinkViewModel model) {

        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
            }
        });

        model.getDrink().observe(this, drink -> {
            if (drink != null) {
                Glide.with(Objects.requireNonNull(getContext()))
                        .load(drink.getImage() != null ? drink.getImage() : R.drawable.camera_placeholder)
                        .apply(RequestOptions.centerCropTransform())
                        .into(mDrinkImage);
                if(drink.getDescription() != null) mDescriptionText.setText(drink.getDescription());
                if(drink.getName() != null) mCollapsingToolbarLayout.setTitle(drink.getName());

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
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);

        mAppBarLayout = view.findViewById(R.id.singleDrinkAppbar);
        mCollapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);

        mAppBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset) > mAppBarLayout.getTotalScrollRange() - 140) {
                appBarExpanded = false;
                getActivity().invalidateOptionsMenu();
            } else {
                appBarExpanded = true;
                getActivity().invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && !appBarExpanded) {
            //collapsed
            collapsedMenu.add(R.string.menu_edit)
                    .setIcon(R.drawable.ic_edit_done)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onPrepareOptionsMenu(collapsedMenu);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_without_search_menu, menu);
        collapsedMenu = menu;
    }


    private void initViews(View rootView) {
        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);

        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LocalDrinkIngredientItemAdapter(ingredientItem -> Toast.makeText(getContext(),
                ingredientItem.getIngredientName(), Toast.LENGTH_LONG).show());
        rvIngredients.setAdapter(mAdapter);
    }

    private void initFAB(View view){
        FloatingActionButton mFab = view.findViewById(R.id.single_drink_fab);
        mFab.setOnClickListener(view1 -> editPressed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().exit();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Local drink fragment anbout text");
            }
        }
        if (item.getTitle() == getString(R.string.menu_edit)) {
            editPressed();
        }
        return false;
    }

    private void editPressed(){
        if (getParentFragment() != null) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, mDrinkId);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider)getParentFragment()).getRouter().exit();
        }
        return true;
    }
}
