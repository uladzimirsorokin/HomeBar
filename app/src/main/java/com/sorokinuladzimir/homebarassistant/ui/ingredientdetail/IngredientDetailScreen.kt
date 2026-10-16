package com.sorokinuladzimir.homebarassistant.ui.ingredientdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.DetailCard
import com.sorokinuladzimir.homebarassistant.core.ui.components.SimpleListItem
import com.sorokinuladzimir.homebarassistant.core.ui.components.imageModel
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient

@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient?,
    relatedDrinks: List<Drink>,
    onDrinkClick: (Drink) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val image = imageModel(ingredient?.image)
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
        if (!ingredient?.description.isNullOrEmpty()) {
            DetailCard(title = stringResource(R.string.ingredient_description_title)) {
                Text(ingredient.description.orEmpty())
            }
        }
        if (!ingredient?.notes.isNullOrEmpty()) {
            DetailCard(title = stringResource(R.string.tv_singledrink_notes)) {
                Text(ingredient.notes.orEmpty())
            }
        }
        if (relatedDrinks.isNotEmpty()) {
            DetailCard(title = stringResource(R.string.ingredient_related_drinks_title)) {
                Column {
                    relatedDrinks.forEach { drink ->
                        SimpleListItem(
                            image = drink.image,
                            title = drink.name.orEmpty(),
                            onClick = { onDrinkClick(drink) }
                        )
                    }
                }
            }
        }
    }
}
