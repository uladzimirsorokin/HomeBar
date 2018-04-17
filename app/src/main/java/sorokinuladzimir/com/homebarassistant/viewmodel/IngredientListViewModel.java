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

import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.db.entity.Ingredient;


public class IngredientListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<Ingredient>> mObservableIngredients;
    private final LiveData<List<Ingredient>> mLiveIngredients;
    private LiveData<List<Ingredient>> mLiveSearchIngredients;

    public IngredientListViewModel(Application application) {
        super(application);

        mObservableIngredients = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableIngredients.setValue(null);

        mLiveIngredients = BarApp.getInstance().getBarRepository().getIngredients();

        mObservableIngredients.addSource(mLiveIngredients, mObservableIngredients::setValue);
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return mObservableIngredients;
    }

    public void searchIngredients(String query) {
        removeAllSources();
        if (query != null && !query.equals("")){
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
        mLiveSearchIngredients = BarApp.getInstance().getBarRepository().getIngredientsByName(query);
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
