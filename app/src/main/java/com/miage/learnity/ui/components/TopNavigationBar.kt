package com.miage.learnity.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import androidx.compose.foundation.layout.statusBarsPadding
import com.miage.learnity.ui.utils.*

/**
 * TopNavigationBar optimisé avec avatar dynamique et badge streak
 */
@Composable
fun TopNavigationBar(
    currentStreak: Int = 0,
    photoUrl: String? = null, // ⭐ Ajout du paramètre pour l'avatar
    onProfileClick: () -> Unit,
    onLogoClick: () -> Unit = {},
    onStreakClick: (() -> Unit)? = null
) {
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    // ⭐ Logique de récupération de l'avatar (identique au ProfileScreen)
    val avatarResId = remember(photoUrl) {
        val photoName = if (photoUrl.isNullOrBlank()) "avatar_b1" else photoUrl
        try {
            val id = context.resources.getIdentifier(photoName, "drawable", context.packageName)
            if (id != 0) id else R.drawable.avatar_b1
        } catch (e: Exception) { R.drawable.avatar_b1 }
    }

    // Animation flamme
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenPaddingHorizontal,
                    vertical = dimensions.itemSpacing * 0.8f
                )
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo LEARNITY
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onLogoClick)
                    .padding(dimensions.itemSpacing / 3)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo Learnity",
                    modifier = Modifier.size(dimensions.iconSizeLarge),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(dimensions.itemSpacing / 3))
                Text(
                    text = "LEARNITY",
                    fontSize = dimensions.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing * 0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Streak
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = if (onStreakClick != null) {
                        Modifier.clickable(onClick = onStreakClick)
                    } else Modifier
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensions.itemSpacing * 0.7f,
                            vertical = dimensions.itemSpacing / 2.5f
                        ),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = dimensions.bodyMedium * if (currentStreak > 0) scale else 1f
                        )
                        Text(
                            text = "$currentStreak",
                            fontSize = dimensions.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // ⭐ Bouton Profil avec Avatar Dynamique
                Box(
                    modifier = Modifier
                        .size(dimensions.iconSizeLarge * 0.95f) // Taille ajustée
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320)
@Preview(name = "Moyen (360dp)", widthDp = 360)
@Preview(name = "Grand (410dp)", widthDp = 410)
@Composable
fun TopNavigationBarOptimizedPreview() {
    MaterialTheme {
        Column {
            // Avec streak
            TopNavigationBar(
                currentStreak = 12,
                onProfileClick = { }
            )

            Divider()

            // Sans streak
            TopNavigationBar(
                currentStreak = 0,
                onProfileClick = { }
            )
        }
    }
}