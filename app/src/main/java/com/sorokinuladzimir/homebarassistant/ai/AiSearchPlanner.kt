package com.sorokinuladzimir.homebarassistant.ai

import android.content.Context
import android.util.Log
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import com.sorokinuladzimir.homebarassistant.net.SearchRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a natural-language request plus the current UI filters into a small set of
 * TheCocktailDB queries using the local LLM.
 */
@Singleton
class AiSearchPlanner @Inject constructor(
    private val aiClient: AiClient,
    @ApplicationContext private val context: Context
) {

    suspend fun plan(
        description: String,
        current: SearchConditions
    ): ApiResult<SearchRequest> {
        val userPrompt = buildUserPrompt(description, current)
        Log.d(TAG, "AI search request:\n$userPrompt")
        return when (val result = aiClient.complete(AiPrompts.SearchPlanner, userPrompt)) {
            is ApiResult.Success -> {
                Log.d(TAG, "AI search plan: ${result.data}")
                val plan = runCatching {
                    AiJson.decode(result.data, AiSearchPlan.serializer())
                }.getOrNull()
                val request = plan?.toRequest()
                    ?.takeIf { it.queries.isNotEmpty() }
                    ?: SearchRequest.single(current)
                Log.d(TAG, "AI search queries: ${request.queries}")
                ApiResult.Success(request)
            }
            is ApiResult.Error -> {
                Log.w(TAG, "AI search failed: ${result.message}")
                result
            }
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

    private companion object {
        private const val TAG = "HomeBar-AI"
    }
}

@Serializable
internal data class AiSearchPlan(
    @SerialName("queries") val queries: List<AiSearchQuery>? = null,
    // Legacy single-query shape support.
    @SerialName("name") val name: String? = null,
    @SerialName("ingredient") val ingredient: String? = null,
    @SerialName("glass") val glass: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("alcoholic") val alcoholic: Boolean? = null,
    @SerialName("reason") val reason: String? = null
) {

    fun toRequest(): SearchRequest {
        val fromQueries = queries.orEmpty().flatMap { it.toConditions() }
        val legacy = AiSearchQuery(name, ingredient, glass, category, alcoholic).toConditions()
        val merged = (fromQueries + legacy).distinct().take(MAX_QUERIES)
        return SearchRequest(merged)
    }
}

private const val MAX_QUERIES = 3

@Serializable
internal data class AiSearchQuery(
    @SerialName("name") val name: String? = null,
    @SerialName("ingredient") val ingredient: String? = null,
    @SerialName("glass") val glass: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("alcoholic") val alcoholic: Boolean? = null,
    @SerialName("reason") val reason: String? = null
) {

    /**
     * One single-filter condition per non-null field. The model is asked to set exactly one
     * field, but if it returns several they are expanded into separate queries defensively.
     */
    fun toConditions(): List<SearchConditions> = buildList {
        name?.takeIf { it.isNotBlank() }?.let { add(SearchConditions(name = it.trim())) }
        ingredient?.takeIf { it.isNotBlank() }?.let { add(SearchConditions(ingredient = it.trim())) }
        glass?.takeIf { it.isNotBlank() }?.let {
            add(SearchConditions(glass = it.trim().replace(" ", "_")))
        }
        category?.takeIf { it.isNotBlank() }?.let {
            add(SearchConditions(category = it.trim().replace(" ", "_")))
        }
        alcoholic?.let { add(SearchConditions(alcoholic = it)) }
    }
}
