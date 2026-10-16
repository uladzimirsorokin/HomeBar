package com.sorokinuladzimir.homebarassistant.core.di

import android.content.Context
import android.os.Build
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.net.AbsolutDrinksApi
import com.sorokinuladzimir.homebarassistant.net.ConnectivityInterceptor
import com.sorokinuladzimir.homebarassistant.net.DrinksRequestInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAbsolutDrinksApi(@ApplicationContext context: Context): AbsolutDrinksApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(ConnectivityInterceptor(context))
            .addInterceptor(DrinksRequestInterceptor(systemLanguage(context)))
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.Uri.ABSOLUT_DRINKS_ROOT)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AbsolutDrinksApi::class.java)
    }

    private fun systemLanguage(context: Context): String {
        val language = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
        return if (language == "ru") language else "en"
    }
}
