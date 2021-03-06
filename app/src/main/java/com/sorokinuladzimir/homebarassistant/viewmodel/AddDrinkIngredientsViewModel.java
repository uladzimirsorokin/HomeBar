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

import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient;

import java.util.List;

import static com.sorokinuladzimir.homebarassistant.BarApp.getDatabase;


public class AddDrinkIngredientsViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<Ingredient>> mObservableIngredients;

    private final MutableLiveData<String> mFilteredIngredientsQuery = new MutableLiveData<>();

    private final MutableLiveData<List<Long>> mLocalSelection = new MutableLiveData<>();


    public AddDrinkIngredientsViewModel(Application application) {
        super(application);
        mObservableIngredients = new MediatorLiveData<>();
        mObservableIngredients.setValue(null);
        LiveData<List<Ingredient>> liveIngredients = getDatabase().getIngredientDao().loadIngredients();
        mObservableIngredients.addSource(liveIngredients, mObservableIngredients::setValue);
    }

    public LiveData<String> getQuery() {
        return mFilteredIngredientsQuery;
    }

    public void searchIngredients(String query) {
        mFilteredIngredientsQuery.setValue(query);
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return mObservableIngredients;
    }

    public LiveData<List<Long>> getLocalSelection() {
        return mLocalSelection;
    }

    public void setLocalSelection(List<Long> list) {
        mLocalSelection.setValue(list);
    }
}
