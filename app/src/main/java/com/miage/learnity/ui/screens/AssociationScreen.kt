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


    val allAssociations = remember {
        listOf(
            // FRANCE
            Association("Fédération ATENA", "https://www.helloasso.com/associations/federation-atena/formulaires/1", "logo_atena", "Épicerie solidaire pour étudiants à Bordeaux."),
            Association("Les Restos du Cœur", "https://www.restosducoeur.org/faire-un-don-financier/", "logo_restosducoeur", "Aide alimentaire et accompagnement social en France."),
            Association("Secours Populaire", "https://www.secourspopulaire.fr/don", "logo_secours_populaire_francais", "Lutte contre la pauvreté et l'exclusion."),
            Association("Linkee", "https://www.helloasso.com/associations/linkee-bordeaux/formulaires/3", "logo_linkee", "Anti-gaspillage alimentaire, redistribution étudiants."),
            Association("M-Tech", "https://www.helloasso.com/associations/association-m-tech/formulaires/1", "logo_mtech", "Association technologique étudiante innovante."),

            // SÉNÉGAL
            Association("ENDA Pronat", "https://endapronat.org/", "logo_enda_senegal", "Agriculture durable et sécurité alimentaire au Sénégal."),
            Association("SOS Villages Sénégal", "https://www.sosve.org/senegal", "logo_sosenfantvillage_senegal", "Protection et éducation des enfants vulnérables."),
            Association("APAF Sénégal", "https://www.apaf-afrique.org/", "logo_apafsenegal", "Formation agricole pour jeunes sénégalais."),

            // MALI
            Association("UNICEF Mali", "https://www.unicef.org/mali/", "logo_unicefmali", "Protection et éducation des enfants maliens."),
            Association("Croix-Rouge Mali", "https://www.ifrc.org/our-network/national-societies/mali", "logo_croixrougemali", "Secours d'urgence et aide humanitaire au Mali."),
            Association("MSF Mali", "https://www.msf.org/mali", "logo_medecinsansfrontiere", "Soins médicaux d'urgence dans les zones de conflit."),

            // RDC
            Association("Caritas Congo", "https://www.caritas.org/where-caritas-work/africa/democratic-republic-of-congo/", "logo_caritascongo", "Aide humanitaire et développement communautaire en RDC."),
            Association("World Vision RDC", "https://www.worldvision.org/our-work/countries/democratic-republic-of-congo", "logo_worldvisoncongo", "Parrainage d'enfants et développement en RDC."),
            Association("PAM RDC", "https://www.wfp.org/countries/democratic-republic-congo", "logo_pamcongo", "Aide alimentaire d'urgence et lutte contre la faim."),

            // MARTINIQUE
            Association("Secours Populaire 972", "https://www.secourspopulaire.fr/", "logo_secourspopulairemartinique", "Aide alimentaire et lutte contre l'exclusion en Martinique."),
            Association("Banque Alimentaire 972", "https://www.banquealimentaire.org/", "logo_banquealimentairemartinique", "Collecte et redistribution de denrées alimentaires."),
            Association("Secours Catholique 972", "https://www.secours-catholique.org/", "logo_secourscatholiquemartinique", "Solidarité et accompagnement des personnes en précarité."),

            // MAROC
            Association("AMADE Maroc", "https://www.amade.ma/", "logo_amadmaroc", "Protection et éducation des enfants défavorisés au Maroc."),
            Association("Fondation Mohammed V", "https://fm5.ma/", "logo_fondationmaroc", "Solidarité sociale et aide aux démunis au Maroc.")
        )
    }


    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
            MaterialTheme.colorScheme.background
        )
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
                color = MaterialTheme.colorScheme.primary
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
                    Text(
                        text = "Nos associations partenaires",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }


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


                items(allAssociations) { asso ->
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Montant à déduire de votre dette :",
                            fontSize = dimensions.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AssociationScreenPreview() {
    MaterialTheme {
        AssociationScreen()
    }
}
