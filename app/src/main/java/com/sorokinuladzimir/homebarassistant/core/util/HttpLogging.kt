package com.sorokinuladzimir.homebarassistant.core.util

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

/**
 * HTTP logging interceptor for debug builds.
 *
 * Log to Logcat with the given [tag]. Inspect outgoing requests with:
 *   adb logcat -s HomeBar-CocktailDB:V HomeBar-AI:V
 */
fun debugLoggingInterceptor(tag: String): HttpLoggingInterceptor =
    HttpLoggingInterceptor { message -> Log.d(tag, message) }.apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }
