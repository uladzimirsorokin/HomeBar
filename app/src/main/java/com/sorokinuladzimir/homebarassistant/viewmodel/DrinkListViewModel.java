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
import android.text.TextUtils;

import com.sorokinuladzimir.homebarassistant.db.entity.Drink;

import java.util.List;

import static com.sorokinuladzimir.homebarassistant.BarApp.getBarRepository;
import static com.sorokinuladzimir.homebarassistant.BarApp.getDatabase;


public class DrinkListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<Drink>> mObservableDrinks;
    private final LiveData<List<Drink>> mLiveDrinks;
    private LiveData<List<Drink>> mLiveSearchDrinks;

    public DrinkListViewModel(Application application) {
        super(application);
        mObservableDrinks = new MediatorLiveData<>();
        mObservableDrinks.setValue(null);
        mLiveDrinks = getDatabase().getDrinkDao().loadAllDrinks();
        mObservableDrinks.addSource(mLiveDrinks, mObservableDrinks::setValue);
    }

    public LiveData<List<Drink>> getDrinks() {
        return mObservableDrinks;
    }

    public void searchDrinks(String query) {
        removeAllSources();
        if (!TextUtils.isEmpty(query)) {
            addSearchResultSource(query);
        } else {
            restoreInitialSource();
        }
    }

    private void removeAllSources() {
        mObservableDrinks.removeSource(mLiveDrinks);
        mObservableDrinks.removeSource(mLiveSearchDrinks);
    }

    private void addSearchResultSource(String query) {
        mLiveSearchDrinks = getBarRepository().getDrinksByName(query);
        mObservableDrinks.addSource(mLiveSearchDrinks, mObservableDrinks::setValue);
    }

    private void restoreInitialSource() {
        mObservableDrinks.addSource(mLiveDrinks, mObservableDrinks::setValue);
    }

    public void restoreSources() {
        removeAllSources();
        restoreInitialSource();
    }
}
