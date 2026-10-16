package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddDrinkIngredientsViewModel @Inject constructor(
    application: Application,
    repository: BarData
) : AndroidViewModel(application) {

    private val mObservableIngredients = MediatorLiveData<List<Ingredient>>()
    private val mFilteredIngredientsQuery = MutableLiveData<String>()
    private val mLocalSelection = MutableLiveData<List<Long>>()

    init {
        mObservableIngredients.value = null
        mObservableIngredients.addSource(repository.getIngredients()) { mObservableIngredients.value = it }
    }

    fun getQuery(): LiveData<String> = mFilteredIngredientsQuery

    fun searchIngredients(query: String) {
        mFilteredIngredientsQuery.value = query
    }

    fun getIngredients(): LiveData<List<Ingredient>> = mObservableIngredients

    fun getLocalSelection(): LiveData<List<Long>> = mLocalSelection

    fun setLocalSelection(list: List<Long>) {
        mLocalSelection.value = list
    }
}
