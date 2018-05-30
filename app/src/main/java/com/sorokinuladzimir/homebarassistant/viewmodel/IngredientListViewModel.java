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

import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient;

import java.util.List;

import static com.sorokinuladzimir.homebarassistant.BarApp.getBarRepository;
import static com.sorokinuladzimir.homebarassistant.BarApp.getDatabase;


public class IngredientListViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<Ingredient>> mObservableIngredients;
    private final LiveData<List<Ingredient>> mLiveIngredients;
    private LiveData<List<Ingredient>> mLiveSearchIngredients;

    public IngredientListViewModel(Application application) {
        super(application);
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredients.setValue(null);
        mLiveIngredients = getDatabase().getIngredientDao().loadIngredients();
        mObservableIngredients.addSource(mLiveIngredients, mObservableIngredients::setValue);
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return mObservableIngredients;
    }

    public void searchIngredients(String query) {
        removeAllSources();
        if (!TextUtils.isEmpty(query)) {
            addSearchResultSource(query);
        } else {
            restoreInitialSource();
        }
    }

    private void removeAllSources() {
        mObservableIngredients.removeSource(mLiveIngredients);
        mObservableIngredients.removeSource(mLiveSearchIngredients);
    }

    private void addSearchResultSource(String query) {
        mLiveSearchIngredients = getBarRepository().getIngredientsByName(query);
        mObservableIngredients.addSource(mLiveSearchIngredients, mObservableIngredients::setValue);
    }

    private void restoreInitialSource() {
        mObservableIngredients.addSource(mLiveIngredients, mObservableIngredients::setValue);
    }

    public void restoreSources() {
        removeAllSources();
        restoreInitialSource();
    }
}
