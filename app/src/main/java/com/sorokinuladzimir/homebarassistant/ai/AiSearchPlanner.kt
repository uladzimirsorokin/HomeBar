package com.sorokinuladzimir.homebarassistant.ai

import android.content.Context
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a natural-language request plus the current UI filters into a single
 * TheCocktailDB query using the local LLM.
 */
@Singleton
class AiSearchPlanner @Inject constructor(
    private val aiClient: AiClient,
    @ApplicationContext private val context: Context
) {

    suspend fun plan(
        description: String,
        current: SearchConditions
    ): ApiResult<SearchConditions> {
        val userPrompt = buildUserPrompt(description, current)
        return when (val result = aiClient.complete(AiPrompts.SearchPlanner, userPrompt)) {
            is ApiResult.Success -> {
                val plan = runCatching {
                    AiJson.decode(result.data, AiSearchPlan.serializer())
                }.getOrNull()
                ApiResult.Success(plan?.toConditions() ?: current)
            }
            is ApiResult.Error -> result
        }
    }

    private fun buildUserPrompt(description: String, current: SearchConditions): String {
        val glasses = context.resources.getStringArray(R.array.glass_id)
            .drop(1)
            .joinToString(", ")
        val spirits = context.resources.getStringArray(R.array.ingridient_type)
            .drop(1)
            .joinToString(", ")
        val language = if (Locale.getDefault().language == "ru") "Russian" else "English"
        val request = description.trim().ifBlank { current.name.orEmpty() }

        return buildString {
            appendLine("User request: \"$request\"")
            appendLine("UI filters:")
            appendLine("- name: ${current.name ?: "-"}")
            appendLine("- glass: ${current.glass ?: "-"}")
            appendLine("- ingredient: ${current.ingredient ?: "-"}")
            appendLine("- taste: ${current.taste ?: "-"}")
            appendLine("- colour: ${current.color ?: "-"}")
            appendLine(
                "- alcoholic: ${
                    when (current.alcoholic) {
                        true -> "true (default)"
                        false -> "false"
                        null -> "-"
                    }
                }"
            )
            appendLine("- carbonated: ${current.carbonated?.toString() ?: "-"}")
            appendLine("- skill: ${current.skill?.toString() ?: "-"}")
            appendLine("Known glasses: $glasses")
            appendLine("Known spirit ingredients: $spirits")
            appendLine("Target app language: $language")
        }
    }
}

@Serializable
internal data class AiSearchPlan(
    @SerialName("name") val name: String? = null,
    @SerialName("ingredient") val ingredient: String? = null,
    @SerialName("glass") val glass: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("alcoholic") val alcoholic: Boolean? = null,
    @SerialName("reason") val reason: String? = null
) {

    fun toConditions(): SearchConditions? = when {
        !name.isNullOrBlank() -> SearchConditions(name = name.trim())
        !ingredient.isNullOrBlank() -> SearchConditions(ingredient = ingredient.trim())
        !glass.isNullOrBlank() -> SearchConditions(glass = glass.trim().replace(" ", "_"))
        !category.isNullOrBlank() -> SearchConditions(category = category.trim().replace(" ", "_"))
        alcoholic != null -> SearchConditions(alcoholic = alcoholic)
        else -> null
    }
}
