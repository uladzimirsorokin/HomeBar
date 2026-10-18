package com.sorokinuladzimir.homebarassistant.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sorokinuladzimir.homebarassistant.R

private val items: List<Pair<Int, @Composable () -> Unit>> = listOf(
    R.string.bottom_bar_search to { Icon(Icons.Filled.Search, contentDescription = null) },
    R.string.bottom_bar_drinks to { Icon(painterResource(R.drawable.cocktail), contentDescription = null) },
    R.string.bottom_bar_ingredients to { Icon(painterResource(R.drawable.bottles), contentDescription = null) },
)

@Composable
fun HomeBarBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        items.forEachIndexed { index, (labelRes, iconContent) ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = iconContent,
                label = { Text(stringResource(labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            )
        }
    }
}
