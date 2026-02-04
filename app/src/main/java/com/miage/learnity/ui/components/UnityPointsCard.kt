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
 * ✨ UNITY POINTS CARD - VERSION ÉPURÉE
 * ✅ Supprimé : Jauge circulaire, barre de progression et "Mon impact"
 * ✅ Conservé : Gradient vibrant bleu et affichage clair du solde
 */
@Composable
fun UnityPointsCard(
    currentPoints: Int,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    // GRADIENT VIBRANT BLEU
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4A90E2),  // Bleu clair vibrant
            Color(0xFF357ABD)   // Bleu foncé vibrant
        )
    )

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
                            text = "Unity Points",
                            fontSize = dimensions.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Bouclier d'assiduité actif",
                            fontSize = dimensions.bodySmall,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }

                    // Icône simple sans jauge
                    Surface(
                        modifier = Modifier.size(dimensions.iconSizeLarge),
                        shape = CircleShape,
                        color = overlayColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✨",
                                fontSize = dimensions.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

                // Points actuels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$currentPoints",
                        fontSize = dimensions.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    Text(
                        text = " pts",
                        fontSize = dimensions.titleLarge,
                        color = textColor,
                        modifier = Modifier.offset(y = (-6).dp, x = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                // Petit texte informatif sur l'usage des points
                Text(
                    text = "Utilise tes points pour protéger ta cagnotte.",
                    fontSize = dimensions.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(name = "Preview Points", widthDp = 360)
@Composable
fun UnityPointsCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UnityPointsCard(
                currentPoints = 1540,
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}