package com.sorokinuladzimir.homebarassistant.ui.fragments;

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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.db.entity.Drink;
import com.sorokinuladzimir.homebarassistant.ui.adapters.LocalDrinkIngredientItemAdapter;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider;
import com.sorokinuladzimir.homebarassistant.ui.utils.TastesHelper;
import com.sorokinuladzimir.homebarassistant.viewmodel.DrinkViewModel;


public class LocalDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_EDITABLE = "extra_editable";

    private Long mDrinkId = 0L;

    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    private ImageView mDrinkImage;
    private LocalDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private View mCardPreparation;
    private View mCardNotes;
    private TextView mTvNotes;
    private TextView mTvGlass;
    private TextView mTvTastes;
    private TextView mTvType;
    private TextView mTvRating;
    private boolean mIsEditableDrink;

    public static LocalDrinkFragment getNewInstance(String name, Bundle bundle) {
        LocalDrinkFragment fragment = new LocalDrinkFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putLong(EXTRA_ID, bundle.getLong(Constants.Extra.EXTRA_ID));
        arguments.putBoolean(EXTRA_EDITABLE, bundle.getBoolean(Constants.Extra.EDITABLE));
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);
        if (getArguments() != null) {
            mDrinkId = getArguments().getLong(EXTRA_ID);
            mIsEditableDrink = getArguments().getBoolean(EXTRA_EDITABLE);
        }
        if (savedInstanceState != null) {
            mIsEditableDrink = savedInstanceState.getBoolean(EXTRA_EDITABLE);
        }
        initToolbar(rootView);
        initFAB(rootView, mIsEditableDrink);
        initViews(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DrinkViewModel.Factory factory = new DrinkViewModel.Factory(
                Objects.requireNonNull(getActivity()).getApplication(), mDrinkId);
        DrinkViewModel mViewModel = ViewModelProviders.of(this, factory).get(DrinkViewModel.class);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(DrinkViewModel model) {
        subscribeIngredients(model);
        model.getDrink().observe(this, drink -> {
            if (drink != null) {
                setImage(drink);
                setName(drink);
                setTextToCard(mCardPreparation, mDescriptionText, drink.getDescription());
                setTextToCard(mCardNotes, mTvNotes, drink.getNotes());
                mTvRating.setText(String.valueOf(drink.getRating()));
                setGlass(drink);
                setTastes(drink);
                mTvType.setText(getDrinkType(drink.isAlcoholic(), drink.isCarbonated()));
            }
        });
    }

    private void setTastes(Drink drink) {
        if (drink.getGlass() != null && drink.getGlass().getGlassName() != null) {
            mTvGlass.setText(drink.getGlass().getGlassName());
        } else {
            mTvGlass.setText("");
        }
    }

    private void setGlass(Drink drink) {
        if (drink.getTastes() != null && !drink.getTastes().isEmpty()) {
            mTvTastes.setVisibility(View.VISIBLE);
            mTvTastes.setText(TastesHelper.tastesToString(drink.getTastes()));
        } else {
            mTvTastes.setVisibility(View.GONE);
        }
    }

    private void setName(Drink drink) {
        if (drink.getName() != null) mCollapsingToolbarLayout.setTitle(drink.getName());
    }

    private void setImage(Drink drink) {
        Glide.with(Objects.requireNonNull(getContext()))
                .load(drink.getImage() != null ? drink.getImage() : R.drawable.camera_placeholder)
                .into(mDrinkImage);
    }

    private void subscribeIngredients(DrinkViewModel model) {
        model.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
            }
        });
    }

    private String getDrinkType(boolean isAlcoholic, boolean isCarbonated) {
        String type;
        if (isAlcoholic) {
            type = getString(R.string.alcoholic_drink_type);
        } else {
            type = getString(R.string.no_alco_drink_type);
        }
        if (isCarbonated) {
            type += getString(R.string.carbo_drink_type);
        }

        return type;
    }

    private void setTextToCard(View containerView, TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            containerView.setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            containerView.setVisibility(View.GONE);
        }
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.singleDrinkToolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.singleDrinkAppbar);
        mCollapsingToolbarLayout = view.findViewById(R.id.collapsingToolbarLayout);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset) >  appBarLayout.getTotalScrollRange() - 140) {
                if (appBarExpanded) {
                    appBarExpanded = false;
                    getActivity().invalidateOptionsMenu();
                }
            } else {
                if (!appBarExpanded) {
                    appBarExpanded = true;
                    getActivity().invalidateOptionsMenu();
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && !appBarExpanded && mIsEditableDrink) {
            //collapsed
            collapsedMenu.add(R.string.menu_edit)
                    .setIcon(R.drawable.ic_edit_done)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onPrepareOptionsMenu(collapsedMenu);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        collapsedMenu = menu;
    }

    private void initViews(View rootView) {
        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);
        mCardNotes = rootView.findViewById(R.id.card_notes);
        mCardPreparation = rootView.findViewById(R.id.card_preparation);
        mTvNotes = rootView.findViewById(R.id.tv_notes);
        mTvGlass = rootView.findViewById(R.id.tv_glass);
        mTvTastes = rootView.findViewById(R.id.tv_tastes);
        mTvType = rootView.findViewById(R.id.tv_type);
        mTvRating = rootView.findViewById(R.id.tv_rating);
        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LocalDrinkIngredientItemAdapter(ingredientItem ->
                navigateToIngredient(ingredientItem.getIngredientId()));
        rvIngredients.setAdapter(mAdapter);
    }

    private void navigateToIngredient(Long ingredientId) {
        if (getParentFragment() != null) {
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.Extra.EXTRA_ID, ingredientId);
            bundle.putBoolean(Constants.Extra.EDITABLE, false);
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.LOCAL_INGREDIENT, bundle);
        }
    }

    private void initFAB(View view, boolean isEditable) {
        FloatingActionButton mFab = view.findViewById(R.id.single_drink_fab);
        if (isEditable) {
            mFab.setOnClickListener(view1 -> editPressed());
        } else {
            mFab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_EDITABLE, mIsEditableDrink);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getParentFragment() != null) {
                ((RouterProvider) getParentFragment()).getRouter().exit();
            }
            return true;
        }
        if (item.getTitle() == getString(R.string.menu_edit)) {
            editPressed();
        }
        return false;
    }

    private void editPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, mDrinkId);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().exit();
        }
        return true;
    }
}
