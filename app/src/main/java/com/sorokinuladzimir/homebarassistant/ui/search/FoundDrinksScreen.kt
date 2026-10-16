package com.sorokinuladzimir.homebarassistant.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.core.ui.components.EmptyState
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity

@Composable
fun FoundDrinksScreen(
    drinks: List<DrinkEntity>,
    emptyMessage: String,
    onItemClick: (DrinkEntity) -> Unit,
) {
    if (drinks.isEmpty()) {
        EmptyState(message = emptyMessage)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(drinks, key = { it.id ?: it.hashCode() }) { drink ->
                DrinkCard(drink = drink, onClick = { onItemClick(drink) })
            }
        }
    }
}

@Composable
private fun DrinkCard(drink: DrinkEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = drink.name.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val tags = drink.tastes?.joinToString(", ") { it.text.orEmpty() }.orEmpty()
                if (tags.isNotBlank()) {
                    Text(
                        text = tags,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            AsyncImage(
                model = drink.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )
        }
    }
}
