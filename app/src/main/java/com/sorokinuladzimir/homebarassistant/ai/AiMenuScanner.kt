package com.sorokinuladzimir.homebarassistant.ai

import android.graphics.Bitmap
import android.util.Log
import com.sorokinuladzimir.homebarassistant.core.util.toBase64Jpeg
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.entity.IngredientDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A cocktail recognised from a menu photo.
 *
 * [imageBox] is a drink-picture bounding box in a 0-1000 scale, or null when not detected.
 */
data class ScannedCocktail(
    val name: String?,
    val ingredients: List<IngredientDto>,
    val instructions: String?,
    val glass: String?,
    val alcoholic: Boolean?,
    val confidence: Double,
    val imageBox: List<Int>? = null
) {
    fun isGoodRecognition(): Boolean =
        !name.isNullOrBlank() && ingredients.isNotEmpty() && confidence >= GOOD_CONFIDENCE

    companion object {
        const val GOOD_CONFIDENCE = 0.5
    }
}

/**
 * Reads a menu/photo with the vision model and extracts a cocktail recipe.
 */
@Singleton
class AiMenuScanner @Inject constructor(
    private val aiClient: AiClient
) {

    suspend fun scan(bitmap: Bitmap): ApiResult<ScannedCocktail> {
        val dataUrl = "data:image/jpeg;base64," + bitmap.toBase64Jpeg()
        return when (
            val result = aiClient.completeVision(
                systemPrompt = AiPrompts.MenuScanner,
                userPrompt = "Extract the cocktail from this menu photo.",
                imageDataUrl = dataUrl
            )
        ) {
            is ApiResult.Success -> {
                Log.d(TAG, "menu scan result: ${result.data}")
                val scanned = runCatching {
                    AiJson.decode(result.data, AiScannedDrink.serializer())
                }.getOrNull()
                if (scanned == null) {
                    ApiResult.Error("AI returned an unreadable response")
                } else {
                    ApiResult.Success(scanned.toCocktail())
                }
            }
            is ApiResult.Error -> result
        }
    }

    private companion object {
        private const val TAG = "HomeBar-AI"
    }
}

@Serializable
internal data class AiScannedDrink(
    @SerialName("confidence") val confidence: Double = 0.0,
    @SerialName("name") val name: String? = null,
    @SerialName("alcoholic") val alcoholic: Boolean? = null,
    @SerialName("glass") val glass: String? = null,
    @SerialName("instructions") val instructions: String? = null,
    @SerialName("ingredients") val ingredients: List<AiScannedIngredient>? = null,
    @SerialName("imageBox") val imageBox: List<Int>? = null
) {

    fun toCocktail(): ScannedCocktail = ScannedCocktail(
        name = name.clean(),
        ingredients = ingredients.orEmpty().mapNotNull { ingredient ->
            ingredient.name.clean()?.let {
                IngredientDto(name = it, measure = ingredient.measure.clean())
            }
        },
        instructions = instructions.clean(),
        glass = glass.clean(),
        alcoholic = alcoholic,
        confidence = confidence.coerceIn(0.0, 1.0),
        imageBox = imageBox?.takeIf { it.size == 4 }
    )
}

@Serializable
internal data class AiScannedIngredient(
    @SerialName("name") val name: String? = null,
    @SerialName("measure") val measure: String? = null
)

/**
 * Vision models sometimes emit the literal string "null"/"none" instead of JSON null.
 */
private fun String?.clean(): String? =
    this?.trim()?.takeIf {
        it.isNotEmpty() &&
            !it.equals("null", ignoreCase = true) &&
            !it.equals("none", ignoreCase = true) &&
            it != "-"
    }
