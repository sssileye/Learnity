package com.miage.learnity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.ui.navigation.MainScreen
import com.miage.learnity.ui.theme.LearnityTheme

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)) // Fond gris clair
            //.padding(paddingValues) // IMPORTANT : Respecte le padding du Scaffold
            .padding(16.dp) // Marge interne supplémentaire
            .verticalScroll(rememberScrollState()), // Rend l'écran scrollable
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VirtualDebtCard()
        DailyQuizCard()
        UnityPointsCard()

    }
}
@Composable
fun VirtualDebtCard() {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF9966), Color(0xFFFF5E62)) // Orange -> Rouge
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.background(gradient).padding(20.dp)) {
            Column {
                // Titre
                Text(
                    text = "Dette Virtuelle ce mois-ci : 😈",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Prix et Bouton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12.50€",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Button(
                        onClick = { /* Action */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Solder ma dette",
                            color = Color(0xFFFF5E62),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barre de progression (Slider customisé)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_1),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Slider(
                        value = 0.4f, // Valeur de l'avancement
                        onValueChange = {},
                        enabled = false, // Désactivé pour l'aspect visuel seulement
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            disabledThumbColor = Color.White,
                            disabledActiveTrackColor = Color.White,
                            disabledInactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_1), // Icône valise
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Texte en bas
                Text(
                    text = "Dans 4 mois",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// COMPOSABLE 2 : QUIZ DU JOUR (Bleu/Violet)
// ---------------------------------------------------------
@Composable
fun DailyQuizCard() {
    Column {
        val gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)) // Bleu -> Violet
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.background(gradient).padding(20.dp)) {
                Column {
                    Text(
                        text = "Ton Quiz du Jour : Prêt(e)\nà coder ?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Module : Architecture Logicielle",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Gros bouton translucide
                    Button(
                        onClick = { /* Lancer */ },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF311B92).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, shape = RoundedCornerShape(6.dp))
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id= R.drawable.ic_settings_1),
                                    contentDescription = null,
                                    tint = Color(0xFF5E35B1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Lancer le Quiz",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        // Texte hors de la carte
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
// ---------------------------------------------------------
// COMPOSABLE 3 : UNITY POINTS (Vert)
// ---------------------------------------------------------
@Composable
fun UnityPointsCard() {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF66BB6A), Color(0xFF00897B))
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(), // Vérifie que c'est bien présent
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        // AJOUT : .fillMaxWidth() sur le Box pour que le vert occupe tout l'espace
        Box(modifier = Modifier
            .background(gradient)
            .fillMaxWidth() // <--- À AJOUTER ABSOLUMENT
            .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Tes Unity Points : ✨",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // AJOUT : .fillMaxWidth() sur la Row pour bien répartir le contenu
                Row(
                    modifier = Modifier.fillMaxWidth(), // <--- À AJOUTER
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier.size(64.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 5.dp,
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "1540 pts",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Prochain don à 2000 pts",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Voir mon impact",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
// --- SECTION PREVIEW ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    HomeScreen()
}