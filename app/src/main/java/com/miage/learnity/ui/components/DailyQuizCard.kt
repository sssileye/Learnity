package com.miage.learnity.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * Card Vibrant du Quiz du Jour avec progression hebdomadaire intégrée
 * Version mise à jour avec WeeklyProgressCard
 */
@Composable
fun DailyQuizCard(
    dimensions: ResponsiveDimensions,
    isDiscoveryMode: Boolean,
    lastScore: Pair<Int, Int>?,
    weeklyProgress: Pair<Int, Int>?, // (completed, total) - NOUVEAU
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

    // 🎨 Gradient selon état
    val gradient = if (hasDoneQuizToday) {
        // Complété : gradient bleu-violet
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6B9FFF), // Bleu
                Color(0xFF7C6FFF)  // Violet
            )
        )
    } else {
        // À faire : gradient violet-rose
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF7C6FFF), // Violet clair
                Color(0xFFFF6FB5)  // Rose
            )
        )
    }

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
                                color = Color.White
                            )
                        }

                        // Badge mode
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (isDiscoveryMode) "Découverte" else "Révision",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
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
                                color = Color.White
                            )
                            Text(
                                text = "/$totalQuestions",
                                fontSize = dimensions.titleLarge,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.offset(y = (-8).dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                        // Boutons d'action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                        ) {
                            OutlinedButton(
                                onClick = { onAction(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensions.buttonHeightSmall),
                                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    Color.White
                                )
                            ) {
                                Text(
                                    "Revoir",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimensions.bodyMedium
                                )
                            }

                            Button(
                                onClick = { onAction(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensions.buttonHeightSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                            ) {
                                Text(
                                    "Refaire",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimensions.bodyMedium,
                                    color = Color(0xFF635BFF)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

                        Button(
                            onClick = { onAction(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
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
                                color = Color(0xFF635BFF)
                            )
                        }
                    }
                }
            }
        }

        // ⭐ NOUVEAU : WeeklyProgressCard au lieu du texte statique
        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

        weeklyProgress?.let { (completed, total) ->
            WeeklyProgressCard(
                completedSessions = completed,
                totalGoal = total,
                dimensions = dimensions
            )
        } ?: run {
            // Fallback si les données ne sont pas encore chargées
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cette semaine",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Chargement...",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

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