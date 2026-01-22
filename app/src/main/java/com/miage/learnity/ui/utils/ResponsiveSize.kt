package com.miage.learnity.ui.utils

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Extensions pour calculs responsives (Scalable DP)
 */
@Composable
fun Int.sdp(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val baseWidth = 360.dp
    val scaleFactor = (screenWidth / baseWidth)

    // On calcule d'abord le Float, on applique les limites, PUIS on transforme en .dp
    val finalValue = (this.toFloat() * scaleFactor).coerceIn(
        (this * 0.5f),
        (this * 1.8f)
    )
    return finalValue.dp
}

/**
 * Scaled SP - Taille de police adaptative
 */
@Composable
fun Int.ssp(): TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val baseWidth = 360.dp
    val scaleFactor = (screenWidth / baseWidth)

    // Idem : Calcul en Float d'abord pour que le coerceIn fonctionne
    val finalValue = (this.toFloat() * scaleFactor).coerceIn(
        (this * 0.75f),
        (this * 1.5f)
    )
    return finalValue.sp
}

/**
 * Pourcentages et dimensions regroupées
 */
@Composable
fun rememberResponsiveDimensions(): ResponsiveDimensions {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return ResponsiveDimensions(
        screenPaddingHorizontal = (screenWidth * 0.045f).coerceIn(12.dp, 32.dp),
        screenPaddingVertical = (screenHeight * 0.025f).coerceIn(12.dp, 24.dp),
        itemSpacing = (screenWidth * 0.03f).coerceIn(8.dp, 20.dp),
        cardPadding = (screenWidth * 0.045f).coerceIn(12.dp, 24.dp),
        maxContentWidth = (screenWidth * 0.8f).coerceIn(600.dp, 900.dp),

        displayLarge = 40.ssp(),
        titleLarge = 28.ssp(),
        titleMedium = 20.ssp(),
        bodyLarge = 16.ssp(),
        bodyMedium = 14.ssp(),
        bodySmall = 12.ssp(),
        labelLarge = 16.ssp(),

        iconSizeSmall = 20.sdp(),
        iconSizeMedium = 24.sdp(),
        iconSizeLarge = 48.sdp(),
        logoSize = 100.sdp(),
        profilePictureSize = 96.sdp(),

        buttonHeight = 56.sdp(),
        buttonHeightSmall = 48.sdp(),

        cardElevation = 2.dp,
        cornerRadiusSmall = 8.dp,
        cornerRadiusMedium = 12.dp,
        cornerRadiusLarge = 16.dp,
        bottomNavHeight = 64.dp,
        topBarHeight = 64.dp
    )
}

data class ResponsiveDimensions(
    val screenPaddingHorizontal: Dp,
    val screenPaddingVertical: Dp,
    val itemSpacing: Dp,
    val cardPadding: Dp,
    val maxContentWidth: Dp,
    val displayLarge: TextUnit,
    val titleLarge: TextUnit,
    val titleMedium: TextUnit,
    val bodyLarge: TextUnit,
    val bodyMedium: TextUnit,
    val bodySmall: TextUnit,
    val labelLarge: TextUnit,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,
    val logoSize: Dp,
    val profilePictureSize: Dp,
    val buttonHeight: Dp,
    val buttonHeightSmall: Dp,
    val cardElevation: Dp,
    val cornerRadiusSmall: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusLarge: Dp,
    val bottomNavHeight: Dp,
    val topBarHeight: Dp
)

fun Modifier.responsiveMaxWidth(dimensions: ResponsiveDimensions): Modifier {
    return this.widthIn(max = dimensions.maxContentWidth)
}