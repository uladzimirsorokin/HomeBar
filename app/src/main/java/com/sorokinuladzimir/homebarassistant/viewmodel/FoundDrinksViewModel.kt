package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.sorokinuladzimir.homebarassistant.BarDataRepository
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FoundDrinksViewModel @Inject constructor(
    application: Application,
    private val repository: BarDataRepository
) : AndroidViewModel(application) {

    private val mObservableRemoteDrinks = MediatorLiveData<List<DrinkEntity>>()

    init {
        mObservableRemoteDrinks.value = null
        mObservableRemoteDrinks.addSource(repository.getObservableRemoteDrinks()) {
            mObservableRemoteDrinks.value = it
        }
    }

    fun getDrinks(): LiveData<List<DrinkEntity>> = mObservableRemoteDrinks

    fun searchDrinks(query: String, searchType: BarDataRepository.QueryType, clearList: Boolean) {
        repository.getRemoteDrinks(query, searchType, clearList)
    }
}
