package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sorokinuladzimir.homebarassistant.BarDataRepository
import com.sorokinuladzimir.homebarassistant.ai.AiMenuScanner
import com.sorokinuladzimir.homebarassistant.ai.ScannedCocktail
import com.sorokinuladzimir.homebarassistant.core.util.cropToImageBox
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.net.entity.Glass
import com.sorokinuladzimir.homebarassistant.ui.utils.ImageHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface AiScanUiState {
    data object Idle : AiScanUiState
    data class Picked(val uri: Uri) : AiScanUiState
    data object Loading : AiScanUiState
    data class Scanned(
        val cocktail: ScannedCocktail,
        val fromDatabase: Boolean,
        val preview: Bitmap?
    ) : AiScanUiState
    data class Error(val message: String) : AiScanUiState
    data object Saved : AiScanUiState
}

@HiltViewModel
class AiScanViewModel @Inject constructor(
    application: Application,
    private val scanner: AiMenuScanner,
    private val repository: BarDataRepository,
    private val imageHandler: ImageHandler
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<AiScanUiState>(AiScanUiState.Idle)
    val state: LiveData<AiScanUiState> = _state

    private var pickedUri: Uri? = null
    private var bitmap: Bitmap? = null

    fun onImagePicked(uri: Uri) {
        pickedUri = uri
        bitmap = null
        _state.value = AiScanUiState.Picked(uri)
    }

    fun createPhotoFile(): Uri? = repository.createImageFile()

    fun recognize() {
        val uri = pickedUri ?: return
        if (_state.value is AiScanUiState.Loading) return
        _state.value = AiScanUiState.Loading
        viewModelScope.launch {
            val image = withContext(Dispatchers.IO) {
                runCatching {
                    imageHandler.getBitmapFromUri(getApplication<Application>(), uri, 1280)
                }.getOrNull()
            }
            if (image == null) {
                _state.value = AiScanUiState.Error("Cannot read the image")
                return@launch
            }
            bitmap = image
            when (val result = scanner.scan(image)) {
                is ApiResult.Success -> {
                    var cocktail = result.data
                    var fromDatabase = false
                    if (!cocktail.isGoodRecognition() && !cocktail.name.isNullOrBlank()) {
                        val match = repository.findRemoteDrink(cocktail.name!!)
                        if (match != null) {
                            cocktail = mergeFromDatabase(cocktail, match)
                            fromDatabase = true
                        }
                    }
                    // Prefer the drink picture cropped from the photo over the whole menu.
                    val cropped = image.cropToImageBox(cocktail.imageBox)
                    bitmap = cropped ?: image
                    _state.value = AiScanUiState.Scanned(cocktail, fromDatabase, bitmap)
                }
                is ApiResult.Error -> _state.value = AiScanUiState.Error(result.message)
            }
        }
    }

    fun save() {
        val cocktail = (_state.value as? AiScanUiState.Scanned)?.cocktail ?: return
        val entity = DrinkEntity(
            name = cocktail.name,
            description = cocktail.instructions,
            isAlcoholic = cocktail.alcoholic ?: false,
            servedIn = cocktail.glass?.let { Glass(text = it) },
            ingredients = cocktail.ingredients
        )
        repository.saveRemoteDrink(entity, bitmap)
        _state.value = AiScanUiState.Saved
    }

    fun reset() {
        _state.value = AiScanUiState.Idle
    }

    private fun mergeFromDatabase(scanned: ScannedCocktail, match: DrinkEntity): ScannedCocktail =
        scanned.copy(
            name = match.name ?: scanned.name,
            ingredients = match.ingredients?.takeIf { it.isNotEmpty() } ?: scanned.ingredients,
            instructions = match.description ?: scanned.instructions,
            glass = match.servedIn?.text ?: scanned.glass,
            alcoholic = match.isAlcoholic
        )
}
