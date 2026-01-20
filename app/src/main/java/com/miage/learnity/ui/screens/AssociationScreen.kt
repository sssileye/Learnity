package com.miage.learnity.ui.screens

import android.content.Intent
import android.net.Uri // IMPORT MANQUANT POUR PARSE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.AssociationCardCustom // VÉRIFIE LE NOM ICI

@Composable
fun AssociationScreen() {
    var detteVirtuelle by remember { mutableStateOf(12.50f) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedAsso by remember { mutableStateOf<Association?>(null) }
    var montantDon by remember { mutableStateOf("") }
    val context = LocalContext.current

    val associations = listOf(
        Association("Fédération ATENA - Comptoir Aliénor", "https://www.helloasso.com/associations/federation-atena/formulaires/1", "logo_atena", "La Fédération ATENA regroupe les associations étudiantes de l'Université de Bordeaux. Son Comptoir Aliénor est une épicerie solidaire proposant des produits de première nécessité à prix réduits pour soutenir le pouvoir d'achat des étudiants."),
        Association("Les Restos du Coeur", "https://www.restosducoeur.org/faire-un-don-financier/", "logo_restosducoeur", "Association reconnue d'utilité publique qui lutte contre la précarité et l'exclusion. Elle assure une aide alimentaire gratuite aux personnes démunies et les accompagne dans leur insertion sociale et professionnelle à travers de multiples ateliers."),
        Association("M-Tech", "https://www.helloasso.com/associations/association-m-tech/formulaires/1?utm_source=ig&utm_medium=social&utm_content=link_in_bio&fbclid=PAb21jcAPbGC5leHRuA2FlbQIxMQBzcnRjBmFwcF9pZA81NjcwNjczNDMzNTI0MjcAAaePqfQs8u6NEBF8i71_6vElLGllv5WdDf3VX5n1p_JSTYOp2DxPchxbvAY0qg_aem_3MjQEee0F6YKVDMnxz16lg", "logo_mtech", "Association technologique étudiante axée sur l'innovation et l'ingénierie. Elle permet aux étudiants de développer des projets techniques concrets, de partager des compétences en robotique ou informatique, et de créer un lien entre le monde académique et professionnel."),
        Association("Linkee", "https://www.helloasso.com/associations/linkee-bordeaux/formulaires/3", "logo_linkee", "Solution logistique et solidaire de lutte contre le gaspillage alimentaire. Linkee récupère les invendus auprès des commerçants et entreprises pour les redistribuer gratuitement aux étudiants et aux personnes en situation de précarité.")
    )

    // Définition du dégradé horizontal
    val backgroundGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1A1454), // Bleu plus clair à gauche
            Color(0xFF0F0B3A)  // Bleu plus foncé à droite
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        //Text("Mon Association", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        //Spacer(modifier = Modifier.height(20.dp))

        // Box Dette Virtuelle avec Dégradé
        Card(modifier = Modifier
            .fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFF2994A), Color(0xFFF2C94C)))).padding(20.dp)) {
                Column {
                    Text("Dette Virtuelle", color = Color.White, fontSize = 14.sp)
                    Text(text = String.format("%.2f €", detteVirtuelle), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Nos associations partenaires", color = Color.White, fontWeight = FontWeight.SemiBold)

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(associations) { asso ->
                AssociationCardCustom(
                    asso = asso,
                    onDonClick = {
                        selectedAsso = asso
                        showDialog = true
                    }
                )
            }
        }

        // Popup de confirmation
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Donner à ${selectedAsso?.name}") },
                text = {
                    Column {
                        Text("Montant à déduire de votre dette :")
                        TextField(
                            value = montantDon,
                            onValueChange = { montantDon = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val montant = montantDon.toFloatOrNull() ?: 0f
                        if (montant > 0 && montant <= detteVirtuelle) {
                            detteVirtuelle -= montant
                            showDialog = false
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedAsso?.websiteUrl))
                            context.startActivity(intent)
                        }
                    }) { Text("Confirmer") }
                }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}