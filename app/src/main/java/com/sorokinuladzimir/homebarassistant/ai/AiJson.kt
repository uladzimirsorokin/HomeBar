package com.sorokinuladzimir.homebarassistant.ai

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

/**
 * Helpers for talking to the local LLM, which is asked to return JSON.
 */
object AiJson {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Strips markdown code fences / surrounding prose some models add around JSON.
     */
    fun extract(raw: String): String {
        val trimmed = raw.trim()
        val withoutFence = if (trimmed.startsWith("```")) {
            trimmed.removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .substringBeforeLast("```")
                .trim()
        } else {
            trimmed
        }
        val start = withoutFence.indexOf('{')
        val end = withoutFence.lastIndexOf('}')
        return if (start >= 0 && end > start) withoutFence.substring(start, end + 1) else withoutFence
    }

    fun <T> decode(raw: String, deserializer: DeserializationStrategy<T>): T =
        json.decodeFromString(deserializer, extract(raw))
}
