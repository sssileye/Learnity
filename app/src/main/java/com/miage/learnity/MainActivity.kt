package com.miage.learnity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.miage.learnity.ui.navigation.AppNav // Vérifie bien ton package d'import

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 2. Configuration Edge-to-Edge (fond transparent pour les barres)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // 3. MASQUER LES WINDOW INSETS (Mode Immersif)
        hideSystemBars()

        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // On configure le comportement : les barres ne reviennent que par un "swipe"
        // et se recachent automatiquement après quelques secondes.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // On cache la barre de statut (haut) et la barre de navigation (bas)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}