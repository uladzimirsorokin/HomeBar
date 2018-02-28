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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddIngredientViewModel;


import static android.app.Activity.RESULT_OK;


public class AddIngredientFragment extends Fragment implements BackButtonListener, AddImageDialogFragment.AddImageDialogFragmentCallback {

    private final String TAG = "AddIngredientFragment";

    private static final String EXTRA_NAME = "extra_name";

    private static final String EXTRA_ID = "extra_id";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int OPEN_PICTURE_CODE = 2;

    private static final String ALBUM_NAME = "HomeBar";

    private static final int DEFAULT_IMAGE_SIZE = 750;

    private ImageView mIngredientImage;
    private EditText mName;
    private EditText mDesc;
    private EditText mNotes;

    private AddIngredientViewModel mViewModel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_ingredient, container, false);

        AddIngredientViewModel.Factory factory = new AddIngredientViewModel.Factory(getActivity().getApplication(),
                getArguments().getLong(EXTRA_ID));
        mViewModel = ViewModelProviders.of(this, factory).get(AddIngredientViewModel.class);

        initToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeUi(mViewModel);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void subscribeUi(AddIngredientViewModel mViewModel) {

        mViewModel.getCurrentImagePath().observe(this, imagePath -> {
                Glide.with(getContext())
                        .load(imagePath != null ? imagePath : R.drawable.camera_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mIngredientImage);
        });

        if(!mViewModel.getIsNewIngredient()) {
            mViewModel.getIngredient().observe(this, ingredient -> {
                if(ingredient != null && mViewModel.getCurrentImagePath().getValue() == null) {
                    if (ingredient.image != null && !mViewModel.getIsImageRemoved()){
                        mViewModel.getCurrentImagePath().setValue(ingredient.image);
                    }
                    if (ingredient.name != null) mName.setText(ingredient.name);
                    if (ingredient.description != null) mDesc.setText(ingredient.description);
                    if (ingredient.notes != null) mNotes.setText(ingredient.notes);
                }
            });
        }

    }

    public static AddIngredientFragment getNewInstance(String name, Long ingredientId) {
        AddIngredientFragment fragment = new AddIngredientFragment();

        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);

        if ((ingredientId != null)) {
            arguments.putLong(EXTRA_ID, ingredientId);
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
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setHomeAsUpIndicator(R.drawable.ic_close);

            if(mViewModel.getIsNewIngredient()){
                mToolbar.setTitle("Новый ингредиент");
            } else {
                mToolbar.setTitle("Редактирование");
            }
        }
    }

    private void initViews(View view) {
        mIngredientImage = view.findViewById(R.id.image_add_ingredient);
        mName = view.findViewById(R.id.et_ingredient_name);
        mDesc = view.findViewById(R.id.et_ingredient_description);
        mNotes = view.findViewById(R.id.et_ingredient_notes);

        mIngredientImage.setOnClickListener(view1 -> showAddImageDialog());
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
                Log.d(TAG,"Some strange type of taking photo");
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

    private void saveIngredient() {
        mViewModel.saveIngredient(getContext(), mName.getText().toString(),
                mDesc.getText().toString(), mNotes.getText().toString());
        ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.INGREDIENTS_LIST);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_item_menu, menu);
        if(mViewModel.getIsNewIngredient()){
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
                saveIngredient();
                return true;
            case R.id.ab_delete:
                int count = mViewModel.deleteIngredient();
                if(count != 0) Toast.makeText(getContext(),
                        "Can't be deleted, used in " + count + "cocktails",Toast.LENGTH_SHORT).show();
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.INGREDIENTS_LIST);
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
        ((RouterProvider)getParentFragment()).getRouter().exit();
        return true;
    }

}
