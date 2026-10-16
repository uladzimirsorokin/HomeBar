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
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddIngredientViewModel @Inject constructor(
    application: Application,
    private val repository: BarData,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mIngredientId: Long = savedStateHandle.get<Long>(ARG_ID) ?: -1L
    private val mObservableIngredient = MediatorLiveData<Ingredient?>()
    private val mObservableCurrentImagePath = MediatorLiveData<String?>()
    private var mPhotoUri: Uri? = null
    private var mIsNewIngredient = true
    private var mIsImageRemoved = false

    init {
        mObservableIngredient.value = null
        mObservableCurrentImagePath.value = null
        repository.resetIngredientImagePath()
        mObservableCurrentImagePath.addSource(repository.getObservableIngredientImagePath()) {
            mObservableCurrentImagePath.value = it
        }
        if (mIngredientId != -1L) {
            mIsNewIngredient = false
            mObservableIngredient.addSource(repository.getIngredient(mIngredientId)) {
                mObservableIngredient.value = it
            }
        }
    }

    fun getIngredient(): LiveData<Ingredient?> = mObservableIngredient

    fun getCurrentImagePath(): MutableLiveData<String?> = mObservableCurrentImagePath

    fun getPhotoUri(): Uri? = mPhotoUri

    fun getIsNewIngredient(): Boolean = mIsNewIngredient

    fun getIsImageRemoved(): Boolean = mIsImageRemoved

    fun createPhotoFile(): Uri? {
        mPhotoUri = repository.createImageFile()
        return mPhotoUri
    }

    fun handleImage(imageUri: Uri, sizeForScale: Int, deleteSource: Boolean) {
        mIsImageRemoved = false
        removeImageFile(getIngredient().value?.image, getCurrentImagePath().value, false)
        repository.saveImageToAlbum(imageUri, sizeForScale, deleteSource, true)
    }

    fun saveIngredient(name: String?, description: String?, notes: String?) {
        removeImageFile(getIngredient().value?.image, getCurrentImagePath().value, true)
        var ingredient = mObservableIngredient.value
        if (ingredient == null) ingredient = Ingredient()
        ingredient.image = mObservableCurrentImagePath.value
        ingredient.name = name
        ingredient.description = description
        ingredient.notes = notes
        repository.addIngredient(ingredient)
    }

    fun deleteIngredient(): Int = repository.deleteIngredient(mIngredientId)

    fun removeCurrentImage() {
        mIsImageRemoved = true
        removeImageFile(getIngredient().value?.image, getCurrentImagePath().value, false)
        getCurrentImagePath().value = null
    }

    private fun removeImageFile(dbPath: String?, currentPath: String?, savingIngredient: Boolean) {
        var deletePath: String? = null
        if (savingIngredient) {
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

    companion object {
        const val ARG_ID = "extra_id"
    }
}
