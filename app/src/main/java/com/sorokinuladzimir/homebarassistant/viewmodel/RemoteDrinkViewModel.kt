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
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class RemoteDrinkViewModel @Inject constructor(
    application: Application,
    private val repository: BarDataRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mDrinkEntity: DrinkEntity? =
        savedStateHandle.get<String>(ARG_ENTITY)
            ?.takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<DrinkEntity>(it) }

    private val mObservableDrink = MediatorLiveData<Drink?>()
    private val mDrinkIngredients = MediatorLiveData<List<WholeCocktail>>()
    private var mBitmap: Bitmap? = null

    init {
        mDrinkEntity?.id?.let { repository.getRemoteDrinkIngredients(it) }
        mObservableDrink.value = mDrinkEntity?.let {
            DrinkEntityToDrinkMapper.getInstance().reverseMap(it)
        }
        mDrinkIngredients.value = null
        mDrinkIngredients.addSource(repository.getRemoteDrinkIngredients()) {
            mDrinkIngredients.value = it
        }
    }

    fun getDrink(): MediatorLiveData<Drink?> = mObservableDrink

    fun getDrinkIngredients(): MediatorLiveData<List<WholeCocktail>> = mDrinkIngredients

    fun setBitmap(bitmap: Bitmap?) {
        mBitmap = bitmap
    }

    fun saveDrink() {
        mDrinkEntity?.let { repository.saveRemoteDrink(it, mBitmap) }
    }

    companion object {
        const val ARG_ENTITY = "entity"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
