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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp // Gardé pour les border strokes spécifiques ou cas rares
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Association
import com.miage.learnity.ui.components.* // Import de vos composants responsives
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

@Composable
fun AssociationScreen(
    // Injection du ViewModel global
    userViewModel: UserViewModel = viewModel()
) {
    // 1. Initialisation des dimensions responsives
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    // 🎯 RÉCUPÉRATION DE LA VRAIE DETTE (Remplace ta variable supprimée)
    val uiState by userViewModel.uiState.collectAsState()
    val detteVirtuelle = uiState.profile?.detteCumulee ?: 0.0

    // ✅ ON GARDE UNIQUEMENT LES ÉTATS DE LA POPUP
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

    // Dégradés
    val backgroundGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF1A1454), Color(0xFF0F0B3A))
    )
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF2994A), Color(0xFFF2C94C))
    )

    // 2. Structure principale
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // On utilise LazyColumn pour tout l'écran pour garantir le scroll sur petit écran
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPaddingHorizontal), // Marge horizontale responsive
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            contentPadding = PaddingValues(vertical = dimensions.screenPaddingVertical)
        ) {

            // --- ITEM 1 : CARTE DETTE VIRTUELLE ---
            item {
                // On utilise une Card standard ici car on veut un background gradient spécifique
                // mais on utilise les dimensions responsives pour la forme et le padding
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardGradient)
                            .padding(dimensions.cardPadding) // Padding interne responsive
                    ) {
                        Column {
                            Text(
                                text = "Dette Virtuelle",
                                color = Color.White,
                                fontSize = dimensions.bodyMedium, // Police responsive
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                            Text(
                                text = String.format("%.2f €", detteVirtuelle),
                                color = Color.White,
                                fontSize = dimensions.displayLarge, // Très grande police responsive
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // --- ITEM 2 : TITRE SECTION ---
            item {
                Spacer(modifier = Modifier.height(dimensions.itemSpacing))
                Text(
                    text = "Nos associations partenaires",
                    color = Color.White,
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- ITEMS 3 : LISTE DES ASSOS ---
            items(associations) { asso ->
                // Ici, on suppose que AssociationCardCustom a été adaptée ou on l'enveloppe
                // Si AssociationCardCustom n'est pas responsive, vous pouvez l'envelopper dans une Box
                // Pour l'instant, on l'utilise telle quelle mais on gère l'espacement via le LazyColumn
                AssociationCardCustom(
                    asso = asso,
                    onDonClick = {
                        selectedAsso = asso
                        showDialog = true
                        montantDon = "" // Reset du champ
                        isInputError = false
                    }
                )
            }

            // Espace en bas de liste pour éviter que le contenu soit coupé par la navigation
            item {
                Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
            }
        }

        // 3. Popup de confirmation Responsive
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
                    Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                        Text(
                            text = "Montant à déduire de votre dette :",
                            fontSize = dimensions.bodyMedium
                        )

                        // Utilisation du composant ResponsiveTextField
                        ResponsiveTextField(
                            value = montantDon,
                            onValueChange = {
                                montantDon = it
                                isInputError = false
                            },
                            label = "Montant (€)",
                            isError = isInputError,
                            errorMessage = "Montant invalide ou supérieur à la dette",
                            // On force le clavier numérique
                            // Note: ResponsiveTextField doit supporter keyboardOptions, sinon ajouter le paramètre dans le composant
                            // Si votre ResponsiveTextField actuel ne l'a pas, on passe par modifier ou on l'ajoute.
                            // Pour cet exemple, je suppose une implémentation standard ou je fallback sur un TextField simple si besoin.
                            // UPDATE : Votre fichier 1 montre que ResponsiveTextField n'a pas keyboardOptions.
                            // C'est une amélioration à faire dans ResponsiveTextField, mais pour l'instant cela fonctionnera en texte.
                        )
                    }
                },
                confirmButton = {
                    ResponsiveSmallButton(
                        text = "Confirmer",
                        onClick = {
                            // On convertit le texte en nombre (Double pour correspondre au ViewModel)
                            val montant = montantDon.replace(",", ".").toDoubleOrNull() ?: 0.0

                            if (montant > 0 && montant <= detteVirtuelle) {
                                // 🎯 ACTION : On enregistre le don dans Firebase
                                userViewModel.makeDonation(montant)

                                showDialog = false

                                // Redirection externe
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

// Placeholder pour prévisualisation si AssociationCardCustom n'est pas dispo dans le contexte de copie
// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
//@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
//@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
//@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}