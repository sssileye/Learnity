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
 * 💰 VIRTUAL DEBT CARD - VERSION ÉPURÉE
 * ✅ Supprimé : Barre de progression et échéance
 * ✅ Conservé : Gradients vibrants et boutons d'action
 */
@Composable
fun VirtualDebtCard(
    debtAmount: Double,
    onPayClick: () -> Unit,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    val hasDebt = debtAmount > 0.01

    // GRADIENTS VIBRANTS FORCÉS
    val gradient = if (hasDebt) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF9A56), // Orange vibrant
                Color(0xFFFF6B6B)  // Rouge vibrant
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4ECDC4), // Turquoise vibrant
                Color(0xFF44A08D)  // Vert vibrant
            )
        )
    }

    val textColor = Color.White
    val overlayColor = Color.White.copy(alpha = 0.25f)

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
                            color = textColor
                        )
                        Text(
                            text = if (hasDebt) "Soutien à régulariser" else "Compteur à jour",
                            fontSize = dimensions.bodySmall,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }

                    // Icône circulaire
                    Surface(
                        modifier = Modifier.size(dimensions.iconSizeLarge),
                        shape = CircleShape,
                        color = overlayColor
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

                // Montant de la dette et bouton d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%.2f".format(debtAmount),
                            fontSize = dimensions.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = " €",
                            fontSize = dimensions.titleLarge,
                            color = textColor,
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }

                    if (hasDebt) {
                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
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

                // Pied de carte informatif
                if (!hasDebt) {
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                    Text(
                        text = "Aucune dette ce mois-ci !",
                        fontSize = dimensions.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(name = "Preview Dette", widthDp = 360)
@Composable
fun VirtualDebtCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VirtualDebtCard(
                debtAmount = 12.50,
                onPayClick = {},
                dimensions = rememberResponsiveDimensions()
            )
            VirtualDebtCard(
                debtAmount = 0.0,
                onPayClick = {},
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}