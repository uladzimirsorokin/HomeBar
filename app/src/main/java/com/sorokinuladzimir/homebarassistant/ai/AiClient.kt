package com.sorokinuladzimir.homebarassistant.ai

import com.sorokinuladzimir.homebarassistant.BuildConfig
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.ai.AiApi
import com.sorokinuladzimir.homebarassistant.net.ai.AiChatMessage
import com.sorokinuladzimir.homebarassistant.net.ai.AiChatRequest
import com.sorokinuladzimir.homebarassistant.net.ai.AiResponseFormat
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
}
