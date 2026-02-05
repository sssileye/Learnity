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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * 📝 DAILY QUIZ CARD - VERSION MINIMALISTE VIBRANTE
 * ✅ Retrait total des emojis pour un aspect plus Pro
 * ✅ Focus sur le gradient et le score
 */
@Composable
fun DailyQuizCard(
    dimensions: ResponsiveDimensions,
    isDiscoveryMode: Boolean,
    lastScore: Pair<Int, Int>?,
    weeklyProgress: Pair<Int, Int>?,
    onAction: (isReview: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasDoneQuizToday = lastScore != null
    val scoreValue = lastScore?.first ?: 0
    val totalQuestions = lastScore?.second ?: 10

    // Message sobre sans icône
    val message = when {
        !hasDoneQuizToday -> "Prêt pour le défi ?"
        scoreValue >= 9 -> "Score excellent"
        scoreValue >= 7 -> "Très bon travail"
        else -> "Continue tes efforts !"
    }

    val gradient = if (hasDoneQuizToday) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF667EEA), Color(0xFFF093FB))
        )
    }

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
                    // En-tête simplifié
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasDoneQuizToday) message else "Quiz du jour",
                            fontSize = dimensions.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        // Badge mode
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            color = overlayColor
                        ) {
                            Text(
                                text = if (isDiscoveryMode) "DÉCOUVERTE" else "RÉVISION",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Black, // Plus marqué pour compenser l'icône
                                color = textColor,
                                modifier = Modifier.padding(
                                    horizontal = dimensions.itemSpacing,
                                    vertical = dimensions.itemSpacing / 2
                                )
                            )
                        }
                    }

                    if (hasDoneQuizToday) {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.2f))

                        // Score centré et imposant
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$scoreValue",
                                fontSize = dimensions.displayLarge * 1.3f,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "/$totalQuestions",
                                fontSize = dimensions.titleLarge,
                                color = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.offset(y = (-6).dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.2f))

                        Button(
                            onClick = { onAction(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.buttonHeightSmall),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                        ) {
                            Text(
                                "REFAIRE LE TEST",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimensions.bodyMedium,
                                color = Color(0xFF764BA2) // Rappel de la couleur du gradient
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2f))

                        Button(
                            onClick = { onAction(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.buttonHeightSmall)
                        ) {
                            Text(
                                "COMMENCER",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimensions.bodyMedium,
                                color = Color(0xFF667EEA)
                            )
                        }
                    }
                }
            }
        }
    }
}