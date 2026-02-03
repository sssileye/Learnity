package com.miage.learnity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * ═══════════════════════════════════════════════════════════════
 * 📝 DAILY QUIZ CARD - VERSION GRADIENTS VIBRANTS
 * ═══════════════════════════════════════════════════════════════
 *
 * Card Vibrant du Quiz du Jour avec progression hebdomadaire intégrée
 * ✅ Gradients VIBRANTS forcés (même en dark mode)
 * ✅ Structure IDENTIQUE au code original
 */
@Composable
fun DailyQuizCard(
    dimensions: ResponsiveDimensions,
    isDiscoveryMode: Boolean,
    lastScore: Pair<Int, Int>?,
    weeklyProgress: Pair<Int, Int>?, // (completed, total)
    onAction: (isReview: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasDoneQuizToday = lastScore != null
    val scoreValue = lastScore?.first ?: 0
    val totalQuestions = lastScore?.second ?: 10

    // Message selon performance
    val (icon, message) = when {
        !hasDoneQuizToday -> "" to "Prêt pour le défi ?"
        scoreValue >= 9 -> "" to "Score excellent"
        scoreValue >= 7 -> "" to "Très bon travail"
        else -> "📚" to "Continue tes efforts"
    }

    // ✅ CHANGEMENT ICI : Gradients VIBRANTS forcés au lieu d'adaptatifs
    val gradient = if (hasDoneQuizToday) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA),  // Bleu vibrant
                Color(0xFF764BA2)   // Violet vibrant
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF667EEA),  // Violet
                Color(0xFFF093FB)   // Rose vibrant
            )
        )
    }

    // ✅ CHANGEMENT ICI : Couleurs de texte forcées au lieu d'adaptatives
    val textColor = Color.White
    val overlayColor = Color.White.copy(alpha = 0.25f)

    Column(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation * 1.5f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradient)
                    .padding(dimensions.cardPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // En-tête
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = icon,
                                fontSize = dimensions.titleMedium
                            )
                            Text(
                                text = if (hasDoneQuizToday) message else "Quiz du jour",
                                fontSize = dimensions.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }

                        // Badge mode
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            color = overlayColor
                        ) {
                            Text(
                                text = if (isDiscoveryMode) "Découverte" else "Révision",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.padding(
                                    horizontal = dimensions.itemSpacing,
                                    vertical = dimensions.itemSpacing / 2
                                )
                            )
                        }
                    }

                    if (hasDoneQuizToday) {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                        // Score affiché
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$scoreValue",
                                fontSize = dimensions.displayLarge * 1.5f,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "/$totalQuestions",
                                fontSize = dimensions.titleLarge,
                                color = textColor.copy(alpha = 0.9f),
                                modifier = Modifier.offset(y = (-8).dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                        // Boutons d'action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                        ) {
                            Button(
                                onClick = { onAction(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensions.buttonHeightSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                            ) {
                                Text(
                                    "Refaire",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimensions.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

                        Button(
                            onClick = { onAction(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.buttonHeightSmall),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                "Commencer maintenant",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimensions.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

//        // WeeklyProgressCard
//        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
//
//        weeklyProgress?.let { (completed, total) ->
//            WeeklyProgressCard(
//                completedSessions = completed,
//                totalGoal = total,
//                dimensions = dimensions
//            )
//        } ?: run {
//            // Fallback si les données ne sont pas encore chargées
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Cette semaine",
//                    fontSize = dimensions.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontWeight = FontWeight.Medium
//                )
//                Text(
//                    text = "Chargement...",
//                    fontSize = dimensions.bodySmall,
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 PREVIEWS
// ═══════════════════════════════════════════════════════════════

@Preview(name = "Moyen (360dp)", widthDp = 360)
@Composable
fun DailyQuizCardWithProgressPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quiz non fait
            DailyQuizCard(
                dimensions = rememberResponsiveDimensions(),
                isDiscoveryMode = true,
                lastScore = null,
                weeklyProgress = 2 to 4,
                onAction = {}
            )

            // Quiz fait avec bon score
            DailyQuizCard(
                dimensions = rememberResponsiveDimensions(),
                isDiscoveryMode = false,
                lastScore = 9 to 10,
                weeklyProgress = 3 to 4,
                onAction = {}
            )
        }
    }
}