package com.sorokinuladzimir.homebarassistant.ai

import com.sorokinuladzimir.homebarassistant.net.ApiResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Translates recipe instructions to the app language with the local LLM.
 */
@Singleton
class AiTranslator @Inject constructor(
    private val aiClient: AiClient
) {

    fun appLanguageCode(): String = if (Locale.getDefault().language == "ru") "ru" else "en"

    /**
     * Cheap heuristic to avoid an LLM call when the text is already in the target language.
     */
    fun needsTranslation(text: String?, targetLanguageCode: String): Boolean {
        if (text.isNullOrBlank()) return false
        val hasCyrillic = text.any { it in '\u0400'..'\u04FF' }
        return if (targetLanguageCode == "ru") !hasCyrillic else hasCyrillic
    }

    suspend fun translateInstructions(
        text: String?,
        targetLanguageCode: String
    ): ApiResult<String?> {
        if (text.isNullOrBlank()) return ApiResult.Success(null)
        val language = if (targetLanguageCode == "ru") "Russian" else "English"
        val userPrompt = "Target language: $language\n\nText:\n$text"
        return when (val result = aiClient.complete(AiPrompts.translationSystem(language), userPrompt)) {
            is ApiResult.Success -> {
                val translation = runCatching {
                    AiJson.decode(result.data, AiTranslation.serializer())
                }.getOrNull()
                ApiResult.Success(translation?.translation?.takeIf { it.isNotBlank() })
            }
            is ApiResult.Error -> result
        }
    }
}

@Serializable
internal data class AiTranslation(
    @SerialName("translation") val translation: String? = null
)
