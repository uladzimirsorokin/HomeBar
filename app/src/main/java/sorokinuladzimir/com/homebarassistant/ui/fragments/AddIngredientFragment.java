package sorokinuladzimir.com.homebarassistant.ui.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.MissingFormatArgumentException;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.BuildConfig;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.ui.utils.ImageHandler;
import sorokinuladzimir.com.homebarassistant.viewmodel.IngredientViewModel;

import static android.app.Activity.RESULT_OK;


public class AddIngredientFragment extends Fragment implements BackButtonListener, AddImageDialogFragment.AddImageDialogFragmentCallback {

    private final String TAG = "AddIngredientFragment";

    private static final String EXTRA_NAME = "extra_name";

    private static final String EXTRA_ID = "extra_id";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int OPEN_PICTURE_CODE = 2;

    private static final String ALBUM_NAME = "HomeBar";

    private static final int DEFAULT_IMAGE_SIZE = 750;

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private boolean mIsNewIngredient = true;

    private Long mIngredientId;

    private ImageHandler imageHandler = new ImageHandler();


    private ImageView mIngredientImage;
    private ActionBar mToolbar;

    private List<Ingredient> mIngredients;
    private Ingredient mIngredient = new Ingredient();
    private EditText mName;
    private EditText mDesc;
    private Bitmap mBitmap;

    private String mCurrentPhotoPath;
    private Uri photoURI;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_ingredient, container, false);

        mIngredientId = getArguments().getLong(EXTRA_ID);

        if(mIngredientId != null && mIngredientId != -1L){
            mIsNewIngredient = false;
        }

        initToolbar(rootView);
        initViews(rootView);

        return rootView;
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
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setHomeAsUpIndicator(R.drawable.ic_close);

            if(mIsNewIngredient){
                mToolbar.setTitle("Новый ингредиент");
            } else {
                mToolbar.setTitle("Редактирование");
            }
        }
    }

    private void initViews(View view) {
        mIngredientImage = view.findViewById(R.id.image_add_ingredient);
        mName = view.findViewById(R.id.et_ingredient_description);
        mDesc = view.findViewById(R.id.et_ingredient_notes);

        mIngredientImage.setOnClickListener(view1 -> showAddImageDialog());

        if(!mIsNewIngredient){
            mIngredients = BarApp.getInstance().getRepository().loadIngredients();
            mIngredient = mIngredients.get(Math.toIntExact(mIngredientId - 1));
            mName.setText(mIngredient.name);
            mDesc.setText(String.valueOf(mIngredient.id));

            if(mIngredient.image != null) {
                Glide.with(getContext())
                        .load(mIngredient.image)
                        .into(mIngredientImage);
            } else {
                Glide.with(getContext())
                        .load(R.drawable.camera_placeholder)
                        .into(mIngredientImage);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == OPEN_PICTURE_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {

                Uri imageUri = resultData.getData();
                handleChoosenPicture(imageUri);

            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            handleTakenPicture();

        }
    }

    private void handleChoosenPicture(Uri imageUri){

        try {

            mBitmap = imageHandler.getBitmapFromUri(getContext(), imageUri, DEFAULT_IMAGE_SIZE);
            mCurrentPhotoPath = imageHandler.saveImage(mBitmap, ALBUM_NAME);

            Glide.with(getContext())
                    .load(mBitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mIngredientImage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTakenPicture(){

        try {

            mBitmap = imageHandler.getBitmapFromUri(getContext(), photoURI, DEFAULT_IMAGE_SIZE);
            imageHandler.deleteImage(getContext(), mCurrentPhotoPath, AUTHORITY);
            mCurrentPhotoPath = imageHandler.saveImage(mBitmap, ALBUM_NAME);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(getContext())
                .load(mBitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(mIngredientImage);

    }

    void showAddImageDialog() {
        DialogFragment newFragment = AddImageDialogFragment.newInstance("Change photo");
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
            default:
                Log.d(TAG,"Some strange type of taking photo");
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = imageHandler.createImageFile(ALBUM_NAME);
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
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

        mIngredient.name = mName.getText().toString();
        mIngredient.description = mDesc.getText().toString();

        mIngredient.image = mCurrentPhotoPath;

        BarApp.getInstance().getRepository().insertIngredient(mIngredient);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_ingredient_menu, menu);
        if(mIsNewIngredient){
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
                ((RouterProvider)getParentFragment()).getRouter().exit();
                return true;
            case R.id.ab_add:
                saveIngredient();
                return true;
            case R.id.ab_delete:
                Toast.makeText(getContext(),"delete ingredient",Toast.LENGTH_SHORT).show();
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
