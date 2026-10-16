package com.sorokinuladzimir.homebarassistant.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.EmptyState
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity

@Composable
fun FoundDrinksScreen(
    drinks: List<DrinkEntity>,
    emptyMessage: String,
    onItemClick: (DrinkEntity) -> Unit,
    onLoadMore: () -> Unit,
) {
    if (drinks.isEmpty()) {
        EmptyState(message = emptyMessage)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(drinks) { index, drink ->
                DrinkCard(drink = drink, onClick = { onItemClick(drink) })
                if (index == drinks.lastIndex) {
                    LaunchedEffect(drinks.size) { onLoadMore() }
                }
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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = drink.name.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = drink.tastes?.joinToString(", ") { it.text.orEmpty() }.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_drink_rating),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = drink.rating.toString(), style = MaterialTheme.typography.bodyMedium)
                    if (drink.isCarbonated) {
                        Spacer(Modifier.width(16.dp))
                        Icon(
                            painter = painterResource(R.drawable.carbonated),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            AsyncImage(
                model = Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + drink.id + ".png",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )
        }
    }
}
