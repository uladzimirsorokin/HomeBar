package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sorokinuladzimir.homebarassistant.BarDataRepository
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.ai.AiTranslator
import com.sorokinuladzimir.homebarassistant.core.util.MeasureParser
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.db.mapper.DrinkEntityToDrinkMapper
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.CocktailDbImages
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteDrinkViewModel @Inject constructor(
    application: Application,
    private val repository: BarDataRepository,
    private val aiTranslator: AiTranslator,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mObservableDrink = MediatorLiveData<Drink?>()
    private val mDrinkIngredients = MediatorLiveData<List<WholeCocktail>>()
    private var mEntity: DrinkEntity? = null
    private var mBitmap: Bitmap? = null
    private var translationRequested = false

    private val allowedUnits: List<String> =
        application.resources.getStringArray(R.array.ingredient_unit).toList()

    init {
        val drinkId = savedStateHandle.get<String>(ARG_ID)?.takeIf { it.isNotBlank() }
        mDrinkIngredients.value = null
        if (drinkId != null) {
            mObservableDrink.addSource(repository.getRemoteDrink(drinkId)) { entity ->
                publish(entity)
                maybeTranslate(entity)
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

    private fun publish(entity: DrinkEntity?) {
        mEntity = entity
        mObservableDrink.value = entity?.let { DrinkEntityToDrinkMapper.getInstance().reverseMap(it) }
        mDrinkIngredients.value = entity?.ingredients.orEmpty().map { ingredient ->
            val parsed = MeasureParser.parse(ingredient.measure, allowedUnits)
            WholeCocktail().apply {
                ingredientName = ingredient.name
                amount = parsed.amount
                unit = parsed.unit
                image = CocktailDbImages.ingredient(ingredient.name)
            }
        }
    }

    private fun maybeTranslate(entity: DrinkEntity?) {
        if (entity == null || translationRequested) return
        val target = aiTranslator.appLanguageCode()
        if (!aiTranslator.needsTranslation(entity.description, target)) return
        translationRequested = true
        viewModelScope.launch {
            when (val result = aiTranslator.translateInstructions(entity.description, target)) {
                is ApiResult.Success -> {
                    val translated = result.data
                    if (!translated.isNullOrBlank() && mEntity?.id == entity.id) {
                        publish(entity.copy(description = translated))
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    companion object {
        const val ARG_ID = "drinkId"
    }
}
