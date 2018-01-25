package sorokinuladzimir.com.homebarassistant.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.Constants;
import sorokinuladzimir.com.homebarassistant.R;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;
import sorokinuladzimir.com.homebarassistant.net.entity.IngredientEntity;
import sorokinuladzimir.com.homebarassistant.ui.adapters.SingleDrinkIngredientItemAdapter;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.BackButtonListener;
import sorokinuladzimir.com.homebarassistant.ui.subnavigation.RouterProvider;

/**
 * Created by sorok on 18.10.2017.
 */

public class SingleDrinkFragment extends Fragment implements BackButtonListener {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_BUNDLE = "extra_bundle";

    private DrinkEntity mCocktail;
    private ImageView mDrinkImage;
    private ActionBar mToolbar;
    private SingleDrinkIngredientItemAdapter mAdapter;
    private TextView mDescriptionText;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFab;

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

    private void initViews(View rootView) {
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsingToolbarLayout);

        mDrinkImage = rootView.findViewById(R.id.image_singledrink);
        /*Glide.with(getContext())
                .load(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + mCocktail.getId() + ".png")
                .into(mDrinkImage);*/

        Glide.with(getContext())
                .load(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + mCocktail.id + ".png")
                .into(new SimpleTarget<Drawable>() {

                          @Override
                          public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();
                                mDrinkImage.setImageBitmap(bitmap);
                                mDrinkImage.setDrawingCacheEnabled(true);

                                String filename = mCocktail.id + ".png";
                                String root = getContext().getFilesDir().getPath();
                                File myDir = new File(root + "/CocktailsLab");
                                if (! myDir.exists()){
                                    myDir.mkdirs();
                                }

                              File imageFile = new File(myDir, filename);

                              try {
                                  FileOutputStream fos = new FileOutputStream(imageFile);
                                  bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                  fos.close();
                                  Log.d("shit happens", "File created1" + imageFile.getPath());
                                  Log.d("shit happens2", "File created2" + imageFile.getPath());
                                  Log.d("shit happens3", "File created3" + imageFile.getPath());
                                  Log.d("shit happens4", "File created4" + imageFile.getPath());
                                  Log.d("shit happens5", "File created5" + imageFile.getPath());
                                  Log.d("shit happens6", "File created6" + imageFile.getPath());
                              } catch (FileNotFoundException e) {
                                  Log.d("shit happens", "File not found: " + e.getMessage());
                              } catch (IOException e) {
                                  Log.d("shit happens", "Error accessing file: " + e.getMessage());
                              }

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
        mAdapter = new SingleDrinkIngredientItemAdapter(new SingleDrinkIngredientItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(IngredientEntity item) {
                Toast.makeText(getContext(),item.name, Toast.LENGTH_LONG).show();
            }
        });

        mAdapter.setData(mCocktail.ingredients);
        rvIngredients.setAdapter(mAdapter);

        mDescriptionText = rootView.findViewById(R.id.tv_singledrink_descriptionPlain);
        mDescriptionText.setText(mCocktail.description);
        mCollapsingToolbarLayout.setTitle(mCocktail.name);
    }

    private void initFAB(View view){
        mFab = view.findViewById(R.id.single_drink_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Toast.makeText(getContext(),"Добавлен в мои коктейли", Toast.LENGTH_SHORT).show();
                Glide.with(getContext())
                        .load(getContext().getFilesDir().getPath()+"/CocktailsLab/vodka-bramble.png")
                        .into(mDrinkImage);*/
                //((RouterProvider)getParentFragment()).getRouter().navigateTo(Screens.ADD_DRINK, mCocktail);
                Drink drink = new Drink();
                drink.image = mCocktail.id;
                drink.name = mCocktail.name;
                drink.tastes = mCocktail.tastes;

                BarApp.getInstance().getRepository().insertDrink(drink);
                mFab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_done));
                mFab.setEnabled(false);
            }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.Extra.COCKTAIL, mCocktail);
    }
}
