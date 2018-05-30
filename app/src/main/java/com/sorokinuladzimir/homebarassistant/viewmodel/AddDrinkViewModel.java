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

package com.sorokinuladzimir.homebarassistant.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.sorokinuladzimir.homebarassistant.BarApp;
import com.sorokinuladzimir.homebarassistant.BarDataRepository;
import com.sorokinuladzimir.homebarassistant.db.entity.Drink;
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin;
import com.sorokinuladzimir.homebarassistant.db.entity.Glass;
import com.sorokinuladzimir.homebarassistant.db.entity.Taste;
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail;
import com.sorokinuladzimir.homebarassistant.db.mapper.WholeCocktailToDrinkIngredientJoinMapper;
import com.sorokinuladzimir.homebarassistant.ui.utils.TastesHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class AddDrinkViewModel extends AndroidViewModel {

    private final Long mDrinkId;
    private final MediatorLiveData<Drink> mObservableDrink;
    private final MediatorLiveData<String> mObservableCurrentImagePath;
    private final MutableLiveData<List<WholeCocktail>> mObservableIngredients;
    private final MediatorLiveData<List<WholeCocktail>> mInitialIngredients = new MediatorLiveData<>();
    private final LinkedHashMap<Long, WholeCocktail> tempIngredients = new LinkedHashMap<>();
    private final MediatorLiveData<List<WholeCocktail>> mObservableLiveIngredients = new MediatorLiveData<>();
    private final MediatorLiveData<ArrayList<Taste>> mCurrentTastesList = new MediatorLiveData<>();
    private final BarDataRepository mRepository;
    private Uri mPhotoUri = null;
    private boolean mIsNewDrink = true;
    private boolean mIsImageRemoved = false;
    private List<Long> mIngredientIds = new ArrayList<>();
    private LiveData<List<WholeCocktail>> mLiveListIngredients;

    AddDrinkViewModel(Application application, Long drinkId) {
        super(application);
        mDrinkId = drinkId;
        mRepository = BarApp.getBarRepository();
        mObservableDrink = new MediatorLiveData<>();
        mObservableDrink.setValue(null);
        mObservableCurrentImagePath = new MediatorLiveData<>();
        mObservableCurrentImagePath.setValue(null);
        mRepository.resetDrinkImagePath();
        mObservableCurrentImagePath.addSource(mRepository.getObservableDrinkImagePath(), mObservableCurrentImagePath::setValue);
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredients.setValue(null);
        mObservableDrink.setValue(null);
        LiveData<Drink> mLiveDrink;
        if (mDrinkId != -1L) {
            mIsNewDrink = false;
            mLiveDrink = mRepository.getDrink(mDrinkId);
            mObservableDrink.addSource(mLiveDrink, mObservableDrink::setValue);
            mInitialIngredients.addSource(mRepository.getDrinkIngredients(mDrinkId), mInitialIngredients::setValue);
        } else {
            mLiveDrink = null;
        }
    }

    public LiveData<Drink> getDrink() {
        return mObservableDrink;
    }

    public LiveData<List<WholeCocktail>> getInitialIngredients() {
        return mInitialIngredients;
    }

    public void setInitialIngredients(List<WholeCocktail> ingredients, boolean expose) {
        tempIngredients.clear();
        List<Long> newIds = new ArrayList<>();
        for (WholeCocktail wholeCocktail : ingredients) {
            newIds.add(wholeCocktail.getIngredientId());
            tempIngredients.put(wholeCocktail.getIngredientId(), wholeCocktail);
        }
        setSelectedIds(newIds);
        if (expose) exposeIngredients();
    }

    public void setIngredients(List<WholeCocktail> ingredients, boolean expose) {
        tempIngredients.clear();
        for (WholeCocktail wholeCocktail : ingredients) {
            tempIngredients.put(wholeCocktail.getIngredientId(), wholeCocktail);
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

    public MediatorLiveData<List<WholeCocktail>> getObservableLiveIngredients() {
        return mObservableLiveIngredients;
    }

    public List<Long> getIngredientIds() {
        return mIngredientIds;
    }

    public void removeIngredient(WholeCocktail cocktail) {
        mIngredientIds.remove(cocktail.getIngredientId());
        setSelectedIds(mIngredientIds);
    }

    public void setSelectedIds(List<Long> ingredientIds) {
        if (ingredientIds != null) {
            mIngredientIds = ingredientIds;
            if (mLiveListIngredients != null)
                mObservableLiveIngredients.removeSource(mLiveListIngredients);
            mLiveListIngredients = mRepository.getListIngredientsNames(mIngredientIds);
            mObservableLiveIngredients.addSource(mLiveListIngredients,
                    mObservableLiveIngredients::setValue);
        }
    }

    public void updateIngredients(List<WholeCocktail> newIngredients, List<WholeCocktail> adapterCocktailList) {
        boolean isListChangedFlag;
        isListChangedFlag = updateListKeys(newIngredients);
        isListChangedFlag = updateNames(newIngredients, isListChangedFlag);
        updateAmountAndUnit(adapterCocktailList);
        if (isListChangedFlag) exposeIngredients();
    }

    private boolean updateListKeys(List<WholeCocktail> newIngredients) {
        boolean isChanged = false;
        List<Long> oldIds = new ArrayList<>(tempIngredients.keySet());
        List<Long> newIds = new ArrayList<>();
        for (WholeCocktail cocktail : newIngredients) {
            newIds.add(cocktail.getIngredientId());
        }
        if (oldIds.size() != newIds.size()) {
            oldIds.removeAll(newIds);
            isChanged = true;
            if (!oldIds.isEmpty()) {
                tempIngredients.keySet().removeAll(oldIds);
            }
        } else {
            oldIds.removeAll(newIds);
            if (!oldIds.isEmpty()) {
                isChanged = true;
                tempIngredients.keySet().removeAll(oldIds);
            }
        }
        return isChanged;
    }

    private void updateAmountAndUnit(List<WholeCocktail> adapterCocktailList) {
        if (adapterCocktailList.isEmpty())
            adapterCocktailList = new ArrayList<>(tempIngredients.values());
        for (WholeCocktail cocktail : adapterCocktailList) {
            if (tempIngredients.containsKey(cocktail.getIngredientId())) {
                tempIngredients.get(cocktail.getIngredientId()).setUnit(cocktail.getUnit());
                tempIngredients.get(cocktail.getIngredientId()).setAmount(cocktail.getAmount());
            }
        }
    }

    private boolean updateNames(List<WholeCocktail> newIngredients, boolean isListChangedFlag) {
        boolean isChanged = isListChangedFlag;
        for (WholeCocktail cocktail : newIngredients) {
            if (tempIngredients.containsKey(cocktail.getIngredientId())) {
                if (!tempIngredients.get(cocktail.getIngredientId()).getIngredientName().equals(cocktail.getIngredientName())) {
                    tempIngredients.get(cocktail.getIngredientId()).setIngredientName(cocktail.getIngredientName());
                    isChanged = true;
                }
            } else {
                tempIngredients.put(cocktail.getIngredientId(), cocktail);
                isChanged = true;
            }
        }
        return isChanged;
    }

    public MediatorLiveData<ArrayList<Taste>> getTastesList() {
        return mCurrentTastesList;
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

    public MutableLiveData<String> getCurrentImagePath() {
        return mObservableCurrentImagePath;
    }

    public Uri createPhotoFile() {
        mPhotoUri = mRepository.createImageFile();
        return mPhotoUri;
    }

    public void handleImage(Uri imageUri, int sizeForScale, boolean deleteSource) {
        mIsImageRemoved = false;
        removeImageFile(getDrink().getValue() == null ? null : getDrink().getValue().getImage(),
                getCurrentImagePath().getValue(), false);
        mRepository.saveImageToAlbum(imageUri, sizeForScale, deleteSource, false);
    }

    public void removeCurrentImage() {
        mIsImageRemoved = true;
        removeImageFile(getDrink().getValue() == null ? null : getDrink().getValue().getImage(),
                getCurrentImagePath().getValue(), false);
        getCurrentImagePath().setValue(null);
    }

    private void removeImageFile(String dbPath, String currentPath, Boolean save) {
        String deletePath = null;
        if (save) {
            if (dbPath != null && !TextUtils.equals(currentPath, dbPath)) {
                deletePath = dbPath;
            }
        } else {
            if (currentPath != null && !TextUtils.equals(currentPath, dbPath)) {
                deletePath = currentPath;
            }
        }
        if (deletePath != null) mRepository.deleteImage(deletePath);
    }

    public void saveDrink(String name, String description, List<WholeCocktail> ingredients, int rating,
                          String glassName, boolean carbonated, boolean alcoholic, String notes) {
        removeImageFile(getDrink().getValue() == null ? null : getDrink().getValue().getImage(),
                getCurrentImagePath().getValue(), true);
        Drink drink = mObservableDrink.getValue();
        if (drink == null) drink = new Drink();
        drink.setImage(mObservableCurrentImagePath.getValue());
        drink.setName(name != null ? name : "");
        drink.setDescription(description != null ? description : "");
        drink.setTastes(mCurrentTastesList.getValue());
        drink.setRating(rating);
        drink.setCarbonated(carbonated);
        drink.setAlcoholic(alcoholic);
        Glass glass = new Glass();
        glass.setGlassName(glassName);
        drink.setGlass(glass);
        drink.setNotes(notes != null ? notes : "");
        List<DrinkIngredientJoin> ingredientsList = WholeCocktailToDrinkIngredientJoinMapper.getInstance().reverseMap(ingredients);
        mRepository.saveDrink(drink, ingredientsList);
    }

    public void deleteDrink() {
        mRepository.deleteDrink(mDrinkId);
    }

    public void setTastes(List<Integer> selectedIngredients, String[] tastesArray) {
        mCurrentTastesList.setValue(
                new ArrayList<>(TastesHelper.toTastesList(selectedIngredients, tastesArray)));
    }

    public List<Integer> getTastes(String[] tastesArray) {
        return TastesHelper.getAsIntegerList(mCurrentTastesList.getValue(), tastesArray);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final Long mDrinkId;

        public Factory(@NonNull Application application, Long drinkId) {
            mApplication = application;
            mDrinkId = drinkId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AddDrinkViewModel(mApplication, mDrinkId);
        }
    }

}
