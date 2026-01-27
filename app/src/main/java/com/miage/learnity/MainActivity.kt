package com.miage.learnity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.miage.learnity.ui.navigation.AppNav
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.theme.ThemeViewModel
import com.miage.learnity.ui.theme.ThemeViewModelFactory
import com.miage.learnity.ui.utils.LocalFontSize

class MainActivity : ComponentActivity() {

    // ✅ ViewModel créé avec factory
    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        hideSystemBars()

        setContent {
            // ✅ Observe le thème via ViewModel
            val settings by themeViewModel.settings.collectAsState()

            LearnityTheme(darkTheme = settings.isDarkMode) {
                CompositionLocalProvider(LocalFontSize provides settings.fontSize) {
                    AppNav()
                }
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}