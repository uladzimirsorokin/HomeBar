package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.materialrangebar.RangeBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.Objects;

import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.ui.adapters.AddDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;
import sorokinuladzimir.com.homebarassistant.ui.utils.SpinnerGlassMatcher;
import sorokinuladzimir.com.homebarassistant.ui.utils.TastesHelper;
import sorokinuladzimir.com.homebarassistant.viewmodel.AddDrinkViewModel;
import sorokinuladzimir.com.homebarassistant.viewmodel.SharedViewModel;

import static android.app.Activity.RESULT_OK;
import static sorokinuladzimir.com.homebarassistant.Constants.Values.DEFAULT_IMAGE_SIZE;

public class AddDrinkFragment extends Fragment implements BackButtonListener,
        AddImageDialogFragment.AddImageDialogFragmentCallback,
        AddTastesDialogFragment.AddTastesDialogFragmentCallback{

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int OPEN_PICTURE_CODE = 2;


    private AddDrinkIngredientItemAdapter mAdapter;
    private AddDrinkViewModel mViewModel;
    private SharedViewModel mSharedIngredientsViewModel;
    private ImageView mDrinkImage;
    private EditText mEtName;
    private EditText mEtDescription;
    private TextView mTvTastes;
    private RangeBar mRangebarRating;
    private Switch mSwIsCarbonated;
    private Switch mSwIsAlcoholic;
    private Spinner mSpGlass;
    private EditText mEtNotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_add_drink, container, false);

        Long mDrinkId = -1L;
        if (getArguments() != null) {
            mDrinkId = getArguments().getLong(EXTRA_ID);
        }

        AddDrinkViewModel.Factory factory = new AddDrinkViewModel.Factory(Objects.requireNonNull(getActivity()).getApplication(), mDrinkId);

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

        drinkModel.getCurrentImagePath().observe(this, imagePath ->
                Glide.with(Objects.requireNonNull(getContext()))
                        .load(imagePath != null ? imagePath : R.drawable.camera_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mDrinkImage));

        if(!drinkModel.getIsNewDrink()) {
            drinkModel.getDrink().observe(this, drink -> {
                if(drink != null && drinkModel.getCurrentImagePath().getValue() == null) {
                    if (drink.getImage() != null && !drinkModel.getIsImageRemoved()){
                        drinkModel.getCurrentImagePath().setValue(drink.getImage());
                    }
                    if (drink.getName() != null) mEtName.setText(drink.getName());
                    if (drink.getDescription() != null) mEtDescription.setText(drink.getDescription());
                    if (drink.getTastes() != null) drinkModel.getTastesList().setValue(drink.getTastes());
                    mRangebarRating.setRangePinsByValue(0, drink.getRating());
                    mSwIsAlcoholic.setChecked(drink.isAlcoholic());
                    mSwIsCarbonated.setChecked(drink.isCarbonated());
                    int glassPos = SpinnerGlassMatcher.matchGlass(
                            getResources().getStringArray(R.array.glass_name_local), drink.getGlass());
                    mSpGlass.setSelection(glassPos == -1 ? 0 : glassPos);
                    if (drink.getNotes() != null) mEtNotes.setText(drink.getNotes());
                }
            });
        }

        drinkModel.getIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                mAdapter.setData(ingredients);
             }
        });

        if(!drinkModel.getIsNewDrink() && drinkModel.getIngredients().getValue() == null)
        drinkModel.getInitialIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                drinkModel.setInitialIngredients(ingredients, true);
                drinkModel.getInitialIngredients().removeObservers(this);
            }
        });

        drinkModel.getObservableLiveIngredients().observe(this, ingredients -> {
            if (ingredients != null) {
                drinkModel.updateIngredients(ingredients, mAdapter.getIngredients());
            }
        });

        drinkModel.getTastesList().observe(this, tastes -> mTvTastes.setText(TastesHelper.tastesToString(tastes)));

        sharedIngredients.getSelectedIds().observe(this, list -> {
            if (list != null) {
                drinkModel.setSelectedIds(list);
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews(View view) {

        mEtName = view.findViewById(R.id.et_drink_name);
        mEtDescription = view.findViewById(R.id.et_preparation);

        TextView mTvAddTastes = view.findViewById(R.id.tv_add_tastes);
        mTvTastes = view.findViewById(R.id.tv_tastes);

        mTvAddTastes.setOnClickListener(v -> showAddTastesDialog());

        mDrinkImage = view.findViewById(R.id.image_add_drink);

        RxPermissions rxPermissions = new RxPermissions(Objects.requireNonNull(getActivity()));
        mDrinkImage.setOnClickListener(image ->
                rxPermissions
                        .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(permission -> {
                            if (permission.granted) {
                                showAddImageDialog();
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // Denied permission with ask never again
                                Toast.makeText(getContext(), "denied",Toast.LENGTH_SHORT).show();
                            } else {
                                // Denied permission with ask never again
                                // Need to go to the settings
                                Toast.makeText(getContext(), "denied, ask never again",Toast.LENGTH_SHORT).show();
                            }
                        }));

        RecyclerView mRvIngredients = view.findViewById(R.id.rv_ingredients);
        mRvIngredients.setHasFixedSize(true);
        mRvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        mRvIngredients.addItemDecoration(itemDecoration);

        mAdapter = new AddDrinkIngredientItemAdapter(getContext(), (position, cocktail) -> {
            //mAdapter.deleteItem(position);
            mViewModel.removeIngredient(cocktail, mAdapter.getIngredients());
        });
        mRvIngredients.setAdapter(mAdapter);


        TextView mTvAddIngredients = view.findViewById(R.id.tv_add_ingredient);
        mTvAddIngredients.setOnClickListener(view1 -> {
            mViewModel.setIngredients(mAdapter.getIngredients(), true);
            mSharedIngredientsViewModel.selectIds(mViewModel.getIngredientIds());
            if (getParentFragment() != null) {
                ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK_INGREDIENTS);
            }
        });

        mRangebarRating = view.findViewById(R.id.rb_drink_rating);
        mSwIsCarbonated = view.findViewById(R.id.sw_search_carbonated);
        mSwIsAlcoholic = view.findViewById(R.id.sw_search_alcoholic);
        mEtNotes = view.findViewById(R.id.et_notes);
        mEtNotes.setOnTouchListener((v, event) -> {
            if (v.getId() == R.id.et_notes) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
            }
            return false;
        });
        mSpGlass = view.findViewById(R.id.spin_glass_type);
        ArrayAdapter<CharSequence> adapterGlass = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
                R.array.glass_name_local, android.R.layout.simple_spinner_item);
        adapterGlass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpGlass.setAdapter(adapterGlass);
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
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        ActionBar mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null){
            mToolbar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setHomeAsUpIndicator(R.drawable.ic_close);

            if(mViewModel.getIsNewDrink()){
                mToolbar.setTitle(R.string.title_new_cocktail);
            } else {
                mToolbar.setTitle(R.string.title_edit_cocktail);
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
        mViewModel.handleImage(imageUri, DEFAULT_IMAGE_SIZE, false);
    }

    private void handleTakenPicture(Uri imageUri){
        mViewModel.handleImage(imageUri, DEFAULT_IMAGE_SIZE, true);
    }

    void showAddImageDialog() {
        DialogFragment newFragment = AddImageDialogFragment.newInstance("Change photo",
                mViewModel.getCurrentImagePath().getValue() != null);
        newFragment.setTargetFragment(this,911);
        newFragment.show(Objects.requireNonNull(getFragmentManager()), "dialog");
    }

    void showAddTastesDialog() {
        DialogFragment newFragment = AddTastesDialogFragment.newInstance(getString(R.string.title_choose_tastes),
                mViewModel.getTastes(getResources().getStringArray(R.array.taste_name)));
        newFragment.setTargetFragment(this,911);
        newFragment.show(Objects.requireNonNull(getFragmentManager()), "dialog");
    }

    @Override
    public void addTastesDialogCallback(List<Integer> tastes) {
        mViewModel.setTastes(tastes, getResources().getStringArray(R.array.taste_name));
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
                mViewModel.removeCurrentImage();
                break;
            default:
                Log.d(Objects.requireNonNull(getContext()).getClass().getSimpleName(),"Some strange type of taking photo");
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getContext()).getPackageManager()) != null) {
            if (mViewModel.createPhotoFile() != null) {
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
        mViewModel.saveDrink(mEtName.getText().toString(),
                mEtDescription.getText().toString(),
                mAdapter.getIngredients(),
                Integer.valueOf(mRangebarRating.getRightPinValue()),
                getResources().getStringArray(R.array.glass_name_local)[mSpGlass.getSelectedItemPosition()],
                mSwIsCarbonated.isChecked(),
                mSwIsAlcoholic.isChecked(),
                mEtNotes.getText().toString());
        onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_edit_item_menu, menu);
        if(mViewModel.getIsNewDrink()){
            menu.findItem(R.id.ab_add).setTitle(R.string.menu_drink_add);
            menu.findItem(R.id.ab_delete).setVisible(false);
        } else {
            menu.findItem(R.id.ab_add).setTitle(R.string.menu_save);
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
                if (getParentFragment() != null) {
                    ((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.DRINKS_LIST);
                }
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
        if (getParentFragment() != null) {
            ((RouterProvider)getParentFragment()).getRouter().exit();
        }
        return true;
    }

}
