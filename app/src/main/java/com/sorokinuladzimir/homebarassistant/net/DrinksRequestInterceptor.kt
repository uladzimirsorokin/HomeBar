package com.sorokinuladzimir.homebarassistant.net

import com.sorokinuladzimir.homebarassistant.Constants
import okhttp3.Interceptor
import okhttp3.Response

class DrinksRequestInterceptor(private val lang: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter(Constants.Extra.API_KEY, Constants.Keys.ABSOLUT_API_KEY)
            .addQueryParameter(Constants.Extra.LANG, lang)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
