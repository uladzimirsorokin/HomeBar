package com.sorokinuladzimir.homebarassistant.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorokinuladzimir.homebarassistant.R
import com.sorokinuladzimir.homebarassistant.core.util.MeasureParser
import com.sorokinuladzimir.homebarassistant.viewmodel.AiScanUiState

@Composable
fun AiScanScreen(
    state: AiScanUiState,
    onPickPhoto: () -> Unit,
    onRecognize: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val allowedUnits = remember {
        context.resources.getStringArray(R.array.ingredient_unit).toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (state) {
            AiScanUiState.Idle -> {
                Text(stringResource(R.string.scan_hint), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_pick_photo))
                }
            }

            is AiScanUiState.Picked -> {
                AsyncImage(
                    model = state.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 420.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Button(onClick = onRecognize, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_recognize))
                }
                OutlinedButton(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_other_photo))
                }
            }

            AiScanUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                Text(
                    text = stringResource(R.string.scan_recognizing),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is AiScanUiState.Scanned -> {
                val cocktail = state.cocktail
                state.preview?.let { preview ->
                    AsyncImage(
                        model = preview,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 420.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Text(
                    text = stringResource(
                        if (state.fromDatabase) R.string.scan_source_db else R.string.scan_source_ai
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = cocktail.name.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall
                )

                if (cocktail.ingredients.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.scan_ingredients),
                        style = MaterialTheme.typography.titleMedium
                    )
                    cocktail.ingredients.forEach { ingredient ->
                        val parsed = MeasureParser.parse(ingredient.measure, allowedUnits)
                        val amount = listOfNotNull(parsed.amount, parsed.unit)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ingredient.name.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (amount.isNotBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = amount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                cocktail.instructions?.let { instructions ->
                    Text(
                        text = stringResource(R.string.scan_instructions),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = instructions, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(4.dp))
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_save))
                }
                OutlinedButton(onClick = onRecognize, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_recognize))
                }
            }

            is AiScanUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_pick_photo))
                }
            }

            AiScanUiState.Saved -> Unit
        }
    }
}
