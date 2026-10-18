package com.sorokinuladzimir.homebarassistant.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Search criteria selected on the filter screen.
 *
 * TheCocktailDB currently supports only [name], [ingredient], [glass], [category] and
 * [alcoholic]. The remaining fields ([taste], [carbonated], [color], [skill]) are **ignored**
 * for now but are kept in the model so they can be applied later, e.g. via AI post-analysis
 * of the returned drinks.
 */
@Serializable
data class SearchConditions(
    @SerialName("name") val name: String? = null,
    @SerialName("ingredient") val ingredient: String? = null,
    @SerialName("glass") val glass: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("alcoholic") val alcoholic: Boolean? = null,
    // Not supported by TheCocktailDB yet; reserved for future processing.
    @SerialName("taste") val taste: String? = null,
    @SerialName("carbonated") val carbonated: Boolean? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("skill") val skill: Int? = null,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(value: String?): SearchConditions =
            value?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<SearchConditions>(it) }.getOrNull() }
                ?: SearchConditions()
    }

    fun toJson(): String = json.encodeToString(this)
}
