/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorokinuladzimir.com.homebarassistant.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.DataRepository;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.DrinkIngredientJoin;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;
import sorokinuladzimir.com.homebarassistant.db.entity.Taste;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.db.mapper.IngredientToWholeCocktailMapper;
import sorokinuladzimir.com.homebarassistant.db.mapper.WholeCocktailToDrinkIngredientJoinMapper;


public class AddDrinkViewModel extends AndroidViewModel {

    private final MediatorLiveData<Drink> mObservableDrink;

    private final MediatorLiveData<String> mObservableCurrentImagePath;

    private final MediatorLiveData<List<WholeCocktail>> mObservableIngredients;

    private final LiveData<List<WholeCocktail>> mLiveIngredients;

    private final Long mDrinkId;

    private final LiveData<Drink> mLiveDrink;

    private Uri mPhotoUri = null;

    private boolean mIsNewDrink = true;

    private boolean mIsImageRemoved = false;

    private DataRepository mRepository;

    private LiveData<List<Ingredient>> mLiveListIngredients;

    private List<Long> mIngredientIds = new ArrayList<>();

    private final LiveData<List<Long>> mLiveInitialIngredientIds;


    public AddDrinkViewModel(Application application, Long drinkId) {
        super(application);

        mDrinkId = drinkId;

        mRepository = BarApp.getInstance().getRepository();

        mObservableDrink = new MediatorLiveData<>();
        mObservableDrink.setValue(null);
        mObservableIngredients = new MediatorLiveData<>();
        mObservableDrink.setValue(null);

        mObservableCurrentImagePath = new MediatorLiveData<>();
        mObservableCurrentImagePath.setValue(null);
        mRepository.resetDrinkImagePath();
        mObservableCurrentImagePath.addSource(mRepository.getObservableDrinkImagePath(), imagePath -> {
            mObservableCurrentImagePath.setValue(imagePath);
        });

        if(mDrinkId != -1L){
            mIsNewDrink = false;
            mLiveDrink = mRepository.loadDrink(mDrinkId);
            mObservableDrink.addSource(mLiveDrink, ingredient -> mObservableDrink.setValue(ingredient));
            mLiveIngredients = mRepository.loadIngredients(mDrinkId);

            mLiveInitialIngredientIds = mRepository.loadIngredientIds(mDrinkId);

            mObservableIngredients.addSource(mLiveIngredients, ingredients -> mObservableIngredients.setValue(ingredients));
        } else {
            mLiveDrink = null;
            mLiveIngredients = null;
            mObservableIngredients.setValue(new ArrayList<>());
            mLiveInitialIngredientIds = null;
        }
    }

    public LiveData<Drink> getDrink() {
        return mObservableDrink;
    }

    public LiveData<List<WholeCocktail>> getIngredients() {
        return mObservableIngredients;
    }

    public LiveData<List<Long>> getInitialIngredientIds() {
        return mLiveInitialIngredientIds;
    }

    public List<Long> getIngredientIds() {
        return mIngredientIds;
    }

    public MutableLiveData<String> getCurrentImagePath() {
        return mObservableCurrentImagePath;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public boolean getIsNewDrink() {
        return mIsNewDrink;
    }

    public Boolean getIsImageRemoved() {
        return mIsImageRemoved;
    }

    public Uri createPhotoFile(Context context, String albumName) {
        mPhotoUri = BarApp.getInstance().getRepository().createImageFile(context, albumName);
        return mPhotoUri;
    }

    public void handleImage(Context context, String albumName, Uri imageUri, int sizeForScale, boolean deleteSource){
        mIsImageRemoved = false;
        removeImageFile(context, getDrink().getValue() == null ? null : getDrink().getValue().image,
                getCurrentImagePath().getValue(), false);
        mRepository.saveImageToAlbum(context, albumName, imageUri, sizeForScale, deleteSource, false);
    }

    public void removeCurrentImage(Context context) {
        mIsImageRemoved = true;
        removeImageFile(context, getDrink().getValue() == null ? null : getDrink().getValue().image,
                getCurrentImagePath().getValue(), false);
        getCurrentImagePath().setValue(null);
    }

    public void removeImageFile(Context context, String dbPath, String currentPath, Boolean save){

        String deletePath = null;

        if (save) {
            if (dbPath != null && currentPath != dbPath) {
                deletePath = dbPath;
            }
        } else {
            if (currentPath != null && currentPath != dbPath) {
                deletePath = currentPath;
            }
        }

        if (deletePath != null) BarApp.getInstance().getRepository().deleteImage(context, deletePath);
    }

    public void setSelectedIds(List<Long> ingredientIds) {

        if (ingredientIds != null) {
            mIngredientIds = ingredientIds;
            mLiveListIngredients = mRepository.loadIngredients(mIngredientIds);

            mObservableIngredients.addSource(mLiveListIngredients,
                    ingredients -> mObservableIngredients
                            .setValue(IngredientToWholeCocktailMapper.getInstance().reverseMap(ingredients)));

        }

    }

    public void setIngredientIds(List<Long> mIngredientIds) {
        this.mIngredientIds = mIngredientIds;
    }

    public void removeIngredient(WholeCocktail cocktail) {
        mObservableIngredients.getValue().remove(mObservableIngredients.getValue().indexOf(cocktail));
        mIngredientIds.remove(cocktail.id);
    }

    public void saveDrink(Context context, String name, String description, ArrayList<Taste> tastes, List<WholeCocktail> ingredients){

        removeImageFile(context, getDrink().getValue() == null ? null : getDrink().getValue().image,
                getCurrentImagePath().getValue(), true);

        Drink drink = mObservableDrink.getValue();
        if(drink == null) drink = new Drink();

        drink.image = mObservableCurrentImagePath.getValue();
        drink.name = "name stub";
        drink.description = "description stub";
        ArrayList<Taste> tastes1 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Taste taste1 = new Taste();
            taste1.text = "Сладкий" + i;
            tastes1.add(taste1);
        }
        drink.tastes = tastes1;
        List<DrinkIngredientJoin> ingredientsList = WholeCocktailToDrinkIngredientJoinMapper.getInstance().reverseMap(ingredients);
        mRepository.insertDrink(drink, ingredientsList);
    }

    public void deleteDrink() {
        mRepository.deleteDrink(mDrinkId);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final Long mDrinkId;

        public Factory(@NonNull Application application, Long drinkId) {
            mApplication = application;
            mDrinkId = drinkId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AddDrinkViewModel(mApplication, mDrinkId);
        }
    }

}
