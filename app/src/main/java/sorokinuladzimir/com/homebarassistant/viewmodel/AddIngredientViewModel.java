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

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.DataRepository;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;



public class AddIngredientViewModel extends AndroidViewModel {

    private final MediatorLiveData<Ingredient> mObservableIngredient;

    private final MediatorLiveData<String> mObservableCurrentImagePath;

    private final Long mIngredientId;

    private final LiveData<Ingredient> mLiveIngredient;

    private Uri mPhotoUri = null;

    private boolean mIsNewIngredient = true;

    private boolean mIsImageRemoved = false;

    private DataRepository mRepository;

    public AddIngredientViewModel(Application application, Long ingredientId) {
        super(application);

        mIngredientId = ingredientId;

        mRepository = BarApp.getInstance().getRepository();

        mObservableIngredient = new MediatorLiveData<>();
        mObservableIngredient.setValue(null);

        mObservableCurrentImagePath = new MediatorLiveData<>();
        mObservableCurrentImagePath.setValue(null);
        mRepository.resetIngredientImagePath();
        mObservableCurrentImagePath.addSource(mRepository.getObservableIngredientImagePath(), imagePath -> {
            mObservableCurrentImagePath.setValue(imagePath);
        });

        if(mIngredientId != -1L){
            mIsNewIngredient = false;
            mLiveIngredient = BarApp.getInstance().getRepository().loadIngredient(mIngredientId);
            mObservableIngredient.addSource(mLiveIngredient, ingredient -> mObservableIngredient.setValue(ingredient));
        } else {
            mLiveIngredient = null;
        }

    }

    public LiveData<Ingredient> getIngredient() {
        return mObservableIngredient;
    }

    public MutableLiveData<String> getCurrentImagePath() {
        return mObservableCurrentImagePath;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public boolean getIsNewIngredient() {
        return mIsNewIngredient;
    }

    public Boolean getIsImageRemoved() {
        return mIsImageRemoved;
    }

    public Uri createPhotoFile() {
        mPhotoUri = mRepository.createImageFile();
        return mPhotoUri;
    }

    public void handleImage(Uri imageUri, int sizeForScale, boolean deleteSource){
        mIsImageRemoved = false;
        removeImageFile(getIngredient().getValue() == null ? null : getIngredient().getValue().getImage(),
                getCurrentImagePath().getValue(), false);
        mRepository.saveImageToAlbum(imageUri, sizeForScale, deleteSource, true);
    }

    public void saveIngredient(String name, String description, String notes){
        removeImageFile(getIngredient().getValue() == null ? null : getIngredient().getValue().getImage(),
                getCurrentImagePath().getValue(), true);
        Ingredient ingredient = mObservableIngredient.getValue();
        if(ingredient == null) ingredient = new Ingredient();
        ingredient.setImage(mObservableCurrentImagePath.getValue());
        ingredient.setName(name);
        ingredient.setDescription(description);
        ingredient.setNotes(notes);
        mRepository.insertIngredient(ingredient);
    }

    public int deleteIngredient() {
        return mRepository.deleteIngredient(mIngredientId);
    }

    public void removeCurrentImage() {
        mIsImageRemoved = true;
        removeImageFile(getIngredient().getValue() == null ? null : getIngredient().getValue().getImage(),
                getCurrentImagePath().getValue(), false);
        getCurrentImagePath().setValue(null);
    }

    public void removeImageFile(String dbPath, String currentPath, Boolean savingIngredient){

        String deletePath = null;

        if (savingIngredient) {
            if (dbPath != null && currentPath != dbPath) {
                deletePath = dbPath;
            }
        } else {
            if (currentPath != null && currentPath != dbPath) {
                deletePath = currentPath;
            }
        }

        if (deletePath != null) BarApp.getInstance().getRepository().deleteImage(deletePath);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final Long mIngredientId;

        public Factory(@NonNull Application application, Long ingredientId) {
            mApplication = application;
            mIngredientId = ingredientId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AddIngredientViewModel(mApplication, mIngredientId);
        }
    }

}
