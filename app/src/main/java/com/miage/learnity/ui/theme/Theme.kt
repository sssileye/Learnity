package com.miage.learnity.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ═══════════════════════════════════════════════════════════════
// 🎨 EXTENSION POUR COULEURS SUCCESS
// ═══════════════════════════════════════════════════════════════

data class SuccessColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
)

val LocalSuccessColors = staticCompositionLocalOf {
    SuccessColors(
        success = SuccessLight,
        onSuccess = OnSuccessLight,
        successContainer = SuccessContainerLight,
        onSuccessContainer = OnSuccessContainerLight
    )
}

val MaterialTheme.successColors: SuccessColors
    @Composable
    get() = LocalSuccessColors.current

// ═══════════════════════════════════════════════════════════════
// 🎨 COLOR SCHEMES AMÉLIORÉS
// ═══════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    onError = OnErrorDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    error = ErrorLight,
    errorContainer = ErrorContainerLight,
    onError = OnErrorLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun LearnityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // ✅ Couleurs de succès adaptatives selon le thème
    val successColors = if (darkTheme) {
        SuccessColors(
            success = SuccessDark,
            onSuccess = OnSuccessDark,
            successContainer = SuccessContainerDark,
            onSuccessContainer = OnSuccessContainerDark
        )
    } else {
        SuccessColors(
            success = SuccessLight,
            onSuccess = OnSuccessLight,
            successContainer = SuccessContainerLight,
            onSuccessContainer = OnSuccessContainerLight
        )
    }

    CompositionLocalProvider(LocalSuccessColors provides successColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}