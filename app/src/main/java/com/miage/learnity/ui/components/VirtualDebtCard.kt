package com.miage.learnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * ═══════════════════════════════════════════════════════════════
 * 💰 VIRTUAL DEBT CARD - VERSION DARK MODE COMPATIBLE
 * ═══════════════════════════════════════════════════════════════
 *
 * Card affichant la dette virtuelle
 * ✅ Gradient orange-rouge adaptatif si dette
 * ✅ Gradient turquoise-vert adaptatif si pas de dette
 * ✅ Dark mode support via gradients adaptatifs
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

    // ✅ GRADIENTS ADAPTATIFS selon état de la dette
    val gradient = if (hasDebt) {
        debtGradient()      // Orange-rouge (adaptatif)
    } else {
        noDebtGradient()    // Turquoise-vert (adaptatif)
    }

    // ✅ COULEURS DE TEXTE ADAPTATIVES
    val textColor = getOnGradientTextColor()
    val overlayColor = getGradientOverlayColor(alpha = 0.25f)

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
                            color = textColor  // ✅ ADAPTATIF
                        )
                        Text(
                            text = "Ce mois-ci",
                            fontSize = dimensions.bodySmall,
                            color = textColor.copy(alpha = 0.9f)  // ✅ ADAPTATIF
                        )
                    }

                    // Icône circulaire
                    Surface(
                        modifier = Modifier.size(dimensions.iconSizeLarge),
                        shape = CircleShape,
                        color = overlayColor  // ✅ ADAPTATIF
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
                            color = textColor  // ✅ ADAPTATIF
                        )
                        Text(
                            text = " €",
                            fontSize = dimensions.titleLarge,
                            color = textColor,  // ✅ ADAPTATIF
                            modifier = Modifier.offset(y = (-6).dp)
                        )
                    }

                    if (hasDebt) {
                        Button(
                            onClick = onPayClick,
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface  // ✅ ADAPTATIF
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Solder",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimensions.bodyMedium,
                                color = MaterialTheme.colorScheme.error  // ✅ ADAPTATIF (rouge)
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
                                color = textColor.copy(alpha = 0.9f)  // ✅ ADAPTATIF
                            )
                            Text(
                                text = if (monthsRemaining > 1) "$monthsRemaining mois" else "$monthsRemaining mois",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor  // ✅ ADAPTATIF
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = textColor,  // ✅ ADAPTATIF
                            trackColor = textColor.copy(alpha = 0.3f),  // ✅ ADAPTATIF
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
                            color = textColor,  // ✅ ADAPTATIF
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 PREVIEWS - Light & Dark Mode
// ═══════════════════════════════════════════════════════════════

@Preview(name = "Moyen (360dp) - Light", widthDp = 360)
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