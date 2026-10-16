package com.sorokinuladzimir.homebarassistant.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.ui.utils.DrinksPathManager
import kotlin.math.roundToInt

@Composable
fun SearchDrinksScreen(onSearch: (String) -> Unit) {
    val context = LocalContext.current
    val glassNames = context.resources.getStringArray(R.array.glass_name).toList()
    val tasteNames = context.resources.getStringArray(R.array.taste_name).toList()
    val ingredientNames = context.resources.getStringArray(R.array.ingridient_name).toList()
    val colors = context.resources.getIntArray(R.array.colors)

    var glassId by rememberSaveable { mutableIntStateOf(0) }
    var tasteId by rememberSaveable { mutableIntStateOf(0) }
    var ingredientId by rememberSaveable { mutableIntStateOf(0) }
    var carbonated by rememberSaveable { mutableStateOf(false) }
    var alcoholic by rememberSaveable { mutableStateOf(true) }
    var color by rememberSaveable { mutableIntStateOf(-2) }
    var skill by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DropdownField(stringResource(R.string.glass_type), glassNames, glassId) { glassId = it }
        DropdownField(stringResource(R.string.taste_type), tasteNames, tasteId) { tasteId = it }
        DropdownField(stringResource(R.string.ingridient_type), ingredientNames, ingredientId) {
            ingredientId = it
        }

        SwitchRow(stringResource(R.string.carbonated), carbonated) { carbonated = it }
        SwitchRow(stringResource(R.string.alcoholic), alcoholic) { alcoholic = it }

        Text(stringResource(R.string.cocktail_color), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.drop(1).forEach { value ->
                val selected = value == color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(value), CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { color = value }
                )
            }
        }

        Text(
            text = "${stringResource(R.string.skill_level_string)}: ${skillLabel(skill)}",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = skill.toFloat(),
            onValueChange = { skill = it.roundToInt() },
            valueRange = 0f..2f,
            steps = 1
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                onSearch(
                    buildPath(
                        context = context,
                        glassId = glassId,
                        tasteId = tasteId,
                        ingredientId = ingredientId,
                        carbonated = carbonated,
                        alcoholic = alcoholic,
                        color = color,
                        skill = skill
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.menu_search))
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

@Composable
private fun skillLabel(skill: Int): String = when (skill) {
    1 -> stringResource(R.string.skill_middle)
    2 -> stringResource(R.string.skill_master)
    else -> stringResource(R.string.skill_novice)
}

private fun buildPath(
    context: android.content.Context,
    glassId: Int,
    tasteId: Int,
    ingredientId: Int,
    carbonated: Boolean,
    alcoholic: Boolean,
    color: Int,
    skill: Int,
): String {
    val glass = if (glassId == 0) null else context.resources.getStringArray(R.array.glass_id)[glassId]
    val taste = context.resources.getStringArray(R.array.taste_id)[tasteId]
    val ingredient =
        if (ingredientId == 0) null else context.resources.getStringArray(R.array.ingridient_type)[ingredientId]
    val tastes: Array<String>? = if (tasteId == 0) null else arrayOf(taste!!)
    val ingredients: Array<String>? = if (ingredientId == 0) null else arrayOf(ingredient!!)
    val skills = Array(skill + 1) { (it + 1).toString() }
    val colors = context.resources.getIntArray(R.array.colors)
    val colorId = colors.indexOf(color)
    val colorResult =
        if (colorId > 0) context.resources.getStringArray(R.array.colors_name)[colorId] else null

    return DrinksPathManager.Builder()
        .setGlassType(glass)
        .setIngredient(ingredients)
        .setTaste(tastes)
        .setRating(0, 100)
        .setSkill(skills)
        .setCarbonated(carbonated)
        .setAlcoholic(alcoholic)
        .setColor(colorResult)
        .build()
        .path
}
