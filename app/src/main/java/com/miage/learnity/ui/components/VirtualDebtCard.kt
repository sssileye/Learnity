package com.miage.learnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * Card Vibrant affichant la dette virtuelle
 * Gradient orange-rouge si dette, vert si pas de dette
 */
@Composable
fun VirtualDebtCard(
    debtAmount: Double,
    monthsRemaining: Int,
    onPayClick: () -> Unit,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    val hasDebt = debtAmount > 0.01

    // 🎨 Gradient selon état de la dette
    val gradient = if (hasDebt) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF9A56), // Orange
                Color(0xFFFF6B6B)  // Rouge
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4ECDC4), // Turquoise
                Color(0xFF44A08D)  // Vert
            )
        )
    }

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
                            text = "Dette virtuelle",
                            fontSize = dimensions.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ce mois-ci",
                            fontSize = dimensions.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Icône circulaire
                    Surface(
                        modifier = Modifier.size(dimensions.iconSizeLarge),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (hasDebt) "💰" else "✨",
                                fontSize = dimensions.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                // Montant de la dette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.2f".format(debtAmount),
                            fontSize = dimensions.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = " €",
                            fontSize = dimensions.titleLarge,
                            color = Color.White,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }

                    if (hasDebt) {
                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Solder",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimensions.bodyMedium,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }

                if (hasDebt) {
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                    // Barre de progression vers échéance
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "À régler avant",
                                fontSize = dimensions.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = if (monthsRemaining > 1) "$monthsRemaining mois" else "$monthsRemaining mois",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2)
                    ) {
                        Text(
                            text = "Aucune dette ce mois-ci !",
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

@Preview(name = "Moyen (360dp)", widthDp = 360)
@Composable
fun VirtualDebtCardVibrantPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avec dette
            VirtualDebtCard(
                debtAmount = 12.50,
                monthsRemaining = 4,
                onPayClick = {},
                dimensions = rememberResponsiveDimensions()
            )

            // Sans dette
            VirtualDebtCard(
                debtAmount = 0.0,
                monthsRemaining = 0,
                onPayClick = {},
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}