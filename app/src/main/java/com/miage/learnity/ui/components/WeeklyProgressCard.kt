package com.miage.learnity.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.theme.successColors
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * ═══════════════════════════════════════════════════════════════
 * 📊 WEEKLY PROGRESS CARD - VERSION DARK MODE COMPATIBLE
 * ═══════════════════════════════════════════════════════════════
 *
 * Composant affichant la progression hebdomadaire des quiz
 * ✅ Barre de progression animée avec objectif de séances
 * ✅ Utilise MaterialTheme.successColors pour le dark mode
 */
@Composable
fun WeeklyProgressCard(
    completedSessions: Int,
    totalGoal: Int = 4,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    val progress = (completedSessions.toFloat() / totalGoal.toFloat()).coerceIn(0f, 1f)
    val isGoalReached = completedSessions >= totalGoal

    // Animation de la barre de progression
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "weekly_progress"
    )

    // Message selon progression
    val (emoji, message) = when {
        isGoalReached -> "🎉" to "Objectif atteint !"
        completedSessions >= totalGoal * 0.75f -> "🔥" to "Presque là"
        completedSessions >= totalGoal * 0.5f -> "💪" to "Bon rythme"
        completedSessions > 0 -> "⚡" to "Continue"
        else -> "🎯" to "À toi de jouer"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2)
    ) {
        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cette semaine",
                fontSize = dimensions.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,  // ✅ Déjà adaptatif
                fontWeight = FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    text = "$completedSessions/$totalGoal séances",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.primary,  // ✅ Déjà adaptatif
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ✅ Barre de progression - Utilise successColors au lieu de Color hardcodé
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isGoalReached)
                MaterialTheme.successColors.success  // ✅ ADAPTATIF (remplace Color(0xFF4CAF50))
            else
                MaterialTheme.colorScheme.primary,  // ✅ Déjà adaptatif
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),  // ✅ Déjà adaptatif
        )

        // Message motivant
        Text(
            text = message,
            fontSize = dimensions.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,  // ✅ Déjà adaptatif
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 PREVIEWS - Light & Dark Mode
// ═══════════════════════════════════════════════════════════════

@Preview(name = "Petit (320dp)", widthDp = 320)
@Preview(name = "Moyen (360dp)", widthDp = 360)
@Preview(name = "Grand (410dp)", widthDp = 410)
@Composable
fun WeeklyProgressCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Début de semaine
            WeeklyProgressCard(
                completedSessions = 1,
                totalGoal = 4,
                dimensions = rememberResponsiveDimensions()
            )

            // Milieu de semaine
            WeeklyProgressCard(
                completedSessions = 2,
                totalGoal = 4,
                dimensions = rememberResponsiveDimensions()
            )

            // Presque fini
            WeeklyProgressCard(
                completedSessions = 3,
                totalGoal = 4,
                dimensions = rememberResponsiveDimensions()
            )

            // Objectif atteint
            WeeklyProgressCard(
                completedSessions = 4,
                totalGoal = 4,
                dimensions = rememberResponsiveDimensions()
            )

            // Dépassé l'objectif
            WeeklyProgressCard(
                completedSessions = 6,
                totalGoal = 4,
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}