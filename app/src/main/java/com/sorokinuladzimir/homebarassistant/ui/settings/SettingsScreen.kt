package com.sorokinuladzimir.homebarassistant.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.theme.BlackDark
import com.sorokinuladzimir.homebarassistant.core.ui.theme.IndigoDark
import com.sorokinuladzimir.homebarassistant.core.ui.theme.PurpleDark

private data class ThemeOption(val id: Int, val color: Color, val name: String)

@Composable
fun SettingsScreen(
    currentThemeId: Int,
    onThemeSelected: (Int) -> Unit,
) {
    val themes = listOf(
        ThemeOption(0, IndigoDark, stringResource(R.string.main_theme_name)),
        ThemeOption(1, PurpleDark, stringResource(R.string.second_theme_name)),
        ThemeOption(2, BlackDark, stringResource(R.string.third_theme_name)),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.app_theme_color_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        themes.forEach { theme ->
            ThemeRow(
                theme = theme,
                selected = theme.id == currentThemeId,
                onClick = { onThemeSelected(theme.id) }
            )
        }
    }
}

@Composable
private fun ThemeRow(theme: ThemeOption, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(40.dp)
                .background(theme.color, CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Text(text = theme.name, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
