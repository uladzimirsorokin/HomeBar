package com.sorokinuladzimir.homebarassistant.ui.adddrink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.imageModel
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import kotlin.math.roundToInt

@Composable
fun AddDrinkScreen(
    imagePath: String?,
    onImageClick: () -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    tastes: String,
    onTastesClick: () -> Unit,
    ingredients: List<WholeCocktail>,
    unitOptions: List<String>,
    onAmountChange: (Int, String) -> Unit,
    onUnitChange: (Int, String) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onAddIngredients: () -> Unit,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    carbonated: Boolean,
    onCarbonatedChange: (Boolean) -> Unit,
    alcoholic: Boolean,
    onAlcoholicChange: (Boolean) -> Unit,
    glassNames: List<String>,
    glassIndex: Int,
    onGlassChange: (Int) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = imageModel(imagePath) ?: R.drawable.camera_placeholder,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onImageClick)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.cocktail_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.et_singledrink_preparation)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(onClick = onTastesClick, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = tastes.ifBlank { stringResource(R.string.bt_add_tastes) },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(stringResource(R.string.et_singledrink_ingredients), style = MaterialTheme.typography.titleMedium)
        ingredients.forEachIndexed { index, ingredient ->
            IngredientEditRow(
                ingredient = ingredient,
                unitOptions = unitOptions,
                onAmountChange = { onAmountChange(index, it) },
                onUnitChange = { onUnitChange(index, it) },
                onRemove = { onRemoveIngredient(index) }
            )
            HorizontalDivider()
        }
        OutlinedButton(onClick = onAddIngredients, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bt_add_ingredients))
        }

        Text(
            text = "${stringResource(R.string.cocktail_rating)} $rating",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = rating.toFloat(),
            onValueChange = { onRatingChange(it.roundToInt()) },
            valueRange = 0f..100f
        )

        SwitchRow(stringResource(R.string.carbonated), carbonated, onCarbonatedChange)
        SwitchRow(stringResource(R.string.alcoholic), alcoholic, onAlcoholicChange)

        DropdownField(
            label = stringResource(R.string.glass_type),
            options = glassNames,
            selectedIndex = glassIndex,
            onSelected = onGlassChange
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.tv_singledrink_notes)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun IngredientEditRow(
    ingredient: WholeCocktail,
    unitOptions: List<String>,
    onAmountChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ingredient.ingredientName.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = ingredient.amount.orEmpty(),
            onValueChange = onAmountChange,
            singleLine = true,
            modifier = Modifier.width(80.dp)
        )
        Spacer(Modifier.width(8.dp))
        UnitDropdown(
            unit = ingredient.unit.orEmpty(),
            options = unitOptions,
            onSelected = onUnitChange
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, contentDescription = null)
        }
    }
}

@Composable
private fun UnitDropdown(unit: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(unit.ifBlank { "-" })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label ${options.getOrElse(selectedIndex) { "" }}")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
