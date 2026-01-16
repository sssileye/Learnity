package com.miage.learnity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.R // Important pour accéder à tes images drawable
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.AssociationCard

@Composable
fun AssociationScreen() {
    // Liste fictive : Remplace "logo_amiage" par tes vrais noms de fichiers dans drawable
    val associations = listOf(
        Association("AMIAGE Bordeaux", "https://amiage.fr", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("MIAGE Connexion", "https://miage-connexion.fr", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena),
        Association("Restos du Cœur", "https://www.restosducoeur.org", R.drawable.assoatena)

    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)) // Gris clair de ton interface
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nos associations partenaires",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Liste défilante performante
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(associations) { asso ->
                AssociationCard(asso = asso)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}