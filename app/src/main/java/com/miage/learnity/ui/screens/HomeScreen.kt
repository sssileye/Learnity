package com.miage.learnity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.miage.learnity.R
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.ui.utils.*

@Composable
fun HomeScreen(
    navController: NavController,
    isDiscoveryMode: Boolean = false
) {
    // 🎨 Dimensions responsives
    val dimensions = rememberResponsiveDimensions()

    val repository = remember { QuizRepository() }
    var dailyScore by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(Unit) {
        repository.getLastDailyQuizScore().onSuccess { score ->
            dailyScore = score
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = dimensions.screenPaddingHorizontal) // ✅ Padding responsive
            .responsiveMaxWidth(dimensions) // ✅ Limite largeur
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing) // ✅ Spacing adaptatif
    ) {
        Spacer(modifier = Modifier.height(dimensions.screenPaddingVertical))

        VirtualDebtCardResponsive(dimensions)

        DailyQuizCardResponsive(
            dimensions = dimensions,
            isDiscoveryMode = isDiscoveryMode,
            lastScore = dailyScore,
            onAction = { isReview ->
                val modeId = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
                navController.navigate("quiz/GLOBAL/$modeId?isReviewMode=$isReview")
            }
        )

        UnityPointsCardResponsive(dimensions)

        Spacer(modifier = Modifier.height(80.dp)) // Pour bottom bar
    }
}

@Composable
private fun DailyQuizCardResponsive(
    dimensions: ResponsiveDimensions,
    isDiscoveryMode: Boolean,
    lastScore: Pair<Int, Int>?,
    onAction: (isReview: Boolean) -> Unit
) {
    val hasDoneQuizToday = lastScore != null
    val scoreValue = lastScore?.first ?: 0

    val (gradient, emoji, message) = when {
        !hasDoneQuizToday -> Triple(
            Brush.linearGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))),
            "🚀", "Prêt pour ton défi ?"
        )
        scoreValue >= 9 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
            "🏆", "Score de Légende !"
        )
        scoreValue >= 7 -> Triple(
            Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF2E7D32))),
            "🥈", "Très bon niveau !"
        )
        scoreValue >= 5 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFE65100))),
            "🥉", "Pas mal, persévère !"
        )
        else -> Triple(
            Brush.linearGradient(listOf(Color(0xFFE57373), Color(0xFFC62828))),
            "📖", "Besoin de révisions..."
        )
    }

    Column {
        Card(
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge), // ✅ Border radius adaptatif
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
        ) {
            Box(
                modifier = Modifier
                    .background(gradient)
                    .padding(dimensions.cardPadding) // ✅ Padding adaptatif
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Quiz du jour $emoji",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = dimensions.titleLarge, // ✅ 28.ssp()
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (hasDoneQuizToday) {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = dimensions.bodyLarge, // ✅ 16.ssp()
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "${lastScore?.first}/${lastScore?.second}",
                            color = Color.White,
                            fontSize = dimensions.displayLarge, // ✅ 40.ssp()
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                        ) {
                            Button(
                                onClick = { onAction(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimensions.buttonHeightSmall), // ✅ 48.sdp()
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    "Revoir",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimensions.bodyMedium // ✅ 14.ssp()
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
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimensions.bodyMedium
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                        Text(
                            text = if (isDiscoveryMode)
                                "Mode : Découverte (Toutes les UE)"
                            else
                                "Mode : Révision (UE étudiées)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = dimensions.bodySmall, // ✅ 12.ssp()
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2))

                        Button(
                            onClick = { onAction(false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.buttonHeight), // ✅ 56.sdp()
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF5E35B1),
                                            shape = RoundedCornerShape(dimensions.cornerRadiusSmall)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_settings_1),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(dimensions.iconSizeSmall) // ✅ 20.sdp()
                                    )
                                }
                                Spacer(modifier = Modifier.width(dimensions.itemSpacing))
                                Text(
                                    text = "Lancer le Quiz",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = dimensions.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

        Text(
            text = "Objectif de semaine : 4 séances / 2 faites.",
            color = Color.Gray,
            fontSize = dimensions.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun VirtualDebtCardResponsive(dimensions: ResponsiveDimensions) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
    )

    Card(
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(dimensions.cardPadding)
        ) {
            Column {
                Text(
                    text = "Dette Virtuelle ce mois-ci : 😈",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyLarge
                )

                Spacer(modifier = Modifier.height(dimensions.itemSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12.50€",
                        color = Color.White,
                        fontSize = dimensions.titleLarge * 1.2f, // ✅ Taille relative
                        fontWeight = FontWeight.ExtraBold
                    )

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                    ) {
                        Text(
                            text = "Solder ma dette",
                            color = Color(0xFFFF5E62),
                            fontWeight = FontWeight.Bold,
                            fontSize = dimensions.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_1),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )

                    Slider(
                        value = 0.4f,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = dimensions.itemSpacing),
                        colors = SliderDefaults.colors(
                            disabledThumbColor = Color.White,
                            disabledActiveTrackColor = Color.White,
                            disabledInactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_1),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                }

                Text(
                    text = "Dans 4 mois",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = dimensions.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun UnityPointsCardResponsive(dimensions: ResponsiveDimensions) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF66BB6A), Color(0xFF00897B))
    )

    Card(
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(dimensions.cardPadding)
        ) {
            Column {
                Text(
                    text = "Tes Unity Points : ✨",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyLarge
                )

                Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier.size(dimensions.iconSizeLarge + 16.dp), // ✅ 64.dp
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 5.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(dimensions.itemSpacing * 2))

                    Column {
                        Text(
                            text = "1540 pts",
                            color = Color.White,
                            fontSize = dimensions.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Prochain don à 2000 pts",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = dimensions.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    text = "Voir mon impact",
                    color = Color.White,
                    fontSize = dimensions.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

