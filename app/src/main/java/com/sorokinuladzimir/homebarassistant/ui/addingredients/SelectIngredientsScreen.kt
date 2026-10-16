package com.sorokinuladzimir.homebarassistant.ui.addingredients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.ui.components.EmptyState
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient

@Composable
fun SelectIngredientsScreen(
    ingredients: List<Ingredient>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedIds: Set<Long>,
    onToggle: (Ingredient) -> Unit,
    emptyMessage: String,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.menu_search)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (ingredients.isEmpty()) {
            EmptyState(message = emptyMessage)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ingredients, key = { it.id ?: it.hashCode().toLong() }) { ingredient ->
                    IngredientSelectRow(
                        ingredient = ingredient,
                        checked = selectedIds.contains(ingredient.id),
                        onClick = { onToggle(ingredient) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun IngredientSelectRow(ingredient: Ingredient, checked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onClick() })
        Text(
            text = ingredient.name.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
