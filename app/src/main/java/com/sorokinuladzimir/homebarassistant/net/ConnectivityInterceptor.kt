package com.sorokinuladzimir.homebarassistant.net

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtil.isOnline(context)) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request().newBuilder().build())
    }
}
