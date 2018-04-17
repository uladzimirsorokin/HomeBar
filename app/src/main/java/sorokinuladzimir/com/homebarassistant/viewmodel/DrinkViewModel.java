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
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;


public class DrinkViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<Drink> mObservableDrink;
    private final MediatorLiveData<List<WholeCocktail>> mObservableIngredients;


    private final Long mDrinkId;
    private final LiveData<Drink> mLiveDrink;
    private final LiveData<List<WholeCocktail>> mLiveIngredients;

    DrinkViewModel(Application application, Long drinkId) {
        super(application);

        mDrinkId = drinkId;

        mObservableDrink = new MediatorLiveData<>();
        mObservableIngredients = new MediatorLiveData<>();

        mObservableDrink.setValue(null);
        mObservableIngredients.setValue(null);


        mLiveDrink = BarApp.getInstance().getBarRepository().getDrink(mDrinkId);
        mLiveIngredients = BarApp.getInstance().getBarRepository().getDrinkIngredients(mDrinkId);

        mObservableDrink.addSource(mLiveDrink, mObservableDrink::setValue);
        mObservableIngredients.addSource(mLiveIngredients, mObservableIngredients::setValue);
    }

    public LiveData<Drink> getDrink() {
        return mObservableDrink;
    }

    public LiveData<List<WholeCocktail>> getIngredients() {
        return mObservableIngredients;
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
            return (T) new DrinkViewModel(mApplication, mDrinkId);
        }
    }

}
