package com.miage.learnity.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizScreen() {
    var selectedOption by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ZONE BLEUE (Question)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(24.dp)
            ) {
                Text("Extraction de Base de Données", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("Chapitre : Clustering", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Quelle est la principale limite de l'algorithme de clustering K-means quand les groupes ont des tailles ou densités différentes ?",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LES 4 RÉPONSES (Grille 2x2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuizOptionCard("Sensibilité aux valeurs aberrantes", 1, selectedOption) { selectedOption = 1 }
                QuizOptionCard("Difficulté à déterminer k optimal", 2, selectedOption) { selectedOption = 2 }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuizOptionCard("Convergence vers un minimum local", 3, selectedOption) { selectedOption = 3 }
                QuizOptionCard("Mauvaise détection de clusters non sphériques", 4, selectedOption) { selectedOption = 4 }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ZONE VERTE (Correction)
        if (selectedOption != 0) {
            val isCorrect = selectedOption == 4
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isCorrect) "Bonne réponse ! ✨" else "Mauvaise réponse... ❌",
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "K-means privilégie les formes sphériques. Pour des densités variées, DBSCAN est plus adapté.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun QuizOptionCard(text: String, id: Int, selectedId: Int, onClick: () -> Unit) {
    val isSelected = id == selectedId

    Box(modifier = Modifier.width(165.dp).height(110.dp)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = if (isSelected) BorderStroke(2.dp, Color(0xFF3F51B5)) else null,
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                Text(text, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
            }
        }

        // Badge avec numéro
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp),
            shape = CircleShape,
            color = Color(0xFF3F51B5)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = id.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizPagePreview() {
    QuizScreen()
}