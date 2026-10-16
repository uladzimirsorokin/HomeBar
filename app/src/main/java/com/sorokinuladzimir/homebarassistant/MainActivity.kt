package com.sorokinuladzimir.homebarassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sorokinuladzimir.homebarassistant.core.ui.theme.HomeBarTheme
import com.sorokinuladzimir.homebarassistant.core.ui.theme.homeBarThemeVariant
import com.sorokinuladzimir.homebarassistant.ui.navigation.HomeBarApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeBarTheme(variant = homeBarThemeVariant()) {
                HomeBarApp()
            }
        }
    }
}
