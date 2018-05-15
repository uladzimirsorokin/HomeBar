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
import com.bumptech.glide.request.RequestOptions;

import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.DrinkSimpleItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientViewModel;


public class IngredientFragment extends Fragment implements BackButtonListener {

    public static final String MENU_ITEM_EDIT = "Edit";

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";
    private static final String EXTRA_EDITABLE = "extra_editable";

    private Long mIngredientId;

    private ImageView mIngredientImage;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private TextView mNotesText;

    private Menu collapsedMenu;
    private boolean appBarExpanded = true;

    private View mCardDescription;
    private View mCardNotes;
    private View mCardRelatedDrinks;
    private DrinkSimpleItemAdapter mAdapter;
    private boolean mIsEditableIngredient;

    public static IngredientFragment getNewInstance(String name, Bundle bundle) {
        IngredientFragment fragment = new IngredientFragment();
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
        View rootView = inflater.inflate(R.layout.fr_single_ingredient, container, false);
        if (getArguments() != null) {
            mIngredientId = getArguments().getLong(EXTRA_ID);
            mIsEditableIngredient = getArguments().getBoolean(EXTRA_EDITABLE);
        }
        if (savedInstanceState != null) {
            mIsEditableIngredient = savedInstanceState.getBoolean(EXTRA_EDITABLE);
        }
        initToolbar(rootView);
        initFAB(rootView, mIsEditableIngredient);
        initViews(rootView);
        initRecyclerView(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                if (ingredient.getName() != null)
                    mCollapsingToolbarLayout.setTitle(ingredient.getName());
                setTextToCard(mCardDescription, mDescriptionText, ingredient.getDescription());
                setTextToCard(mCardNotes, mNotesText, ingredient.getNotes());
            }
        });
        model.getRelatedDrinks().observe(this, drinks -> {
            if (drinks != null && !drinks.isEmpty()) {
                mCardRelatedDrinks.setVisibility(View.VISIBLE);
                mAdapter.setData(drinks);
            } else {
                mCardRelatedDrinks.setVisibility(View.GONE);
            }
        });
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
        Toolbar toolbar = view.findViewById(R.id.singleIngredientToolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mToolbar != null) mToolbar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = view.findViewById(R.id.singleIngredientAppbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (Math.abs(verticalOffset) > appBarLayout.getTotalScrollRange() - 140) {
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
        mAdapter = new DrinkSimpleItemAdapter(drink -> {
            if (getParentFragment() != null) {
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.Extra.EXTRA_ID, drink.getId());
                bundle.putBoolean(Constants.Extra.EDITABLE, false);
                ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.LOCAL_DRINK, bundle);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && !appBarExpanded && mIsEditableIngredient) {
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

    private void initFAB(View view, boolean isEditable) {
        FloatingActionButton mFab = view.findViewById(R.id.fab);
        if (isEditable) {
            mFab.setOnClickListener(view1 -> {
                if (getParentFragment() != null) {
                    ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, mIngredientId);
                }
            });
        } else {
            mFab.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getParentFragment() != null) {
                ((RouterProvider) getParentFragment()).getRouter().exit();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_about && getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ABOUT, "Ingredient fragment about text");
        }
        if (item.getItemId() == R.id.action_settings && getParentFragment() != null) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.SETTINGS);
        }
        if (item.getTitle() == MENU_ITEM_EDIT) {
            ((RouterProvider) getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, mIngredientId);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_EDITABLE, mIsEditableIngredient);
    }
}
