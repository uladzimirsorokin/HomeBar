package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.Glass
import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.db.mapper.WholeCocktailToDrinkIngredientJoinMapper
import com.sorokinuladzimir.homebarassistant.ui.utils.TastesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.inject.Inject

@HiltViewModel
class AddDrinkViewModel @Inject constructor(
    application: Application,
    private val repository: BarData,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mDrinkId: Long = savedStateHandle.get<Long>(ARG_ID) ?: -1L
    private val mObservableDrink = MediatorLiveData<Drink?>()
    private val mObservableCurrentImagePath = MediatorLiveData<String?>()
    private val mObservableIngredients = MediatorLiveData<List<WholeCocktail>>()
    private val mInitialIngredients = MediatorLiveData<List<WholeCocktail>>()
    private val tempIngredients = LinkedHashMap<Long, WholeCocktail>()
    private val mObservableLiveIngredients = MediatorLiveData<List<WholeCocktail>>()
    private val mCurrentTastesList = MediatorLiveData<ArrayList<Taste>>()
    private var mPhotoUri: Uri? = null
    private var mIsNewDrink = true
    private var mIsImageRemoved = false
    private var mIngredientIds: MutableList<Long> = ArrayList()
    private var mLiveListIngredients: LiveData<List<WholeCocktail>>? = null

    init {
        mObservableDrink.value = null
        mObservableCurrentImagePath.value = null
        repository.resetDrinkImagePath()
        mObservableCurrentImagePath.addSource(repository.getObservableDrinkImagePath()) {
            mObservableCurrentImagePath.value = it
        }
        mObservableIngredients.value = null
        if (mDrinkId != -1L) {
            mIsNewDrink = false
            mObservableDrink.addSource(repository.getDrink(mDrinkId)) { mObservableDrink.value = it }
            mInitialIngredients.addSource(repository.getDrinkIngredients(mDrinkId)) {
                mInitialIngredients.value = it
            }
        }
    }

    fun getDrink(): LiveData<Drink?> = mObservableDrink

    fun getInitialIngredients(): LiveData<List<WholeCocktail>> = mInitialIngredients

    fun setInitialIngredients(ingredients: List<WholeCocktail>, expose: Boolean) {
        tempIngredients.clear()
        val newIds = ArrayList<Long>()
        for (wholeCocktail in ingredients) {
            wholeCocktail.ingredientId?.let {
                newIds.add(it)
                tempIngredients[it] = wholeCocktail
            }
        }
        setSelectedIds(newIds)
        if (expose) exposeIngredients()
    }

    fun setIngredients(ingredients: List<WholeCocktail>, expose: Boolean) {
        tempIngredients.clear()
        for (wholeCocktail in ingredients) {
            wholeCocktail.ingredientId?.let { tempIngredients[it] = wholeCocktail }
        }
        if (expose) exposeIngredients()
    }

    private fun exposeIngredients() {
        mObservableIngredients.value = ArrayList(tempIngredients.values)
    }

    fun getIngredients(): LiveData<List<WholeCocktail>> = mObservableIngredients

    fun getObservableLiveIngredients(): MediatorLiveData<List<WholeCocktail>> = mObservableLiveIngredients

    fun getIngredientIds(): List<Long> = mIngredientIds

    fun removeIngredient(cocktail: WholeCocktail) {
        cocktail.ingredientId?.let { mIngredientIds.remove(it) }
        setSelectedIds(mIngredientIds)
    }

    fun setSelectedIds(ingredientIds: List<Long>?) {
        if (ingredientIds != null) {
            mIngredientIds = ingredientIds.toMutableList()
            mLiveListIngredients?.let { mObservableLiveIngredients.removeSource(it) }
            val source = repository.getListIngredientsNames(mIngredientIds)
            mLiveListIngredients = source
            mObservableLiveIngredients.addSource(source) { mObservableLiveIngredients.value = it }
        }
    }

    fun updateIngredients(
        newIngredients: List<WholeCocktail>,
        adapterCocktailList: List<WholeCocktail>
    ) {
        var isListChangedFlag = updateListKeys(newIngredients)
        isListChangedFlag = updateNames(newIngredients, isListChangedFlag)
        updateAmountAndUnit(adapterCocktailList)
        if (isListChangedFlag) exposeIngredients()
    }

    private fun updateListKeys(newIngredients: List<WholeCocktail>): Boolean {
        val oldIds = ArrayList(tempIngredients.keys)
        val newIds = ArrayList<Long>()
        for (cocktail in newIngredients) {
            cocktail.ingredientId?.let { newIds.add(it) }
        }
        oldIds.removeAll(newIds)
        if (oldIds.isNotEmpty()) {
            tempIngredients.keys.removeAll(oldIds)
            return true
        }
        return false
    }

    private fun updateAmountAndUnit(adapterCocktailList: List<WholeCocktail>) {
        val list = if (adapterCocktailList.isEmpty()) {
            ArrayList(tempIngredients.values)
        } else {
            adapterCocktailList
        }
        for (cocktail in list) {
            val id = cocktail.ingredientId
            if (id != null && tempIngredients.containsKey(id)) {
                tempIngredients[id]?.unit = cocktail.unit
                tempIngredients[id]?.amount = cocktail.amount
            }
        }
    }

    private fun updateNames(newIngredients: List<WholeCocktail>, isListChangedFlag: Boolean): Boolean {
        var isChanged = isListChangedFlag
        for (cocktail in newIngredients) {
            val id = cocktail.ingredientId ?: continue
            if (tempIngredients.containsKey(id)) {
                if (tempIngredients[id]?.ingredientName != cocktail.ingredientName) {
                    tempIngredients[id]?.ingredientName = cocktail.ingredientName
                    isChanged = true
                }
            } else {
                tempIngredients[id] = cocktail
                isChanged = true
            }
        }
        return isChanged
    }

    fun getTastesList(): MediatorLiveData<ArrayList<Taste>> = mCurrentTastesList

    fun getPhotoUri(): Uri? = mPhotoUri

    fun getIsNewDrink(): Boolean = mIsNewDrink

    fun getIsImageRemoved(): Boolean = mIsImageRemoved

    fun getCurrentImagePath(): MutableLiveData<String?> = mObservableCurrentImagePath

    fun createPhotoFile(): Uri? {
        mPhotoUri = repository.createImageFile()
        return mPhotoUri
    }

    fun handleImage(imageUri: Uri, sizeForScale: Int, deleteSource: Boolean) {
        mIsImageRemoved = false
        removeImageFile(getDrink().value?.image, getCurrentImagePath().value, false)
        repository.saveImageToAlbum(imageUri, sizeForScale, deleteSource, false)
    }

    fun removeCurrentImage() {
        mIsImageRemoved = true
        removeImageFile(getDrink().value?.image, getCurrentImagePath().value, false)
        getCurrentImagePath().value = null
    }

    private fun removeImageFile(dbPath: String?, currentPath: String?, save: Boolean) {
        var deletePath: String? = null
        if (save) {
            if (dbPath != null && !TextUtils.equals(currentPath, dbPath)) {
                deletePath = dbPath
            }
        } else {
            if (currentPath != null && !TextUtils.equals(currentPath, dbPath)) {
                deletePath = currentPath
            }
        }
        if (deletePath != null) repository.deleteImage(deletePath)
    }

    fun saveDrink(
        name: String?,
        description: String?,
        ingredients: List<WholeCocktail>,
        rating: Int,
        glassName: String?,
        carbonated: Boolean,
        alcoholic: Boolean,
        notes: String?
    ) {
        removeImageFile(getDrink().value?.image, getCurrentImagePath().value, true)
        var drink = mObservableDrink.value
        if (drink == null) drink = Drink()
        drink.image = mObservableCurrentImagePath.value
        drink.name = name ?: ""
        drink.description = description ?: ""
        drink.tastes = mCurrentTastesList.value
        drink.rating = rating
        drink.isCarbonated = carbonated
        drink.isAlcoholic = alcoholic
        val glass = Glass()
        glass.glassName = glassName
        drink.glass = glass
        drink.notes = notes ?: ""
        val ingredientsList = WholeCocktailToDrinkIngredientJoinMapper.getInstance().reverseMap(ingredients)
        repository.saveDrink(drink, ingredientsList)
    }

    fun deleteDrink() {
        repository.deleteDrink(mDrinkId)
    }

    fun setTastes(selectedIngredients: List<Int>, tastesArray: Array<String>) {
        mCurrentTastesList.value = ArrayList(TastesHelper.toTastesList(selectedIngredients, tastesArray))
    }

    fun getTastes(tastesArray: Array<String>): List<Int> =
        TastesHelper.getAsIntegerList(mCurrentTastesList.value, tastesArray)

    companion object {
        const val ARG_ID = "extra_id"
    }
}
