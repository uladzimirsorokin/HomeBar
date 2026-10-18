package com.sorokinuladzimir.homebarassistant.ui.drinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sorokinuladzimir.homebarassistant.core.ui.components.EmptyState
import com.sorokinuladzimir.homebarassistant.core.ui.components.SimpleListItem
import com.sorokinuladzimir.homebarassistant.db.entity.Drink

@Composable
fun DrinksListScreen(
    drinks: List<Drink>,
    emptyMessage: String,
    onItemClick: (Drink) -> Unit,
) {
    if (drinks.isEmpty()) {
        EmptyState(message = emptyMessage)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(drinks, key = { it.id ?: it.hashCode().toLong() }) { drink ->
                SimpleListItem(
                    image = drink.image,
                    title = drink.name.orEmpty(),
                    onClick = { onItemClick(drink) }
                )
                HorizontalDivider()
            }
        }
    }
}
