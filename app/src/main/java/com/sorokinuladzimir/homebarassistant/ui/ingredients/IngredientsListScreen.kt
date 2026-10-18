package com.sorokinuladzimir.homebarassistant.ui.ingredients

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sorokinuladzimir.homebarassistant.core.ui.components.EmptyState
import com.sorokinuladzimir.homebarassistant.core.ui.components.SimpleListItem
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.net.CocktailDbImages

@Composable
fun IngredientsListScreen(
    ingredients: List<Ingredient>,
    emptyMessage: String,
    onItemClick: (Ingredient) -> Unit,
) {
    if (ingredients.isEmpty()) {
        EmptyState(message = emptyMessage)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(ingredients, key = { it.id ?: it.hashCode().toLong() }) { ingredient ->
                SimpleListItem(
                    image = ingredient.image ?: CocktailDbImages.ingredient(ingredient.name),
                    title = ingredient.name.orEmpty(),
                    onClick = { onItemClick(ingredient) }
                )
                HorizontalDivider()
            }
        }
    }
}
