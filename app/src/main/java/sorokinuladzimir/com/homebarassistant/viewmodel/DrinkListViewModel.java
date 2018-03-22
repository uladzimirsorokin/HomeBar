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
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import sorokinuladzimir.com.homebarassistant.BarApp;
import sorokinuladzimir.com.homebarassistant.db.entity.Drink;


public class DrinkListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<Drink>> mObservableDrinks;
    private final LiveData<List<Drink>> mLiveDrinks;
    private LiveData<List<Drink>> mLiveSearchDrinks;
    //private final List<Ingredient> mIngredients;

    public DrinkListViewModel(Application application) {
        super(application);


        mObservableDrinks = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableDrinks.setValue(null);

        mLiveDrinks = BarApp.getInstance().getRepository().getDrinks();

        mObservableDrinks.addSource(mLiveDrinks, drinks -> mObservableDrinks.setValue(drinks));
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<Drink>> getDrinks() {
        return mObservableDrinks;
    }

    public void searchDrinks(String query) {
        if (query != null && !query.equals("")){
            mObservableDrinks.removeSource(mLiveDrinks);
            mLiveSearchDrinks = BarApp.getInstance().getRepository().searchDrinksByName(query);
            mObservableDrinks.addSource(mLiveSearchDrinks, drinks -> mObservableDrinks.setValue(drinks));
        } else {
            mObservableDrinks.removeSource(mLiveSearchDrinks);
            mObservableDrinks.removeSource(mLiveDrinks);
            mObservableDrinks.addSource(mLiveDrinks, drinks -> mObservableDrinks.setValue(drinks));
        }

    }
}
