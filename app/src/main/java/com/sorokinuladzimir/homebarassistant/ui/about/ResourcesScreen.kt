package com.sorokinuladzimir.homebarassistant.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorokinuladzimir.homebarassistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    onBack: () -> Unit,
    onOpenAbsolut: () -> Unit,
    onOpenFreepik: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_resources)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            ResourceCard(
                iconRes = R.drawable.ic_absolut_logo,
                text = stringResource(R.string.absolut_credits),
                onClick = onOpenAbsolut
            )
            ResourceCard(
                iconRes = R.drawable.ic_freepik_logo,
                text = stringResource(R.string.freepic_credits),
                onClick = onOpenFreepik
            )
        }
    }
}

@Composable
private fun ResourceCard(iconRes: Int, text: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
