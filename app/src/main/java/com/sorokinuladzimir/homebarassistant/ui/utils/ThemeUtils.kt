package com.sorokinuladzimir.homebarassistant.ui.utils

import android.app.Activity
import android.content.Context
import com.sorokinuladzimir.homebarassistant.Constants

object ThemeUtils {

    private const val PREFS_SUFFIX = "_preferences"

    fun currentThemeId(context: Context): Int =
        prefs(context).getInt(Constants.Extra.APP_THEME, 0)

    fun changeToTheme(activity: Activity, theme: Int) {
        prefs(activity).edit().putInt(Constants.Extra.APP_THEME, theme).apply()
        activity.recreate()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("${context.packageName}$PREFS_SUFFIX", Context.MODE_PRIVATE)
}
