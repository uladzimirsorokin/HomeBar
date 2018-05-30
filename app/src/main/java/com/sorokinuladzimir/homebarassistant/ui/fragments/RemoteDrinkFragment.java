package com.sorokinuladzimir.homebarassistant.ui.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Objects;

import com.sorokinuladzimir.homebarassistant.Constants;
import com.sorokinuladzimir.homebarassistant.R;
import com.sorokinuladzimir.homebarassistant.db.entity.Drink;
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity;
import com.sorokinuladzimir.homebarassistant.ui.adapters.RemoteDrinkIngredientItemAdapter;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.BackButtonListener;
import com.sorokinuladzimir.homebarassistant.ui.subnavigation.RouterProvider;
import com.sorokinuladzimir.homebarassistant.ui.utils.TastesHelper;
import com.sorokinuladzimir.homebarassistant.viewmodel.RemoteDrinkViewModel;

public class RemoteDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";


    private ImageView mDrinkImage;
    private RemoteDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private View mCardPreparation;
    private TextView mTvGlass;
    private TextView mTvTastes;
    private TextView mTvType;
    private TextView mTvRating;

    private RemoteDrinkViewModel mViewModel;

    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    public static RemoteDrinkFragment getNewInstance(String name, Bundle bundle) {
        RemoteDrinkFragment fragment = new RemoteDrinkFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putBundle(EXTRA_BUNDLE, bundle);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);
        RemoteDrinkViewModel.Factory factory = new RemoteDrinkViewModel.Factory(Objects.requireNonNull(getActivity()).getApplication(),
                (DrinkEntity) Objects.requireNonNull(Objects.requireNonNull(getArguments())
                        .getBundle(EXTRA_BUNDLE)).getSerializable(Constants.Extra.COCKTAIL));
        mViewModel = ViewModelProviders.of(this, factory).get(RemoteDrinkViewModel.class);
        initToolbar(rootView);
        initViews(rootView);
        initFAB(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(RemoteDrinkViewModel viewModel) {
        viewModel.getDrink().observe(this, drink -> {
            if (drink != null) {
                setImage(drink);
                setTextToCard(mCardPreparation, mDescriptionText, drink.getDescription());
                if (drink.getName() != null) mCollapsingToolbarLayout.setTitle(drink.getName());
                mTvRating.setText(String.valueOf(drink.getRating()));
                if (drink.getGlass() != null && drink.getGlass().getGlassName() != null)
                    mTvGlass.setText(drink.getGlass().getGlassName());
                if (drink.getTastes() != null && !drink.getTastes().isEmpty())
                    mTvTastes.setText(TastesHelper.tastesToString(drink.getTastes()));
                mTvType.setText(getDrinkType(drink.isAlcoholic(), drink.isCarbonated()));
            }
        });
        viewModel.getDrinkIngredients().observe(this, ingredientsList -> {
            if (ingredientsList != null) mAdapter.setData(ingredientsList);
        });
    }

    private void setImage(Drink drink) {
        Glide.with(Objects.requireNonNull(getContext()))
                .load(drink.getImage())
                .into(new SimpleTarget<Drawable>() {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        if (bitmap != null) {
                            mViewModel.setBitmap(bitmap);
                        }
                        mDrinkImage.setImageBitmap(bitmap);
                        mDrinkImage.setDrawingCacheEnabled(true);
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

    private void addDrinkToDb() {
        mViewModel.saveDrink();
    }

    private void initToolbar(View view) {
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.singleDrinkToolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.singleDrinkAppbar);
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
        if (collapsedMenu != null && !appBarExpanded) {
            //collapsed
            collapsedMenu.add(getString(R.string.menu_add))
                    .setIcon(R.drawable.ic_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onPrepareOptionsMenu(collapsedMenu);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        collapsedMenu = menu;
    }

    private void initViews(View rootView) {
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsingToolbarLayout);
        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);
        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RemoteDrinkIngredientItemAdapter(ingredientItem ->
                Toast.makeText(getContext(), ingredientItem.getIngredientName(), Toast.LENGTH_LONG).show());
        rvIngredients.setAdapter(mAdapter);
        View cardNotes = rootView.findViewById(R.id.card_notes);
        cardNotes.setVisibility(View.GONE);
        mCardPreparation = rootView.findViewById(R.id.card_preparation);
        mTvGlass = rootView.findViewById(R.id.tv_glass);
        mTvTastes = rootView.findViewById(R.id.tv_tastes);
        mTvType = rootView.findViewById(R.id.tv_type);
        mTvRating = rootView.findViewById(R.id.tv_rating);
    }

    private void initFAB(View view) {
        FloatingActionButton fab = view.findViewById(R.id.single_drink_fab);
        fab.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_add));
        fab.setOnClickListener(view1 -> {
            fab.setEnabled(false);
            addDrinkToDb();
            onBackPressed();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getTitle() == getString(R.string.menu_add)) {
            item.setEnabled(false);
            addDrinkToDb();
            onBackPressed();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().exit();
        }
        return true;
    }

}
