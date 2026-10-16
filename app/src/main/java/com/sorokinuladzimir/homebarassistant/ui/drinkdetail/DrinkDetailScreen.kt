package com.sorokinuladzimir.homebarassistant.ui.drinkdetail

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.DetailCard
import com.sorokinuladzimir.homebarassistant.core.ui.components.imageModel
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail

@Composable
fun DrinkDetailScreen(
    drink: Drink?,
    ingredients: List<WholeCocktail>,
    showNotes: Boolean,
    onIngredientClick: (WholeCocktail) -> Unit,
    onImageLoaded: ((Bitmap?) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val image = imageModel(drink?.image)
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                onSuccess = { state ->
                    onImageLoaded?.invoke((state.result.drawable as? BitmapDrawable)?.bitmap)
                }
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(R.drawable.ic_drink_name, drink?.glass?.glassName.orEmpty())
                InfoRow(R.drawable.taste, drink?.tastes?.joinToString(", ") { it.text.orEmpty() }.orEmpty())
                InfoRow(R.drawable.ic_drink_type, drinkType(drink))
                InfoRow(R.drawable.ic_drink_rating, drink?.rating?.toString().orEmpty())
            }
        }

        if (ingredients.isNotEmpty()) {
            DetailCard(title = stringResource(R.string.et_singledrink_ingredients)) {
                Column {
                    ingredients.forEach { ingredient ->
                        IngredientRow(ingredient) { onIngredientClick(ingredient) }
                    }
                }
            }
        }

        if (!drink?.description.isNullOrEmpty()) {
            DetailCard(title = stringResource(R.string.et_singledrink_preparation)) {
                Text(drink.description.orEmpty())
            }
        }

        if (showNotes && !drink?.notes.isNullOrEmpty()) {
            DetailCard(title = stringResource(R.string.singledrink_title_notes)) {
                Text(drink.notes.orEmpty())
            }
        }
    }
}

@Composable
private fun InfoRow(iconRes: Int, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun IngredientRow(ingredient: WholeCocktail, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageModel(ingredient.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = ingredient.ingredientName.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            val amount = listOfNotNull(ingredient.amount, ingredient.unit)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            if (amount.isNotBlank()) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun drinkType(drink: Drink?): String {
    if (drink == null) return ""
    val base = stringResource(
        if (drink.isAlcoholic) R.string.alcoholic_drink_type else R.string.no_alco_drink_type
    )
    return if (drink.isCarbonated) base + stringResource(R.string.carbo_drink_type) else base
}
