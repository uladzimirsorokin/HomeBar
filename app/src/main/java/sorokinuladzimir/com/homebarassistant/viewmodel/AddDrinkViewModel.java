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
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
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

    private final Long mDrinkId;

    private DataRepository mRepository;

    private final MediatorLiveData<Drink> mObservableDrink;

    private final LiveData<Drink> mLiveDrink;

    private final MediatorLiveData<String> mObservableCurrentImagePath;

    private Uri mPhotoUri = null;

    private boolean mIsNewDrink = true;

    private boolean mIsImageRemoved = false;


    /*
        ready-to be exposed to adapter data obtained by merge(by id) of
        LiveIngredients (ingredient_id, name) <- updated by changes of List<Long> ingredientsId
        Local data  List<WholeCocktail> (join_id, ingredient_id, amount, unit)
    */
    private final MutableLiveData<List<WholeCocktail>> mObservableIngredients;

    //first load ingredient list
    private final MediatorLiveData<List<WholeCocktail>> mInitialIngredients = new MediatorLiveData<>();

    private List<Long> mIngredientIds = new ArrayList<>();
    private final HashMap<Long, WholeCocktail> tempIngredients = new HashMap<>();

    private LiveData<List<WholeCocktail>> mLiveListIngredients;
    private final MediatorLiveData<List<WholeCocktail>> mObservableLiveIngredients = new MediatorLiveData<>();


    public AddDrinkViewModel(Application application, Long drinkId) {
        super(application);

        mDrinkId = drinkId;
        mRepository = BarApp.getInstance().getRepository();

        mObservableDrink = new MediatorLiveData<>();
        mObservableDrink.setValue(null);

        mObservableCurrentImagePath = new MediatorLiveData<>();
        mObservableCurrentImagePath.setValue(null);
        mRepository.resetDrinkImagePath();
        mObservableCurrentImagePath.addSource(mRepository.getObservableDrinkImagePath(), imagePath -> {
            mObservableCurrentImagePath.setValue(imagePath);
        });

        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredients.setValue(null);
        mObservableDrink.setValue(null);

        if(mDrinkId != -1L){
            mIsNewDrink = false;
            mLiveDrink = mRepository.loadDrink(mDrinkId);
            mObservableDrink.addSource(mLiveDrink, ingredient -> mObservableDrink.setValue(ingredient));

            mInitialIngredients.addSource(mRepository.loadIngredients(mDrinkId), ingredients -> mInitialIngredients.setValue(ingredients));
        } else {
            mLiveDrink = null;

        }
    }


    public LiveData<Drink> getDrink() {
        return mObservableDrink;
    }


    // ingredients-related operations block

    public LiveData<List<WholeCocktail>> getInitialIngredients() {
        return mInitialIngredients;
    }

    public void setInitialIngredients(List<WholeCocktail> ingredients, boolean expose) {
        tempIngredients.clear();
        List<Long> newIds = new ArrayList<>();
        for (WholeCocktail wholeCocktail : ingredients) {
            newIds.add(wholeCocktail.ingredientId);
            tempIngredients.put(wholeCocktail.ingredientId, wholeCocktail);
        }
        setSelectedIds(newIds);
        if (expose) exposeIngredients();
    }

    public void setIngredients(List<WholeCocktail> ingredients, boolean expose) {
        tempIngredients.clear();
        for (WholeCocktail wholeCocktail : ingredients) {
            tempIngredients.put(wholeCocktail.ingredientId, wholeCocktail);
        }
        if (expose) exposeIngredients();
    }

    private void exposeIngredients() {
        List<WholeCocktail> ingredients = new ArrayList<>(tempIngredients.values());
        mObservableIngredients.setValue(ingredients);
    }

    public LiveData<List<WholeCocktail>> getIngredients() {
        return mObservableIngredients;
    }

    //ingredients name change or delete ingredients event handle + new selection
    public MediatorLiveData<List<WholeCocktail>> getObservableLiveIngredients() {
        return mObservableLiveIngredients;
    }

    public List<Long> getIngredientIds() {
        return mIngredientIds;
    }

    public void removeIngredient(WholeCocktail cocktail, List<WholeCocktail> cocktailsList) {
        /*cocktailsList.remove(cocktail);
        setIngredients(cocktailsList, true);*/
        mIngredientIds.remove(cocktail.ingredientId);
        setSelectedIds(mIngredientIds);
    }


    /*
    *  List<WholeCocktail> ingredients обновленный список ингридиентов (id, name) без колличества
    *  List<WholeCocktail> adapterCocktailList текущие ингридиенты из адаптера (amount, unit)
    *  HashMap<Long, WholeCocktail> tempIngredients старый список (joinId начальных ингридиентов)
    */
    public void updateIngredients(List<WholeCocktail> newIngredients, List<WholeCocktail> adapterCocktailList) {

        HashMap<Long,WholeCocktail> oldIngredientsList = new HashMap<>();
        oldIngredientsList.putAll(tempIngredients);

        if (adapterCocktailList.size() == 0) adapterCocktailList = new ArrayList<>(tempIngredients.values());

        setIngredients(newIngredients, false);

        for (WholeCocktail cocktail : adapterCocktailList) {
            if (tempIngredients.containsKey(cocktail.ingredientId)){
                tempIngredients.get(cocktail.ingredientId).unit = cocktail.unit;
                tempIngredients.get(cocktail.ingredientId).amount = cocktail.amount;
                tempIngredients.get(cocktail.ingredientId).jointableId = cocktail.jointableId;
            }
        }

        exposeIngredients();

        /*boolean isListChangedFlag = false;

        List<Long> oldIds = new ArrayList<>();
        oldIds.addAll(mIngredientIds);

        List<Long> newIds = new ArrayList<>();
        for (WholeCocktail cocktail : newIngredients) {
            newIds.add(cocktail.ingredientId);
        }

        if (oldIds.size() != newIds.size()) {
            oldIds.removeAll(newIds);
            isListChangedFlag = true;
            setSelectedIds(newIds);
            if (oldIds.size() != 0) {
                //remove some old add some new
                tempIngredients.keySet().removeAll(oldIds);

            } else {
                //old ingr + some new
            }
        } else {
            oldIds.removeAll(newIds);
            if (oldIds.size() != 0) {
                //remove some old add some new
                setSelectedIds(newIds);
                isListChangedFlag = true;
                tempIngredients.keySet().removeAll(oldIds);
            } else {
                //old list, mb updated names
            }
        }

        //update names
        for (WholeCocktail cocktail : newIngredients) {
            if (tempIngredients.containsKey(cocktail.ingredientId)) {
                if (!tempIngredients.get(cocktail.ingredientId).ingredientName.equals(cocktail.ingredientName)){
                    tempIngredients.get(cocktail.ingredientId).ingredientName = cocktail.ingredientName;
                    isListChangedFlag = true;
                }
            } else {
                tempIngredients.put(cocktail.ingredientId, cocktail);
                isListChangedFlag = true;
            }
        }

        for (WholeCocktail cocktail : adapterCocktailList) {
            if (tempIngredients.containsKey(cocktail.ingredientId)){
                tempIngredients.get(cocktail.ingredientId).unit = cocktail.unit;
                tempIngredients.get(cocktail.ingredientId).amount = cocktail.amount;
            }
        }


        if (isListChangedFlag) exposeIngredients();*/
    }

    public void setSelectedIds(List<Long> ingredientIds) {
        if (ingredientIds != null) {
            mIngredientIds = ingredientIds;
            if (mLiveListIngredients != null) mObservableLiveIngredients.removeSource(mLiveListIngredients);
            mLiveListIngredients = mRepository.loadCocktailIngredients(mIngredientIds);
            mObservableLiveIngredients.addSource(mLiveListIngredients,
                    ingredients -> mObservableLiveIngredients.setValue(ingredients));
        }
    }


    // image-related block

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public boolean getIsNewDrink() {
        return mIsNewDrink;
    }

    public Boolean getIsImageRemoved() {
        return mIsImageRemoved;
    }

    public MutableLiveData<String> getCurrentImagePath() {
        return mObservableCurrentImagePath;
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


    // save/delete operations with whole cocktail

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
