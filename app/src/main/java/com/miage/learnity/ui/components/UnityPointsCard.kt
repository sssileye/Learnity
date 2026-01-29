package com.miage.learnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * Card Vibrant affichant les Unity Points
 * Gradient vert-violet énergique symbolisant la progression
 */
@Composable
fun UnityPointsCard(
    currentPoints: Int,
    nextDonationGoal: Int = 2000,
    onViewImpactClick: () -> Unit,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    val progress = (currentPoints.toFloat() / nextDonationGoal.toFloat()).coerceIn(0f, 1f)
    val pointsRemaining = (nextDonationGoal - currentPoints).coerceAtLeast(0)
    val isGoalReached = currentPoints >= nextDonationGoal

    // 🎨 Gradient vert-violet vibrant
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF56CCF2), // Bleu clair
            Color(0xFF2F80ED)  // Bleu
        )
    )

    Card(
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation * 1.5f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(dimensions.cardPadding)
        ) {
            Column {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Unity Points",
                            fontSize = dimensions.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tes points accumulés",
                            fontSize = dimensions.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Indicateur circulaire
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(dimensions.iconSizeLarge + 4.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 5.dp
                        )
                        Surface(
                            modifier = Modifier.size(dimensions.iconSizeLarge - 8.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✨",
                                    fontSize = dimensions.titleMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                // Points actuels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentPoints",
                            fontSize = dimensions.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " pts",
                            fontSize = dimensions.titleLarge,
                            color = Color.White,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }

                    TextButton(
                        onClick = onViewImpactClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Mon impact →",
                            fontWeight = FontWeight.Bold,
                            fontSize = dimensions.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                // Progression vers prochain don
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Prochain don",
                            fontSize = dimensions.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = if (pointsRemaining > 0)
                                "Plus que $pointsRemaining pts"
                            else
                                "Objectif atteint !",
                            fontSize = dimensions.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )

                    if (progress >= 1f) {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2)
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = dimensions.titleMedium
                            )
                            Text(
                                text = "Tu peux effectuer un don !",
                                fontSize = dimensions.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Moyen (360dp)", widthDp = 360)
@Composable
fun UnityPointsCardVibrantPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En progression
            UnityPointsCard(
                currentPoints = 1540,
                nextDonationGoal = 2000,
                onViewImpactClick = {},
                dimensions = rememberResponsiveDimensions()
            )

            // Objectif atteint
            UnityPointsCard(
                currentPoints = 2150,
                nextDonationGoal = 2000,
                onViewImpactClick = {},
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}