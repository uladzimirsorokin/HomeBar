package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.mapper.DrinkEntityToDrinkMapper;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;
import sorokinuladzimir.com.homebarassistant.ui.adapters.SingleDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;

/**
 * Created by sorok on 18.10.2017.
 */
//TODO: need viewmodel to interact with repositiry to get net drink or smth like that

public class SingleDrinkFragment extends Fragment implements BackButtonListener {

    private final String TAG = "SingleDrinkFragment";

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private DrinkEntity mCocktail;
    private ImageView mDrinkImage;
    private ActionBar mToolbar;
    private SingleDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFab;
    private Bitmap mBitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_single_drink, container, false);

        if(savedInstanceState != null) {
            mCocktail = (DrinkEntity) savedInstanceState.getSerializable(Constants.Extra.COCKTAIL);
        }

        Bundle args = getArguments().getBundle(EXTRA_BUNDLE);
        if((args != null) && !args.isEmpty()){
            mCocktail = (DrinkEntity) args.getSerializable(Constants.Extra.COCKTAIL);
            args.clear();
        }

        initToolbar(rootView);
        initViews(rootView);
        initFAB(rootView);

        return rootView;
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
        mToolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(mToolbar != null)
            mToolbar.setDisplayHomeAsUpEnabled(true);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    private void initViews(View rootView) {
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsingToolbarLayout);

        mDrinkImage = rootView.findViewById(R.id.image_singledrink);

        Glide.with(getContext())
                .load(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + mCocktail.id + ".png")
                .into(new SimpleTarget<Drawable>() {

                          @Override
                          public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {

                              mBitmap = ((BitmapDrawable)resource).getBitmap();
                              mDrinkImage.setImageBitmap(mBitmap);
                              mDrinkImage.setDrawingCacheEnabled(true);
                          }
                });


        /*//mToolbar.setTitle(mCocktail.getName());
        String mColor = mCocktail.getColor().toLowerCase();
        if ((mColor != "any") && (mColor != "white")){
            String[] colors = getResources().getStringArray(R.array.colors_name);
            int colorId = 0;
            for (int i = 0; i < colors.length; i++) {
                if(colors[i].equals(mColor)) {
                    colorId = i;
                }
            }

            //if(colorId > 0) mToolbar.setBackgroundDrawable(new ColorDrawable(getResources().getIntArray(R.array.colors)[colorId]));
        }*/

        final RecyclerView rvIngredients = rootView.findViewById(R.id.recycler_singledrink_ingredients);
        rvIngredients.setHasFixedSize(true);
        rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SingleDrinkIngredientItemAdapter(ingredientItem -> Toast.makeText(getContext(), ingredientItem.name, Toast.LENGTH_LONG).show());

        mAdapter.setData(mCocktail.ingredients);
        rvIngredients.setAdapter(mAdapter);

        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);
        mDescriptionText.setText(mCocktail.description);
        mCollapsingToolbarLayout.setTitle(mCocktail.name);
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.single_drink_fab);
        mFab.setOnClickListener(view1 -> {
            //((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, mCocktail);
            addDrinkToDb(mCocktail, mBitmap);
            mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_done));
            mFab.setEnabled(false);
        });
    }

    //TODO: add image on diskIO thread, move method to repository
    private void addDrinkToDb(DrinkEntity cocktail, Bitmap bitmap){

        //Save image to albums
        String filename = cocktail.id + ".png";

        if(isExternalStorageWritable()){
            File albumDir = getAlbumStorageDir("HomeBar");
            File imageFile = new File(albumDir, filename);

            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Log.d(TAG, "File created1" + imageFile.getPath());
                cocktail.id = imageFile.getAbsolutePath();
                imageFile.exists();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }

        //Save raw ingredients without matching with existing and drinks to db
        Drink drink = DrinkEntityToDrinkMapper.getInstance().reverseMap(cocktail);
        Long drinkId = BarApp.getInstance().getRepository().insertDrink(drink);

        Long[] ingredientIds = BarApp.getInstance().getRepository().insertIngredients(drink.ingredients);

        List<DrinkIngredientJoin> list = new ArrayList<>();
        for (Long ingredientId: ingredientIds) {
            DrinkIngredientJoin item = new DrinkIngredientJoin();
            item.ingredientId = ingredientId;
            item.drinkId = drinkId;
            item.amount = 0L;
            item.unit = "Номер в списке " + list.size();
            list.add(item);
        }

        BarApp.getInstance().getRepository().insertDrinkIngredientJoin(list);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.Extra.COCKTAIL, mCocktail);
    }
}
