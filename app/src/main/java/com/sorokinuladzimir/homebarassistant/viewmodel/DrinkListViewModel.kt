package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrinkListViewModel @Inject constructor(
    application: Application,
    private val repository: BarData
) : AndroidViewModel(application) {

    private val mObservableDrinks = MediatorLiveData<List<Drink>>()
    private val mLiveDrinks: LiveData<List<Drink>> = repository.getLocalDrinks()
    private var mLiveSearchDrinks: LiveData<List<Drink>>? = null

    init {
        mObservableDrinks.value = null
        mObservableDrinks.addSource(mLiveDrinks) { mObservableDrinks.value = it }
    }

    fun getDrinks(): LiveData<List<Drink>> = mObservableDrinks

    fun searchDrinks(query: String) {
        removeAllSources()
        if (!query.isNullOrEmpty()) {
            addSearchResultSource(query)
        } else {
            restoreInitialSource()
        }
    }

    private fun removeAllSources() {
        mObservableDrinks.removeSource(mLiveDrinks)
        mLiveSearchDrinks?.let { mObservableDrinks.removeSource(it) }
    }

    private fun addSearchResultSource(query: String) {
        val source = repository.getDrinksByName(query)
        mLiveSearchDrinks = source
        mObservableDrinks.addSource(source) { mObservableDrinks.value = it }
    }

    private fun restoreInitialSource() {
        mObservableDrinks.addSource(mLiveDrinks) { mObservableDrinks.value = it }
    }

    fun restoreSources() {
        removeAllSources()
        restoreInitialSource()
    }
}
