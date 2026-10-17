package com.sorokinuladzimir.homebarassistant.core.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sorokinuladzimir.homebarassistant.BuildConfig
import com.sorokinuladzimir.homebarassistant.core.util.debugLoggingInterceptor
import com.sorokinuladzimir.homebarassistant.net.CocktailDbApi
import com.sorokinuladzimir.homebarassistant.net.ConnectivityInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideCocktailDbApi(@ApplicationContext context: Context): CocktailDbApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(ConnectivityInterceptor(context))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(debugLoggingInterceptor("HomeBar-CocktailDB"))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("${BuildConfig.COCKTAILDB_ROOT}${BuildConfig.COCKTAILDB_API_KEY}/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CocktailDbApi::class.java)
    }
}
