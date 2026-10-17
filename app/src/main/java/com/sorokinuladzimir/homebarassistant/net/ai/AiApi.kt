package com.sorokinuladzimir.homebarassistant.net.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * OpenAI-compatible chat completions API (Ollama / LM Studio / llama.cpp server).
 * The endpoint is configurable via `AI_BASE_URL` in `local.properties`.
 */
interface AiApi {

    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: AiChatRequest): AiChatResponse

    @POST("chat/completions")
    suspend fun chatCompletionsVision(@Body request: AiVisionRequest): AiChatResponse
}

@Serializable
data class AiVisionRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<AiVisionMessage>,
    @SerialName("temperature") val temperature: Double = 0.1,
    @SerialName("stream") val stream: Boolean = false,
    @SerialName("response_format") val responseFormat: AiResponseFormat? = null
)

@Serializable
data class AiVisionMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: List<AiContentPart>
)

@Serializable
data class AiContentPart(
    @SerialName("type") val type: String,
    @SerialName("text") val text: String? = null,
    @SerialName("image_url") val imageUrl: AiImageUrl? = null
)

@Serializable
data class AiImageUrl(
    @SerialName("url") val url: String
)

@Serializable
data class AiChatRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<AiChatMessage>,
    @SerialName("temperature") val temperature: Double = 0.2,
    @SerialName("stream") val stream: Boolean = false,
    @SerialName("response_format") val responseFormat: AiResponseFormat? = null
)

@Serializable
data class AiChatMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class AiResponseFormat(
    @SerialName("type") val type: String
)

@Serializable
data class AiChatResponse(
    @SerialName("choices") val choices: List<AiChoice>? = null
)

@Serializable
data class AiChoice(
    @SerialName("message") val message: AiChatMessage? = null
)
