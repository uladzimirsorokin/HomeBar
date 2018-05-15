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
import sorokinuladzimir.com.homebarassistant.BarDataRepository.QueryType;
import sorokinuladzimir.com.homebarassistant.net.entity.DrinkEntity;


public class FoundDrinksViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<DrinkEntity>> mObservableRemoteDrinks;

    public FoundDrinksViewModel(Application application) {
        super(application);
        mObservableRemoteDrinks = new MediatorLiveData<>();
        mObservableRemoteDrinks.setValue(null);
        mObservableRemoteDrinks.addSource(BarApp.getBarRepository().getObservableRemoteDrinks(), mObservableRemoteDrinks::setValue);
    }

    public LiveData<List<DrinkEntity>> getDrinks() {
        return mObservableRemoteDrinks;
    }

    public void searchDrinks(String query, QueryType searchType, boolean clearList) {
        BarApp.getBarRepository().getRemoteDrinks(query, searchType, clearList);
    }


}
