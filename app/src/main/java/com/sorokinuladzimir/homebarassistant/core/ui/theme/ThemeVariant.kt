package com.sorokinuladzimir.homebarassistant.core.ui.theme

import android.content.Context
import com.sorokinuladzimir.homebarassistant.Constants

/**
 * Возвращает текущую тему приложения из SharedPreferences (то же хранилище, что
 * использует [com.sorokinuladzimir.homebarassistant.ui.utils.ThemeUtils]).
 */
fun Context.homeBarThemeVariant(): ThemeVariant {
    val prefs = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    return ThemeVariant.fromId(prefs.getInt(Constants.Extra.APP_THEME, 0))
}
