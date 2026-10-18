package com.sorokinuladzimir.homebarassistant.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Цветовые схемы приложения. Соответствуют трём темам приложения:
 * INDIGO = 0, PURPLE = 1, BLACK = 2.
 */
enum class ThemeVariant(val id: Int) {
    INDIGO(0),
    PURPLE(1),
    BLACK(2);

    companion object {
        fun fromId(id: Int): ThemeVariant = entries.firstOrNull { it.id == id } ?: INDIGO
    }
}

private val IndigoColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = White,
    primaryContainer = IndigoDark,
    onPrimaryContainer = White,
    secondary = IndigoAccent,
    onSecondary = White,
)

private val PurpleColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = White,
    secondary = PurpleAccent,
    onSecondary = White,
)

private val BlackColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = BlackDark,
    onPrimaryContainer = White,
    secondary = BlackAccent,
    onSecondary = BlackText,
)

@Composable
fun HomeBarTheme(
    variant: ThemeVariant = ThemeVariant.INDIGO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when (variant) {
        ThemeVariant.INDIGO -> IndigoColorScheme
        ThemeVariant.PURPLE -> PurpleColorScheme
        ThemeVariant.BLACK -> BlackColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
