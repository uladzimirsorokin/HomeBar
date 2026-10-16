package com.sorokinuladzimir.homebarassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.ThemeVariant
import com.sorokinuladzimir.homebarassistant.ui.about.ResourcesScreen
import com.sorokinuladzimir.homebarassistant.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResourcesActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeId by themeViewModel.themeId.collectAsStateWithLifecycle()
            HomeBarTheme(variant = ThemeVariant.fromId(themeId)) {
                ResourcesScreen(
                    onBack = { finish() },
                    onOpenAbsolut = { openWebsite(LINK_ABSOLUT_DRINKS) },
                    onOpenFreepik = { openWebsite(LINK_FREEPIC) }
                )
            }
        }
    }

    private fun openWebsite(link: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    private companion object {
        const val LINK_ABSOLUT_DRINKS = "https://www.absolutdrinks.com"
        const val LINK_FREEPIC = "https://www.freepik.com/"
    }
}
