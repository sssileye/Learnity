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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.repository.QuizRepository

@Composable
fun HomeScreen(
    navController: NavController,
    isDiscoveryMode: Boolean = false
) {
    val repository = remember { QuizRepository() }
    var dailyScore by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Charger le score au lancement de l'écran
    LaunchedEffect(Unit) {
        repository.getLastDailyQuizScore().onSuccess { score ->
            dailyScore = score
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VirtualDebtCard()

        // Carte mise à jour avec navigation Revoir/Refaire et design dynamique
        DailyQuizCard(
            isDiscoveryMode = isDiscoveryMode,
            lastScore = dailyScore,
            onAction = { isReview ->
                val modeId = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
                navController.navigate("quiz/GLOBAL/$modeId?isReviewMode=$isReview")
            }
        )

        UnityPointsCard()
    }
}

@Composable
fun DailyQuizCard(
    isDiscoveryMode: Boolean,
    lastScore: Pair<Int, Int>?,
    onAction: (isReview: Boolean) -> Unit
) {
    val hasDoneQuizToday = lastScore != null
    val scoreValue = lastScore?.first ?: 0

    // ⭐ LOGIQUE DE DÉCORATION DYNAMIQUE SELON LE SCORE
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.background(gradient).padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Quiz du jour $emoji",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (hasDoneQuizToday) {
                        // --- ÉTAT : COMPLÉTÉ (Score figé + Revoir/Refaire) ---
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${lastScore?.first}/${lastScore?.second}",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bouton REVOIR
                            Button(
                                onClick = { onAction(true) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text("Revoir", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            // Bouton REFAIRE
                            Button(
                                onClick = { onAction(false) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Refaire", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // --- ÉTAT : À FAIRE ---
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isDiscoveryMode) "Mode : Découverte (Toutes les UE)" else "Mode : Révision (UE étudiées)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onAction(false) },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF5E35B1), shape = RoundedCornerShape(6.dp))
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id= R.drawable.ic_settings_1),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Lancer le Quiz",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Objectif de semaine : 4 séances / 2 faites.",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun VirtualDebtCard() {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.background(gradient).padding(20.dp)) {
            Column {
                Text(text = "Dette Virtuelle ce mois-ci : 😈", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "12.50€", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                        Text(text = "Solder ma dette", color = Color(0xFFFF5E62), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_settings_1), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Slider(value = 0.4f, onValueChange = {}, enabled = false, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), colors = SliderDefaults.colors(disabledThumbColor = Color.White, disabledActiveTrackColor = Color.White, disabledInactiveTrackColor = Color.White.copy(alpha = 0.3f)))
                    Icon(painter = painterResource(id = R.drawable.ic_settings_1), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Text(text = "Dans 4 mois", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun UnityPointsCard() {
    val gradient = Brush.linearGradient(colors = listOf(Color(0xFF66BB6A), Color(0xFF00897B)))
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
        Box(modifier = Modifier.background(gradient).fillMaxWidth().padding(20.dp)) {
            Column {
                Text(text = "Tes Unity Points : ✨", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { 0.75f }, modifier = Modifier.size(64.dp), color = Color.White, trackColor = Color.White.copy(alpha = 0.3f), strokeWidth = 5.dp)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(text = "1540 pts", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Prochain don à 2000 pts", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Voir mon impact", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    HomeScreen(navController = rememberNavController())
}