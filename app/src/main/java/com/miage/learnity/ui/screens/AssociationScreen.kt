package com.miage.learnity.ui.screens

import AssociationRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.data.Association
//import com.miage.learnity.data.AssociationRepository
import com.miage.learnity.ui.components.AssociationCard

@Composable
fun AssociationScreen() {
    // 1. Initialisation du Repository (pour parler à Firebase)
    val repository = remember { AssociationRepository() }

    // 2. État de la liste (vide au départ, se remplit après l'appel Firebase)
    var associationsList by remember { mutableStateOf<List<Association>>(emptyList()) }

    // 3. Appel à Firebase dès l'ouverture de l'écran
    LaunchedEffect(Unit) {
        repository.getAssociations { fetchedList ->
            associationsList = fetchedList
        }
    }

    // 4. Interface utilisateur
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)) // Fond gris clair MIAGE
            .padding(16.dp)
    ) {
        Text(
            text = "Nos associations partenaires",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Soutenez la vie étudiante bordelaise avec vos points",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 5. Liste défilante des associations
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(associationsList) { asso ->
                // Chaque association affiche son propre logo via la logique interne de AssociationCard
                AssociationCard(asso = asso)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}