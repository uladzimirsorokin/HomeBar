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
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;


import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.BarDataRepository;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;
import sorokinuladzimir.com.homebarassistant.db.entity.WholeCocktail;
import sorokinuladzimir.com.homebarassistant.db.mapper.DrinkEntityToDrinkMapper;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;


public class RemoteDrinkViewModel extends AndroidViewModel {


    private final DrinkEntity mDrinkEntity;

    private final MediatorLiveData<Drink> mObservableDrink;

    private final MediatorLiveData<List<WholeCocktail>> mDrinkIngredients;

    private BarDataRepository mRepository;

    private Bitmap mBitmap = null;

    RemoteDrinkViewModel(@NonNull Application application, DrinkEntity drinkEntity) {
        super(application);

        mDrinkEntity = drinkEntity;

        mRepository = BarApp.getInstance().getBarRepository();
        getIngredients(drinkEntity.getId());

        mObservableDrink = new MediatorLiveData<>();
        mObservableDrink.setValue(DrinkEntityToDrinkMapper.getInstance().reverseMap(mDrinkEntity));

        mDrinkIngredients = new MediatorLiveData<>();
        mDrinkIngredients.setValue(null);

        mDrinkIngredients.addSource(mRepository.getRemoteDrinkIngredients(), mDrinkIngredients::setValue);

    }

    public MediatorLiveData<Drink> getDrink() {
        return mObservableDrink;
    }

    public MediatorLiveData<List<WholeCocktail>> getDrinkIngredients() {
        return mDrinkIngredients;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public void saveDrink() {
        mRepository.saveRemoteDrink(mDrinkEntity, mBitmap);
    }

    private void getIngredients(String drinkId) {
        mRepository.getRemoteDrinkIngredients(drinkId);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final DrinkEntity mDrinkEntity;

        public Factory(@NonNull Application application, DrinkEntity drinkEntity) {
            mApplication = application;
            mDrinkEntity = drinkEntity;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new RemoteDrinkViewModel(mApplication, mDrinkEntity);
        }
    }
}
