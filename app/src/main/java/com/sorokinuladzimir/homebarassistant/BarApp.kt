package com.sorokinuladzimir.homebarassistant

import android.app.Application
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

@HiltAndroidApp
class BarApp : Application() {

    private lateinit var cicerone: Cicerone<Router>

    override fun onCreate() {
        super.onCreate()
        sInstance = this
        context = applicationContext
        sDefSystemLanguage = systemLang()
        cicerone = Cicerone.create()
        mAppExecutors = AppExecutors()
    }

    private fun systemLang(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.language
        }

    fun getNavigatorHolder(): NavigatorHolder = cicerone.navigatorHolder

    fun getRouter(): Router = cicerone.router

    companion object {

        private lateinit var sInstance: BarApp
        private lateinit var context: Context
        private var sDefSystemLanguage: String = "en"
        private lateinit var mAppExecutors: AppExecutors

        @JvmStatic
        fun getInstance(): BarApp = sInstance

        @JvmStatic
        fun getAppContext(): Context = context

        @JvmStatic
        fun getExecutors(): AppExecutors = mAppExecutors

        @JvmStatic
        fun getLang(): String = sDefSystemLanguage
    }
}
