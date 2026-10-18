package com.sorokinuladzimir.homebarassistant.ui.ingredientdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.CollapsingDetailScaffold
import com.sorokinuladzimir.homebarassistant.core.ui.components.DetailCard
import com.sorokinuladzimir.homebarassistant.core.ui.components.SimpleListItem
import com.sorokinuladzimir.homebarassistant.core.ui.components.imageModel
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.net.CocktailDbImages

@Composable
fun IngredientDetailScreen(
    ingredient: Ingredient?,
    relatedDrinks: List<Drink>,
    onBack: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
) {
    CollapsingDetailScaffold(
        title = ingredient?.name.orEmpty(),
        image = imageModel(ingredient?.image ?: CocktailDbImages.ingredient(ingredient?.name)),
        onBack = onBack,
        floatingActionButton = floatingActionButton
    ) {
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
