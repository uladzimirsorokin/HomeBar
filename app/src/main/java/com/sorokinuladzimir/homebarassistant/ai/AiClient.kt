package com.sorokinuladzimir.homebarassistant.ai

import com.sorokinuladzimir.homebarassistant.BuildConfig
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.ai.AiApi
import com.sorokinuladzimir.homebarassistant.net.ai.AiChatMessage
import com.sorokinuladzimir.homebarassistant.net.ai.AiChatRequest
import com.sorokinuladzimir.homebarassistant.net.ai.AiContentPart
import com.sorokinuladzimir.homebarassistant.net.ai.AiImageUrl
import com.sorokinuladzimir.homebarassistant.net.ai.AiResponseFormat
import com.sorokinuladzimir.homebarassistant.net.ai.AiVisionMessage
import com.sorokinuladzimir.homebarassistant.net.ai.AiVisionRequest
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over the OpenAI-compatible chat completions endpoint.
 */
@Singleton
class AiClient @Inject constructor(
    private val api: AiApi
) {

    suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        jsonMode: Boolean = true
    ): ApiResult<String> = try {
        val response = api.chatCompletions(
            AiChatRequest(
                model = BuildConfig.AI_MODEL,
                messages = listOf(
                    AiChatMessage(role = "system", content = systemPrompt),
                    AiChatMessage(role = "user", content = userPrompt)
                ),
                responseFormat = if (jsonMode) AiResponseFormat("json_object") else null
            )
        )
        val content = response.choices?.firstOrNull()?.message?.content
        if (content.isNullOrBlank()) {
            ApiResult.Error("AI returned an empty response")
        } else {
            ApiResult.Success(content)
        }
    } catch (e: IOException) {
        ApiResult.Error("AI server is unavailable", e)
    } catch (e: HttpException) {
        ApiResult.Error("AI server error (${e.code()})", e)
    } catch (e: Exception) {
        ApiResult.Error("AI error: ${e.message}", e)
    }

    /**
     * Multimodal call: sends the prompt plus a base64 image (data URL) to the vision model.
     */
    suspend fun completeVision(
        systemPrompt: String,
        userPrompt: String,
        imageDataUrl: String,
        jsonMode: Boolean = true
    ): ApiResult<String> = try {
        val response = api.chatCompletionsVision(
            AiVisionRequest(
                model = BuildConfig.AI_VISION_MODEL,
                messages = listOf(
                    AiVisionMessage(
                        role = "system",
                        content = listOf(AiContentPart(type = "text", text = systemPrompt))
                    ),
                    AiVisionMessage(
                        role = "user",
                        content = listOf(
                            AiContentPart(type = "text", text = userPrompt),
                            AiContentPart(type = "image_url", imageUrl = AiImageUrl(imageDataUrl))
                        )
                    )
                ),
                responseFormat = if (jsonMode) AiResponseFormat("json_object") else null
            )
        )
        val content = response.choices?.firstOrNull()?.message?.content
        if (content.isNullOrBlank()) {
            ApiResult.Error("AI returned an empty response")
        } else {
            ApiResult.Success(content)
        }
    } catch (e: IOException) {
        ApiResult.Error("AI server is unavailable", e)
    } catch (e: HttpException) {
        ApiResult.Error("AI server error (${e.code()})", e)
    } catch (e: Exception) {
        ApiResult.Error("AI error: ${e.message}", e)
    }
}
