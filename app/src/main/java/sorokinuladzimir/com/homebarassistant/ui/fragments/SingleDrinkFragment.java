package sorokinuladzimir.com.homebarassistant.ui.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

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

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;

import sorokinuladzimir.com.homebarassistant.db.mapper.RawIngredientToWholeCocktailMapper;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;

import sorokinuladzimir.com.homebarassistant.ui.adapters.SingleDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.SingleDrinkViewModel;

/**
 * Created by sorok on 18.10.2017.
 */


public class SingleDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";


    private ImageView mDrinkImage;
    private SingleDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFab;


    private SingleDrinkViewModel mViewModel;

    private Menu collapsedMenu;
    private AppBarLayout mAppBarLayout;
    private boolean appBarExpanded = true;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);

        SingleDrinkViewModel.Factory factory = new SingleDrinkViewModel.Factory(getActivity().getApplication(),
                (DrinkEntity) getArguments().getBundle(EXTRA_BUNDLE).getSerializable(Constants.Extra.COCKTAIL));
        mViewModel = ViewModelProviders.of(this, factory).get(SingleDrinkViewModel.class);

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

    private void subscribeUi(SingleDrinkViewModel viewModel) {
        viewModel.getDrink().observe(this, drink -> {
            if (drink != null) {
                Glide.with(getContext())
                        .load(drink.getImage())
                        .into(new SimpleTarget<Drawable>() {

                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();
                                if (bitmap != null) {
                                    mViewModel.setBitmap(bitmap);
                                }
                                mDrinkImage.setImageBitmap(bitmap);
                                mDrinkImage.setDrawingCacheEnabled(true);
                            }
                        });
                if (drink.getIngredients() != null) mAdapter.setData(
                        RawIngredientToWholeCocktailMapper.getInstance().reverseMap(drink.getIngredients()));
                if (drink.getDescription() != null) mDescriptionText.setText(drink.getDescription());
                if (drink.getName() != null) mCollapsingToolbarLayout.setTitle(drink.getName());
            }
        });
    }

    private void addDrinkToDb(){
        mViewModel.saveDrink();
    }

    public static SingleDrinkFragment getNewInstance(String name, Bundle bundle) {
        SingleDrinkFragment fragment = new SingleDrinkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putBundle(EXTRA_BUNDLE, bundle);
        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.singleDrinkToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);

        mAppBarLayout = view.findViewById(R.id.singleDrinkAppbar);
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
            collapsedMenu.add(getString(R.string.menu_add))
                    .setIcon(R.drawable.ic_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            //expanded
        }
        super.onPrepareOptionsMenu(collapsedMenu);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_without_search_menu, menu);
        collapsedMenu = menu;
    }

    private void initViews(View rootView) {

        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsingToolbarLayout);
        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);

        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SingleDrinkIngredientItemAdapter(ingredientItem ->
                Toast.makeText(getContext(), ingredientItem.getIngredientName(), Toast.LENGTH_LONG).show());
        rvIngredients.setAdapter(mAdapter);

    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.single_drink_fab);
        mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_add));
        mFab.setOnClickListener(view1 -> {
            addDrinkToDb();
            mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_done));
            mFab.setEnabled(false);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Single drink fragment anbout text");
        }
        if (item.getTitle() == getString(R.string.menu_add)) {
            addDrinkToDb();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }

}
