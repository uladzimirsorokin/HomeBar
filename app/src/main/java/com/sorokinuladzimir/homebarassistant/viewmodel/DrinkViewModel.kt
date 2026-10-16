package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrinkViewModel @Inject constructor(
    application: Application,
    repository: BarData,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mObservableDrink = MediatorLiveData<Drink?>()
    private val mObservableIngredients = MediatorLiveData<List<WholeCocktail>>()

    init {
        val drinkId = savedStateHandle.get<Long>(ARG_ID) ?: -1L
        mObservableDrink.value = null
        mObservableIngredients.value = null
        mObservableDrink.addSource(repository.getDrink(drinkId)) { mObservableDrink.value = it }
        mObservableIngredients.addSource(repository.getDrinkIngredients(drinkId)) {
            mObservableIngredients.value = it
        }
    }

    fun getDrink(): LiveData<Drink?> = mObservableDrink

    fun getIngredients(): LiveData<List<WholeCocktail>> = mObservableIngredients

    companion object {
        const val ARG_ID = "extra_id"
    }
}
