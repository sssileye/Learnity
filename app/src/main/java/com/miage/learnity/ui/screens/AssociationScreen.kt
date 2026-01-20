package com.miage.learnity.ui.screens

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
import com.miage.learnity.data.AssociationRepository
import com.miage.learnity.ui.components.AssociationCard

@Composable
fun AssociationScreen() {
    val repository = remember { AssociationRepository() }
    var associationsList by remember { mutableStateOf<List<Association>>(emptyList()) }

    // Chargement des données au lancement
    LaunchedEffect(Unit) {
        repository.getAssociations { list ->
            associationsList = list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
    ) {
        Text(
            text = "Nos associations partenaires",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Soutenez-les grâce à vos points d'apprentissage",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Liste dynamique
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(associationsList) { asso ->
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