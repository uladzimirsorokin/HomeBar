package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;

import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.DrinkViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientViewModel;


public class IngredientFragment extends Fragment implements BackButtonListener {

    private final String TAG = "IngredientFragment";

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    private Long mIngredientId;

    private ImageView mIngredientImage;
    private ActionBar mToolbar;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFab;
    private IngredientViewModel mViewModel;
    private TextView mNotesText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_ingredient, container, false);

        initToolbar(rootView);
        initFAB(rootView);
        initViews(rootView);

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Long ingredientId = getArguments().getLong(EXTRA_ID);
        if((ingredientId != null)){
            mIngredientId = ingredientId;
        }

        IngredientViewModel.Factory factory = new IngredientViewModel.Factory(
                getActivity().getApplication(), mIngredientId);

        mViewModel = ViewModelProviders.of(this, factory).get(IngredientViewModel.class);
        subscribeUi(mViewModel);
    }

    private void subscribeUi(IngredientViewModel model) {

        model.getIngredient().observe(this, ingredient -> {
            if (ingredient != null) {
                Glide.with(getContext())
                        .load(ingredient.image != null ? ingredient.image : R.drawable.camera_placeholder)
                        .apply(RequestOptions.centerCropTransform())
                        .into(mIngredientImage);
                if(ingredient.description != null){
                    mDescriptionText.setText(ingredient.description);
                    mNotesText.setText(ingredient.description);
                } else{
                    mDescriptionText.setText("Some description");
                    mNotesText.setText("Some very interesting notes");
                }
                if(ingredient.name!=null) mCollapsingToolbarLayout.setTitle(ingredient.name);
            } else {

            }
        });

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
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null)
            mToolbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initViews(View rootView) {
        mCollapsingToolbarLayout = rootView.findViewById(R.id.singleIngredientToolbarLayout);
        mIngredientImage = rootView.findViewById(R.id.image_singleIngredient);
        mDescriptionText = rootView.findViewById(R.id.tv_singleingredient_description);
        mNotesText = rootView.findViewById(R.id.tv_singleingredient_notes);

    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(view1 -> {
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_INGREDIENT, mIngredientId);
        });
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
}
