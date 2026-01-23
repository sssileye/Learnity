package com.miage.learnity.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// 1. On définit tes couleurs personnalisées pour le mode sombre (Bleu Nuit)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9155FD),
    background = Color(0xFF0F0B3A),
    surface = Color(0xFF1E1A4D),
    onBackground = Color.White,
    onSurface = Color.White
)

// 2. On définit tes couleurs pour le mode clair
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    background = Color(0xFFF2F4F7),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun LearnityTheme(
    themeViewModel: ThemeViewModel = viewModel(), // On injecte le ViewModel ici
    content: @Composable () -> Unit
) {
    // On récupère les valeurs actuelles du ViewModel
    val darkTheme = themeViewModel.isDarkMode.value
    val fontScale = themeViewModel.fontScale.value

    // 3. Calcul du multiplicateur de police
    val multiplier = when {
        fontScale < 0.3f -> 0.85f // Petit
        fontScale > 0.7f -> 1.25f // Grand
        else -> 1.0f              // Normal
    }

    // 4. Création d'une typographie dynamique
    val dynamicTypography = Typography(
        headlineMedium = TextStyle(
            fontSize = (28 * multiplier).sp,
            fontWeight = FontWeight.Bold
        ),
        titleLarge = TextStyle(
            fontSize = (20 * multiplier).sp,
            fontWeight = FontWeight.SemiBold
        ),
        bodyLarge = TextStyle(
            fontSize = (16 * multiplier).sp
        ),
        bodyMedium = TextStyle(
            fontSize = (14 * multiplier).sp
        )
    )

    // 5. Sélection du schéma de couleurs
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography, // On applique la typographie calculée
        content = content
    )
}