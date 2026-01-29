package com.miage.learnity.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // ⭐ RÉCUPÉRATION DYNAMIQUE (Plus de liste hardcodée)
    val firebaseAssociations by userViewModel.associations.collectAsState()

    val profile = uiState.profile
    val detteVirtuelle = profile?.detteCumulee ?: 0.0
    val isLoading = uiState.isLoading

    var showDialog by remember { mutableStateOf(false) }
    var selectedAsso by remember { mutableStateOf<Association?>(null) }
    var montantDon by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }
    var filterCountry by remember { mutableStateOf<String?>(null) }

    // ⭐ FILTRAGE DE LA LISTE DYNAMIQUE
    val displayedAssociations = remember(firebaseAssociations, filterCountry) {
        if (filterCountry != null) {
            firebaseAssociations.filter { it.country == filterCountry }
        } else {
            firebaseAssociations
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // État de chargement initial (si la liste est vide au début)
        if (firebaseAssociations.isEmpty() && isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = dimensions.screenPaddingHorizontal, vertical = dimensions.screenPaddingVertical),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                // En-tête
                item {
                    Text(
                        text = "Mes Associations",
                        fontSize = dimensions.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Dette Virtuelle
                item {
                    CompactDetteCard(montant = detteVirtuelle, dimensions = dimensions)
                }

                // Barre d'outils (Filtre + Compteur)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountryFilterDropdown(
                            selectedCountry = filterCountry,
                            onCountrySelected = { filterCountry = it },
                            dimensions = dimensions
                        )

                        Text(
                            text = "${displayedAssociations.size} trouvée(s)",
                            fontSize = dimensions.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Liste des associations
                if (displayedAssociations.isEmpty() && firebaseAssociations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Aucune association pour ce filtre.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = dimensions.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(displayedAssociations) { asso ->
                        ModernAssociationCard(
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
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.bottomNavHeight + 16.dp))
                }
            }
        }

        // Dialog de don
        if (showDialog && selectedAsso != null) {
            DonationDialog(
                association = selectedAsso!!,
                montantDon = montantDon,
                onMontantChange = { montantDon = it; isInputError = false },
                detteActuelle = detteVirtuelle,
                isInputError = isInputError,
                onDismiss = { showDialog = false },
                onConfirm = {
                    val montant = montantDon.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (montant > 0 && montant <= (detteVirtuelle + 0.001)) {
                        userViewModel.makeDonation(montant)
                        showDialog = false
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedAsso?.websiteUrl))
                        context.startActivity(intent)
                    } else {
                        isInputError = true
                    }
                },
                dimensions = dimensions
            )
        }
    }
}

// ========================================
// COMPOSANTS (Filtre, Carte, Dialog)
// ========================================

@Composable
private fun CountryFilterDropdown(
    selectedCountry: String?,
    onCountrySelected: (String?) -> Unit,
    dimensions: com.miage.learnity.ui.utils.ResponsiveDimensions
) {
    var expanded by remember { mutableStateOf(false) }
    val countries = listOf("Tous les pays", "France", "Sénégal", "Mali", "RDC", "Martinique", "Maroc")

    Box {
        Surface(
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FilterList, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selectedCountry ?: "Tous les pays",
                    color = Color.White,
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country, fontSize = dimensions.bodySmall) },
                    onClick = {
                        onCountrySelected(if (country == "Tous les pays") null else country)
                        expanded = false
                    },
                    leadingIcon = {
                        if ((country == "Tous les pays" && selectedCountry == null) || country == selectedCountry) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF6366F1))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernAssociationCard(
    asso: Association,
    onDonClick: () -> Unit,
    dimensions: com.miage.learnity.ui.utils.ResponsiveDimensions
) {
    val context = LocalContext.current

    // ⭐ Résolution dynamique de l'image (logo_name -> drawable id)
    val imageResId = remember(asso.logoName) {
        try {
            val id = context.resources.getIdentifier(asso.logoName, "drawable", context.packageName)
            if (id != 0) id else android.R.drawable.ic_menu_gallery
        } catch (e: Exception) {
            android.R.drawable.ic_menu_gallery
        }
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(dimensions.cardPadding)) {
            // Header (Logo + Nom + Flèche)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = asso.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }

            // Contenu déplié
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.padding(start = 54.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = asso.country, fontSize = dimensions.bodySmall, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = asso.description, fontSize = dimensions.bodySmall, lineHeight = 18.sp, color = Color(0xFF4B5563))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDonClick,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Favorite, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Faire un don", fontSize = dimensions.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDetteCard(montant: Double, dimensions: com.miage.learnity.ui.utils.ResponsiveDimensions) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cornerRadiusLarge), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFAB40)))).padding(dimensions.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dette Virtuelle", color = Color.White.copy(alpha = 0.9f), fontSize = dimensions.bodyMedium, fontWeight = FontWeight.Medium)
                Text(String.format("%.2f €", montant), color = Color.White, fontSize = dimensions.displayLarge, fontWeight = FontWeight.ExtraBold)
            }
            Text(text = "💸", fontSize = 36.sp)
        }
    }
}

@Composable
private fun DonationDialog(
    association: Association,
    montantDon: String,
    onMontantChange: (String) -> Unit,
    detteActuelle: Double,
    isInputError: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dimensions: com.miage.learnity.ui.utils.ResponsiveDimensions
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        title = {
            Column {
                Text(association.name, fontSize = dimensions.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Text(association.country, fontSize = dimensions.bodySmall, color = Color(0xFF6B7280))
            }
        },
        text = {
            Column {
                Text("Montant du don :", fontSize = dimensions.bodyMedium, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = montantDon,
                    onValueChange = onMontantChange,
                    placeholder = { Text("0.00") },
                    suffix = { Text("€") },
                    isError = isInputError,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                if (isInputError) {
                    Text("Max : ${String.format("%.2f €", detteActuelle)}", fontSize = dimensions.bodySmall, color = Color.Red)
                } else {
                    Text("Dette : ${String.format("%.2f €", detteActuelle)}", fontSize = dimensions.bodySmall, color = Color(0xFF9CA3AF))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))) { Text("Confirmer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = Color(0xFF6B7280)) }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}