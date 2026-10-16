package com.sorokinuladzimir.homebarassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.ui.about.ResourcesScreen

class ResourcesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeBarTheme(variant = homeBarThemeVariant()) {
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
