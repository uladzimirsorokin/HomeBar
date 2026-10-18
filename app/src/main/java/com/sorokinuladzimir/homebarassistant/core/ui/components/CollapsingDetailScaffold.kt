package com.sorokinuladzimir.homebarassistant.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

private val ExpandedHeaderHeight = 260.dp
private val CollapsedHeaderHeight = 56.dp
private val ContentBottomPadding = 96.dp

/**
 * Detail screen scaffold with a collapsing image header.
 *
 * The header image starts expanded and shrinks as the content is scrolled up until only the
 * toolbar (back button + title) remains. Works with [ColumnScope] content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingDetailScaffold(
    title: String,
    image: Any?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onImageSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val collapsedHeightPx = with(density) { CollapsedHeaderHeight.toPx() } + statusBarHeight
    val expandedHeightPx = with(density) { ExpandedHeaderHeight.toPx() } + statusBarHeight

    val scrollState = rememberScrollState()
    val headerHeightPx =
        (expandedHeightPx - scrollState.value).coerceIn(collapsedHeightPx, expandedHeightPx)
    val collapsedFraction =
        ((expandedHeightPx - headerHeightPx) / (expandedHeightPx - collapsedHeightPx))
            .coerceIn(0f, 1f)

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = floatingActionButton
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = with(density) { expandedHeightPx.toDp() },
                        bottom = ContentBottomPadding
                    )
            ) {
                content()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { headerHeightPx.toDp() })
            ) {
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onSuccess = onImageSuccess
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.45f * (1f - collapsedFraction) + 0.15f),
                                0.4f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.45f)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = with(density) { statusBarHeight.toDp() })
                        .height(CollapsedHeaderHeight)
                        .padding(start = 4.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
