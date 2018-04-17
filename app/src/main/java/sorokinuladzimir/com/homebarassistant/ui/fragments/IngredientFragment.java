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
import android.support.v7.widget.DividerItemDecoration;
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

import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Taste;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkSimpleItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientViewModel;


public class IngredientFragment extends Fragment implements BackButtonListener {

    public static final String MENU_ITEM_EDIT = "Edit";

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    private Long mIngredientId;

    private ImageView mIngredientImage;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private TextView mNotesText;

    private Menu collapsedMenu;
    private AppBarLayout mAppBarLayout;
    private boolean appBarExpanded = true;

    private View mCardDescription;
    private View mCardNotes;
    private View mCardRelatedDrinks;
    private DrinkSimpleItemAdapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_ingredient, container, false);

        initToolbar(rootView);
        initFAB(rootView);
        initViews(rootView);
        initRecyclerView(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Long ingredientId;
        if (getArguments() != null) {
            ingredientId = getArguments().getLong(EXTRA_ID);
            mIngredientId = ingredientId;
        }

        IngredientViewModel.Factory factory = new IngredientViewModel.Factory(
                Objects.requireNonNull(getActivity()).getApplication(), mIngredientId);

        IngredientViewModel mViewModel = ViewModelProviders.of(this, factory).get(IngredientViewModel.class);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(IngredientViewModel model) {

        model.getIngredient().observe(this, ingredient -> {
            if (ingredient != null) {
                Glide.with(Objects.requireNonNull(getContext()))
                        .load(ingredient.getImage() != null ? ingredient.getImage() : R.drawable.camera_placeholder)
                        .apply(RequestOptions.centerCropTransform())
                        .into(mIngredientImage);

                if (ingredient.getName() != null) mCollapsingToolbarLayout.setTitle(ingredient.getName());

                setTextToCard(mCardDescription, mDescriptionText,ingredient.getDescription());

                setTextToCard(mCardNotes, mNotesText,ingredient.getNotes());

            }
        });

        model.getRelatedDrinks().observe(this, drinks -> {
            if (drinks != null && drinks.size() != 0) {
                mCardRelatedDrinks.setVisibility(View.VISIBLE);
                mAdapter.setData(drinks);
            } else {
                mCardRelatedDrinks.setVisibility(View.GONE);
            }
        });

    }

    private void setTextToCard(View containerView, TextView textView, String text) {
        if (text != null && !Objects.equals(text,"")) {
            containerView.setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            containerView.setVisibility(View.GONE);
        }
    }

    public static IngredientFragment getNewInstance(String name, Long ingredientId) {
        IngredientFragment fragment = new IngredientFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putLong(EXTRA_ID, ingredientId);

        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.singleIngredientToolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);

        mAppBarLayout = view.findViewById(R.id.singleIngredientAppbar);
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

    private void initRecyclerView(View rootView) {
        final RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DrinkSimpleItemAdapter(drink ->
                Toast.makeText(getContext(), drink.getName(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && !appBarExpanded) {
            //collapsed
            collapsedMenu.add(MENU_ITEM_EDIT)
                    .setIcon(R.drawable.ic_edit_done)
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
        mCollapsingToolbarLayout = rootView.findViewById(R.id.singleIngredientToolbarLayout);
        mIngredientImage = rootView.findViewById(R.id.image_singleIngredient);
        mDescriptionText = rootView.findViewById(R.id.tv_singleingredient_description);
        mNotesText = rootView.findViewById(R.id.tv_singleingredient_notes);

        mCardNotes = rootView.findViewById(R.id.card_notes);
        mCardDescription = rootView.findViewById(R.id.card_description);
        mCardRelatedDrinks = rootView.findViewById(R.id.card_related_drinks);
    }

    private void initFAB(View view){
        FloatingActionButton mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(view1 -> {
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, mIngredientId);
            }
        });
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
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Ingredient fragment about text");
            }
        }
        if (item.getTitle() == MENU_ITEM_EDIT) {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, mIngredientId);
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getParentFragment() != null) {
            ((RouterProvider)getParentFragment()).getRouter().exit();
        }
        return true;
    }
}
