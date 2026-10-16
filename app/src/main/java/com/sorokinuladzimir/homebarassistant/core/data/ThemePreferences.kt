package com.sorokinuladzimir.homebarassistant.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val themeKey = intPreferencesKey("app_theme")

    val themeId: Flow<Int> = context.themeDataStore.data.map { it[themeKey] ?: DEFAULT_THEME_ID }

    suspend fun setThemeId(id: Int) {
        context.themeDataStore.edit { it[themeKey] = id }
    }

    companion object {
        const val DEFAULT_THEME_ID = 0
    }
}
