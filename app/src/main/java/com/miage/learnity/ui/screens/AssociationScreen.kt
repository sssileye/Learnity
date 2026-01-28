package com.miage.learnity.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.rememberResponsiveDimensions
import com.miage.learnity.ui.utils.responsiveMaxWidth

@Composable
fun AssociationScreen() {
    // 1. Initialisation des dimensions responsives
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    // États
    var detteVirtuelle by remember { mutableFloatStateOf(12.50f) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedAsso by remember { mutableStateOf<Association?>(null) }
    var montantDon by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    // Données
    val associations = remember {
        listOf(
            Association("Fédération ATENA - Comptoir Aliénor", "https://www.helloasso.com/associations/federation-atena/formulaires/1", "logo_atena", "La Fédération ATENA regroupe les associations étudiantes de l'Université de Bordeaux. Son Comptoir Aliénor est une épicerie solidaire."),
            Association("Les Restos du Coeur", "https://www.restosducoeur.org/faire-un-don-financier/", "logo_restosducoeur", "Association reconnue d'utilité publique qui lutte contre la précarité et l'exclusion."),
            Association("M-Tech", "https://www.helloasso.com/associations/association-m-tech/formulaires/1", "logo_mtech", "Association technologique étudiante axée sur l'innovation et l'ingénierie."),
            Association("Linkee", "https://www.helloasso.com/associations/linkee-bordeaux/formulaires/3", "logo_linkee", "Solution logistique et solidaire de lutte contre le gaspillage alimentaire.")
        )
    }

    // ✅ Dégradé pour la carte dette (accent coloré conservé)
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C))
    )

    // 2. Structure principale - ✅ DARK MODE
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPaddingHorizontal)
                .responsiveMaxWidth(dimensions),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            contentPadding = PaddingValues(vertical = dimensions.screenPaddingVertical)
        ) {

            // --- ITEM 1 : CARTE DETTE VIRTUELLE ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardGradient)
                            .padding(dimensions.cardPadding)
                    ) {
                        Column {
                            Text(
                                text = "Dette Virtuelle",
                                color = Color.White,
                                fontSize = dimensions.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                            Text(
                                text = String.format("%.2f €", detteVirtuelle),
                                color = Color.White,
                                fontSize = dimensions.displayLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // --- ITEM 2 : TITRE SECTION - ✅ DARK MODE ---
            item {
                Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                Text(
                    text = "Nos associations partenaires",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- ITEMS 3 : LISTE DES ASSOS ---
            items(associations) { asso ->
                AssociationCardCustom(
                    asso = asso,
                    onDonClick = {
                        selectedAsso = asso
                        showDialog = true
                        montantDon = ""
                        isInputError = false
                    },
                    dimensions = dimensions
                )
            }

            // Espace en bas
            item {
                Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
            }
        }

        // 3. Popup de confirmation - ✅ DARK MODE
        if (showDialog && selectedAsso != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                title = {
                    Text(
                        text = "Donner à ${selectedAsso?.name}",
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                        Text(
                            text = "Montant à déduire de votre dette :",
                            fontSize = dimensions.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ResponsiveTextField(
                            value = montantDon,
                            onValueChange = {
                                montantDon = it
                                isInputError = false
                            },
                            label = "Montant (€)",
                            isError = isInputError,
                            errorMessage = "Montant invalide ou supérieur à la dette"
                        )
                    }
                },
                confirmButton = {
                    ResponsiveSmallButton(
                        text = "Confirmer",
                        onClick = {
                            val montant = montantDon.replace(",", ".").toFloatOrNull() ?: 0f
                            if (montant > 0 && montant <= detteVirtuelle) {
                                detteVirtuelle -= montant
                                showDialog = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedAsso?.websiteUrl))
                                context.startActivity(intent)
                            } else {
                                isInputError = true
                            }
                        }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(
                            text = "Annuler",
                            fontSize = dimensions.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun AssociationScreenPreview() {
    LearnityTheme {
        AssociationScreen()
    }
}