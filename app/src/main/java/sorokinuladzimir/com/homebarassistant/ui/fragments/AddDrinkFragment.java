package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.ui.adapters.AddDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddDrinkViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;

import static android.app.Activity.RESULT_OK;


/**
 * Created by sorok on 04.11.2017.
 */

public class AddDrinkFragment extends Fragment implements BackButtonListener,
        AddImageDialogFragment.AddImageDialogFragmentCallback {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int OPEN_PICTURE_CODE = 2;

    private static final String ALBUM_NAME = "HomeBar";

    private static final int DEFAULT_IMAGE_SIZE = 750;

    private ActionBar mToolbar;
    private RecyclerView mRvIngredients;
    private AddDrinkIngredientItemAdapter mAdapter;
    private TextView mGlass;
    private Long mDrinkId = -1L;
    private AddDrinkViewModel mViewModel;
    private SharedViewModel mSharedIngredientsViewModel;
    private ImageView mDrinkImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_drink, container, false);

        mDrinkId = getArguments().getLong(EXTRA_ID);

        AddDrinkViewModel.Factory factory = new AddDrinkViewModel.Factory(getActivity().getApplication(), mDrinkId);

        mViewModel = ViewModelProviders.of(this, factory).get(AddDrinkViewModel.class);

        mSharedIngredientsViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        initToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel, mSharedIngredientsViewModel);
    }

    private void subscribeUi(AddDrinkViewModel drinkModel, SharedViewModel sharedIngredients) {

        drinkModel.getCurrentImagePath().observe(this, imagePath -> {
            Glide.with(getContext())
                    .load(imagePath != null ? imagePath : R.drawable.camera_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mDrinkImage);
        });

        if(!drinkModel.getIsNewDrink()) {
            drinkModel.getDrink().observe(this, drink -> {
                if(drink != null && drinkModel.getCurrentImagePath().getValue() == null) {
                    if (drink.image != null && !drinkModel.getIsImageRemoved()){
                        drinkModel.getCurrentImagePath().setValue(drink.image);
                    }
                 /*   if (drink.name != null) .setText(ingredient.name);
                    if (ingredient.description != null) mDesc.setText(ingredient.description);
                    if (ingredient.notes != null) mNotes.setText(ingredient.notes);*/
                }
            });
        }

        drinkModel.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
             }
        });

        if (!drinkModel.getIsNewDrink()) {
            drinkModel.getInitialIngredientIds().observe(this, ingredientIds -> {
                if (ingredientIds != null) drinkModel.setIngredientIds(ingredientIds);
            });
        }

        sharedIngredients.getSelectedIds().observe(this, list -> {
            if (list != null) {
                drinkModel.setSelectedIds(list);
            }
        });
    }

    private void initViews(View view) {

        mDrinkImage = view.findViewById(R.id.image_add_drink);
        mDrinkImage.setOnClickListener(image -> showAddImageDialog());

        mRvIngredients = view.findViewById(R.id.rv_ingredients);
        mRvIngredients.setHasFixedSize(true);
        mRvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRvIngredients.addItemDecoration(itemDecoration);

        mAdapter = new AddDrinkIngredientItemAdapter((position, cocktail) -> {
            mAdapter.deleteItem(position);
            mViewModel.removeIngredient(cocktail);
        });
        mRvIngredients.setAdapter(mAdapter);


        mGlass = view.findViewById(R.id.tvGlass);
        mGlass.setOnClickListener(view1 -> {
            mSharedIngredientsViewModel.selectIds(mViewModel.getIngredientIds());
            ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK_INGREDIENTS);
        });
    }

    public static AddDrinkFragment getNewInstance(String name, Long drinkId) {
        AddDrinkFragment fragment = new AddDrinkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);

        if ((drinkId != null)) {
            arguments.putLong(EXTRA_ID, drinkId);
        } else {
            arguments.putLong(EXTRA_ID, -1L);
        }

        fragment.setArguments(arguments);

        return fragment;
    }

    private void initToolbar(View view){
        setHasOptionsMenu(true);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setHomeAsUpIndicator(R.drawable.ic_close);

            if(mViewModel.getIsNewDrink()){
                mToolbar.setTitle("Новый коктейль");
            } else {
                mToolbar.setTitle("Редактирование");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == OPEN_PICTURE_CODE && resultCode == RESULT_OK) {

            if (resultData != null) handleChoosenPicture(resultData.getData());

        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            handleTakenPicture(mViewModel.getPhotoUri());

        }
    }

    private void handleChoosenPicture(Uri imageUri){
        mViewModel.handleImage(getContext(), ALBUM_NAME, imageUri, DEFAULT_IMAGE_SIZE, false);
    }

    private void handleTakenPicture(Uri imageUri){
        mViewModel.handleImage(getContext(), ALBUM_NAME, imageUri, DEFAULT_IMAGE_SIZE, true);
    }

    void showAddImageDialog() {
        DialogFragment newFragment = AddImageDialogFragment.newInstance("Change photo",
                mViewModel.getCurrentImagePath().getValue() == null ? false : true );
        newFragment.setTargetFragment(this,911);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void addImageDialogCallback(int item) {
        switch (item){
            case 0:
                dispatchTakePictureIntent();
                break;
            case 1:
                dispatchChoosePictureIntent();
                break;
            case 2:
                mViewModel.removeCurrentImage(getContext());
                break;
            default:
                Log.d(getContext().getClass().getSimpleName(),"Some strange type of taking photo");
                break;
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            if (mViewModel.createPhotoFile(getContext(), ALBUM_NAME) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mViewModel.getPhotoUri());
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchChoosePictureIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_PICTURE_CODE);
    }

    private void saveDrink() {
        mViewModel.saveDrink(getContext(), "NAME", "desc", null, mAdapter.getIngredients());
        onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_item_menu, menu);
        if(mViewModel.getIsNewDrink()){
            menu.findItem(R.id.ab_add).setTitle("Добавить");
            menu.findItem(R.id.ab_delete).setVisible(false);
        } else {
            menu.findItem(R.id.ab_add).setTitle("Сохранить");
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.ab_add:
                saveDrink();
                return true;
            case R.id.ab_delete:
                mViewModel.deleteDrink();
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.DRINKS_LIST);
                return true;
            case R.id.ab_about:
                Toast.makeText(getContext(),"about app",Toast.LENGTH_SHORT).show();
                return true;
            default:
                Toast.makeText(getContext(),"Unknown menu item",Toast.LENGTH_SHORT).show();
                break;
        }

        return false;
    }

    @Override
    public boolean onBackPressed() {
        mSharedIngredientsViewModel.selectIds(null);
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }
}
