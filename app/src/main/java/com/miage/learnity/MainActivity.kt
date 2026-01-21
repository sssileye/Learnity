package com.miage.learnity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.miage.learnity.ui.navigation.AppNav
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Installer le splash natif personnalisé
        val splashScreen = installSplashScreen()

        //Garder le splash visible pendant 2 secondes
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        super.onCreate(savedInstanceState)

        //Délai pour le splash
        lifecycleScope.launch {
            delay(2000) // 2 secondes
            keepSplashOnScreen = false
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }
}