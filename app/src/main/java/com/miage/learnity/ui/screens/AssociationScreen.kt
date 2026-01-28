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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1454), Color(0xFF0F0B3A))
    )
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        if (isLoading && profile == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.screenPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                contentPadding = PaddingValues(vertical = dimensions.screenPaddingVertical)
            ) {
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
                                Spacer(modifier = Modifier.height(4.dp))
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

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nos associations partenaires",
                        color = Color.White,
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // ✅ CORRECTION ICI : Ajout du paramètre 'dimensions' manquant
                items(associations) { asso ->
                    AssociationCardCustom(
                        asso = asso,
                        onDonClick = {
                            selectedAsso = asso
                            showDialog = true
                            montantDon = ""
                            isInputError = false
                        },
                        dimensions = dimensions // Indispensable pour ton composant
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.bottomNavHeight + 16.dp))
                }
            }
        }

        if (showDialog && selectedAsso != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                title = {
                    Text(
                        text = "Donner à ${selectedAsso?.name}",
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Montant à déduire de votre dette :",
                            fontSize = dimensions.bodyMedium
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
                            color = Color.Gray
                        )
                    }
                }
            )
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}