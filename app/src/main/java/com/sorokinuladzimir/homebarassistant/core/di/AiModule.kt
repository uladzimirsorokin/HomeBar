package com.sorokinuladzimir.homebarassistant.core.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sorokinuladzimir.homebarassistant.BuildConfig
import com.sorokinuladzimir.homebarassistant.core.util.debugLoggingInterceptor
import com.sorokinuladzimir.homebarassistant.net.ai.AiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideAiApi(): AiApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = if (BuildConfig.AI_API_KEY.isNotBlank()) {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(debugLoggingInterceptor("HomeBar-AI"))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl())
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AiApi::class.java)
    }

    private fun normalizedBaseUrl(): String {
        val url = BuildConfig.AI_BASE_URL
        return if (url.endsWith("/")) url else "$url/"
    }
}
