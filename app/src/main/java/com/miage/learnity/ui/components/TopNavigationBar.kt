package com.miage.learnity.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.miage.learnity.ui.utils.*

/**
 * TopNavigationBar optimisé avec badge streak cliquable et bouton aide
 */
@Composable
fun TopNavigationBar(
    currentStreak: Int = 0,
    onLogoClick: () -> Unit = {},
    onStreakClick: (() -> Unit)? = null, // ⭐ Reçoit l'action pour ouvrir le Dialog
    onHelpClick: () -> Unit = {}
) {
    val dimensions = rememberResponsiveDimensions()

    // Animation flamme (pulsation)
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
                // Badge Streak (CLIQUABLE)
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

                // Bouton Help
                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier.size(dimensions.iconSizeLarge * 0.9f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensions.iconSizeLarge * 0.9f)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Aide",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(dimensions.iconSizeMedium * 0.85f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog explicatif du Winstreak et des multiplicateurs
 */
@Composable
fun StreakHelpDialog(
    currentStreak: Int,
    multiplier: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("🔥", fontSize = 32.sp) }, // Optionnel : icone en haut
        title = {
            Text(
                text = "Série d'assiduité",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Vous avez une série de $currentStreak jours !",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )

                // Encart multiplicateur actuel
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Multiplicateur actuel", fontSize = 12.sp)
                        Text(
                            "x$multiplier",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text("Barème des bonus (Quiz du Jour) :", fontWeight = FontWeight.Bold)

                val bareme = listOf(
                    "3 jours" to "x1.1",
                    "7 jours" to "x1.2",
                    "15 jours" to "x1.5",
                    "30 jours" to "x2.0"
                )

                bareme.forEach { (jours, mult) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(jours)
                        Text(mult, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Text(
                    "Conseil : Ne manquez aucun jour pour ne pas perdre votre bonus !",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Compris !") }
        }
    )
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
                onHelpClick = { }
            )

            HorizontalDivider()

            // Sans streak
            TopNavigationBar(
                currentStreak = 0,
                onHelpClick = { }
            )
        }
    }
}