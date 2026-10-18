package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IngredientListViewModel @Inject constructor(
    application: Application,
    private val repository: BarData
) : AndroidViewModel(application) {

    private val mObservableIngredients = MediatorLiveData<List<Ingredient>>()
    private val mLiveIngredients: LiveData<List<Ingredient>> = repository.getIngredients()
    private var mLiveSearchIngredients: LiveData<List<Ingredient>>? = null

    init {
        mObservableIngredients.value = null
        mObservableIngredients.addSource(mLiveIngredients) { mObservableIngredients.value = it }
    }

    fun getIngredients(): LiveData<List<Ingredient>> = mObservableIngredients

    fun searchIngredients(query: String) {
        removeAllSources()
        if (!query.isNullOrEmpty()) {
            addSearchResultSource(query)
        } else {
            restoreInitialSource()
        }
    }

    private fun removeAllSources() {
        mObservableIngredients.removeSource(mLiveIngredients)
        mLiveSearchIngredients?.let { mObservableIngredients.removeSource(it) }
    }

    private fun addSearchResultSource(query: String) {
        val source = repository.getIngredientsByName(query)
        mLiveSearchIngredients = source
        mObservableIngredients.addSource(source) { mObservableIngredients.value = it }
    }

    private fun restoreInitialSource() {
        mObservableIngredients.addSource(mLiveIngredients) { mObservableIngredients.value = it }
    }

    fun restoreSources() {
        removeAllSources()
        restoreInitialSource()
    }
}
