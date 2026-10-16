package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import com.sorokinuladzimir.homebarassistant.BarDataRepository
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.db.mapper.DrinkEntityToDrinkMapper
import com.sorokinuladzimir.homebarassistant.net.CocktailDbImages
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RemoteDrinkViewModel @Inject constructor(
    application: Application,
    private val repository: BarDataRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mObservableDrink = MediatorLiveData<Drink?>()
    private val mDrinkIngredients = MediatorLiveData<List<WholeCocktail>>()
    private var mEntity: DrinkEntity? = null
    private var mBitmap: Bitmap? = null

    init {
        val drinkId = savedStateHandle.get<String>(ARG_ID)?.takeIf { it.isNotBlank() }
        mDrinkIngredients.value = null
        if (drinkId != null) {
            mObservableDrink.addSource(repository.getRemoteDrink(drinkId)) { entity ->
                mEntity = entity
                mObservableDrink.value =
                    entity?.let { DrinkEntityToDrinkMapper.getInstance().reverseMap(it) }
                mDrinkIngredients.value = entity?.ingredients.orEmpty().map { ingredient ->
                    WholeCocktail().apply {
                        ingredientName = ingredient.name
                        amount = ingredient.measure
                        image = CocktailDbImages.ingredient(ingredient.name)
                    }
                }
            }
        }
    }

    fun getDrink(): MediatorLiveData<Drink?> = mObservableDrink

    fun getDrinkIngredients(): MediatorLiveData<List<WholeCocktail>> = mDrinkIngredients

    fun setBitmap(bitmap: Bitmap?) {
        mBitmap = bitmap
    }

    fun saveDrink() {
        mEntity?.let { repository.saveRemoteDrink(it, mBitmap) }
    }

    companion object {
        const val ARG_ID = "drinkId"
    }
}
