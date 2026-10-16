package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(
    application: Application,
    repository: BarData,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mObservableIngredient = MediatorLiveData<Ingredient?>()
    private val mRelatedDrinks = MediatorLiveData<List<Drink>>()

    init {
        val ingredientId = savedStateHandle.get<Long>(ARG_ID) ?: -1L
        mObservableIngredient.value = null
        mRelatedDrinks.value = null
        mObservableIngredient.addSource(repository.getIngredient(ingredientId)) {
            mObservableIngredient.value = it
        }
        mRelatedDrinks.addSource(repository.getDrinksByIngredient(ingredientId)) {
            mRelatedDrinks.value = it
        }
    }

    fun getIngredient(): LiveData<Ingredient?> = mObservableIngredient

    fun getRelatedDrinks(): LiveData<List<Drink>> = mRelatedDrinks

    companion object {
        const val ARG_ID = "extra_id"
    }
}
