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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * ═══════════════════════════════════════════════════════════════
 * 🎗️ ASSOCIATION SCREEN - VERSION DARK MODE COMPATIBLE
 * ═══════════════════════════════════════════════════════════════
 *
 * Écran des associations partenaires avec :
 * ✅ Couleurs uniformes avec le reste de l'app (light/dark)
 * ✅ Utilise le thème Material Design 3
 * ✅ Fond cohérent avec HomeScreen
 */
@Composable
fun AssociationScreen(
    userViewModel: UserViewModel = viewModel()
) {
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    val uiState by userViewModel.uiState.collectAsState()
    val profile = uiState.profile
    val detteVirtuelle = profile?.detteCumulee ?: 0.0
    val isLoading = uiState.isLoading

    var showDialog by remember { mutableStateOf(false) }
    var selectedAsso by remember { mutableStateOf<Association?>(null) }
    var montantDon by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    val associations = remember {
        listOf(
            Association("Fédération ATENA - Comptoir Aliénor", "https://www.helloasso.com/associations/federation-atena/formulaires/1", "logo_atena", "La Fédération ATENA regroupe les associations étudiantes de l'Université de Bordeaux. Son Comptoir Aliénor est une épicerie solidaire."),
            Association("Les Restos du Coeur", "https://www.restosducoeur.org/faire-un-don-financier/", "logo_restosducoeur", "Association reconnue d'utilité publique qui lutte contre la précarité et l'exclusion."),
            Association("M-Tech", "https://www.helloasso.com/associations/association-m-tech/formulaires/1", "logo_mtech", "Association technologique étudiante axée sur l'innovation et l'ingénierie."),
            Association("Linkee", "https://www.helloasso.com/associations/linkee-bordeaux/formulaires/3", "logo_linkee", "Solution logistique et solidaire de lutte contre le gaspillage alimentaire.")
        )
    }

    // ✅ Utilise le gradient de dette adaptatif au lieu du gradient violet hardcodé
    val backgroundGradient = debtGradient()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // ✅ ADAPTATIF comme HomeScreen
    ) {
        if (isLoading && profile == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary  // ✅ ADAPTATIF
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.screenPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                contentPadding = PaddingValues(vertical = dimensions.screenPaddingVertical)
            ) {
                // ✅ Carte de dette avec gradient adaptatif
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation * 1.5f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundGradient)  // ✅ Utilise debtGradient()
                                .padding(dimensions.cardPadding)
                        ) {
                            Column {
                                Text(
                                    text = "Dette Virtuelle",
                                    color = getOnGradientTextColor(),  // ✅ ADAPTATIF
                                    fontSize = dimensions.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.2f €", detteVirtuelle),
                                    color = getOnGradientTextColor(),  // ✅ ADAPTATIF
                                    fontSize = dimensions.displayLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // ✅ Titre avec couleur adaptative
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nos associations partenaires",
                        color = MaterialTheme.colorScheme.onBackground,  // ✅ ADAPTATIF
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Liste des associations
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

                item {
                    Spacer(modifier = Modifier.height(dimensions.bottomNavHeight + 16.dp))
                }
            }
        }

        // ✅ Dialog avec couleurs adaptatives
        if (showDialog && selectedAsso != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,  // ✅ ADAPTATIF
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                title = {
                    Text(
                        text = "Donner à ${selectedAsso?.name}",
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface  // ✅ ADAPTATIF
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Montant à déduire de votre dette :",
                            fontSize = dimensions.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface  // ✅ ADAPTATIF
                        )

                        ResponsiveTextField(
                            value = montantDon,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                                    montantDon = it
                                    isInputError = false
                                }
                            },
                            label = "Montant (€)",
                            isError = isInputError,
                            errorMessage = "Max possible : ${String.format("%.2f", detteVirtuelle)} €"
                        )
                    }
                },
                confirmButton = {
                    ResponsiveSmallButton(
                        text = "Confirmer",
                        onClick = {
                            val montant = montantDon.replace(",", ".").toDoubleOrNull() ?: 0.0

                            if (montant > 0 && montant <= (detteVirtuelle + 0.001)) {
                                userViewModel.makeDonation(montant)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant  // ✅ ADAPTATIF
                        )
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 PREVIEWS
// ═══════════════════════════════════════════════════════════════

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    MaterialTheme {
        AssociationScreen()
    }
}