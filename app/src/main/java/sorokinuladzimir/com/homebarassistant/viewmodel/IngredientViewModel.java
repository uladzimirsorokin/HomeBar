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
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;


public class IngredientViewModel extends AndroidViewModel {

    private final MediatorLiveData<Ingredient> mObservableIngredient;
    private final MediatorLiveData<List<Drink>> mRelatedDrinks;

    private IngredientViewModel(Application application, Long ingredientId) {
        super(application);
        mObservableIngredient = new MediatorLiveData<>();
        mRelatedDrinks = new MediatorLiveData<>();
        mObservableIngredient.setValue(null);
        mRelatedDrinks.setValue(null);
        LiveData<Ingredient> mLiveIngredient = BarApp.getBarRepository().getIngredient(ingredientId);
        mRelatedDrinks.addSource(BarApp.getBarRepository().getDrinksByIngredient(ingredientId), mRelatedDrinks::setValue);
        mObservableIngredient.addSource(mLiveIngredient, mObservableIngredient::setValue);
    }

    public LiveData<Ingredient> getIngredient() {
        return mObservableIngredient;
    }

    public LiveData<List<Drink>> getRelatedDrinks() {
        return mRelatedDrinks;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final Long mIngredientId;

        public Factory(@NonNull Application application, Long ingredientId) {
            mApplication = application;
            mIngredientId = ingredientId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new IngredientViewModel(mApplication, mIngredientId);
        }
    }

}
