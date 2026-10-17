package com.sorokinuladzimir.homebarassistant.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A search request that may contain several single-filter [SearchConditions] queries.
 *
 * TheCocktailDB accepts only one filter per request, so a natural-language search is
 * expanded into a few complementary queries whose results are merged (deduplicated by id).
 */
@Serializable
data class SearchRequest(
    @SerialName("queries") val queries: List<SearchConditions> = emptyList()
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun single(conditions: SearchConditions): SearchRequest = SearchRequest(listOf(conditions))

        fun fromJson(value: String?): SearchRequest =
            value?.takeIf { it.isNotBlank() }
                ?.let { runCatching { json.decodeFromString<SearchRequest>(it) }.getOrNull() }
                ?: SearchRequest()
    }

    fun toJson(): String = json.encodeToString(this)
}
