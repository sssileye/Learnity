package com.miage.learnity.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R

@Composable
fun ProfileEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.profile?.let { currentProfile ->
        ProfileEditor(
            profile = currentProfile,
            isLoading = uiState.isLoading,
            availableAvatars = viewModel.availableAvatars,
            onCancel = onNavigateBack,
            onSave = { firstName, lastName, photoResId, redevance ->
                // Conversion de l'ID de ressource en String ("avatar_b1") pour Firestore
                val photoName = viewModel.getResourceName(photoResId, context)

                viewModel.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    photoResName = photoName,
                    redevance = redevance
                )
                onNavigateBack()
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ProfileEditor(
    profile: com.miage.learnity.data.UserProfile,
    availableAvatars: List<Int>,
    onSave: (String, String, Int, Double) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current

    // États pour les champs
    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var redevance by remember { mutableStateOf(profile.redevanceSoutienUnitaire.toString()) }

    // État pour l'avatar sélectionné (on cherche l'ID correspondant au nom stocké)
    var selectedAvatarResId by remember {
        val currentId = context.resources.getIdentifier(profile.photoUrl, "drawable", context.packageName)
        mutableStateOf(if (currentId != 0) currentId else R.drawable.avatar_b1)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(60.dp))

        Text(
            text = "Modifier mon profil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        )
        Text(
            text = "Choisissez votre avatar et modifiez vos infos",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )

        Spacer(Modifier.height(30.dp))

        // --- GRILLE DE SÉLECTION D'AVATARS ---
        Text(
            text = "Sélectionnez un avatar",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // On utilise une Box avec une hauteur fixe pour la grille au milieu du scroll
        Box(modifier = Modifier.height(280.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 4 colonnes pour tes 13 avatars
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableAvatars) { avatarId ->
                    val isSelected = selectedAvatarResId == avatarId

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF673AB7).copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color(0xFF673AB7) else Color(0xFFE0E0E0),
                                shape = CircleShape
                            )
                            .clickable { selectedAvatarResId = avatarId }
                    ) {
                        Image(
                            painter = painterResource(id = avatarId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        // --- CHAMPS DE SAISIE ---
        CustomEditField(
            value = firstName,
            onValueChange = { firstName = it },
            label = "Prénom",
            icon = Icons.Default.Person,
            errorText = if (firstName.isBlank()) "Prénom requis" else ""
        )

        CustomEditField(
            value = lastName,
            onValueChange = { lastName = it },
            label = "Nom",
            icon = Icons.Default.Person,
            errorText = if (lastName.isBlank()) "Nom requis" else ""
        )

        CustomEditField(
            value = redevance,
            onValueChange = { redevance = it },
            label = "Redevance unitaire (€)",
            icon = Icons.Default.ShoppingCart,
            errorText = if (redevance.toDoubleOrNull() == null) "Montant invalide" else "",
            keyboardType = KeyboardType.Decimal
        )

        Spacer(Modifier.height(40.dp))

        // --- BOUTONS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text("Annuler", color = Color.Black)
            }

            Button(
                onClick = {
                    val redValue = redevance.toDoubleOrNull() ?: 0.0
                    onSave(firstName, lastName, selectedAvatarResId, redValue)
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank() && redevance.toDoubleOrNull() != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Enregistrer", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun CustomEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorText: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF673AB7)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = errorText.isNotEmpty(),
            supportingText = { if (errorText.isNotEmpty()) Text(errorText, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF673AB7),
                focusedLabelColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}