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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R
import com.miage.learnity.ui.utils.*

@Composable
fun ProfileEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.profile?.let { currentProfile ->
        ProfileEditor(
            profile = currentProfile,
            isLoading = uiState.isLoading,
            availableAvatars = viewModel.availableAvatars,
            onCancel = onNavigateBack,
            onSave = { firstName, lastName, photoResId, redevance ->
                val photoName = viewModel.getResourceName(photoResId, context)
                viewModel.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    photoResName = photoName,
                    redevance = redevance
                )
                onNavigateBack()
            },
            viewModel = viewModel,
            dimensions = dimensions
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
    viewModel: ProfileViewModel,
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current

    // États pour les champs
    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var redevance by remember { mutableStateOf(profile.redevanceSoutienUnitaire.toString()) }

    // État pour l'avatar sélectionné
    var selectedAvatarResId by remember {
        val currentId = context.resources.getIdentifier(
            profile.photoUrl,
            "drawable",
            context.packageName
        )
        mutableStateOf(if (currentId != 0) currentId else R.drawable.avatar_b1)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = dimensions.screenPaddingHorizontal)
    ) {
        Spacer(Modifier.height(dimensions.profilePictureSize * 0.63f))  // ✅ 60.sdp()

        // ✅ TITRE - RESPONSIVE
        Text(
            text = "Modifier mon profil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                fontSize = dimensions.titleLarge * 0.86f  // ✅ 24.ssp()
            )
        )
        Text(
            text = "Choisissez votre avatar et modifiez vos infos",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                fontSize = dimensions.bodyMedium
            )
        )

        Spacer(Modifier.height(dimensions.itemSpacing * 2.5f))  // ✅ 30.sdp()

        // ✅ TITRE SECTION AVATARS - RESPONSIVE
        Text(
            text = "Sélectionnez un avatar",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = dimensions.bodyLarge
            ),
            modifier = Modifier.padding(bottom = dimensions.itemSpacing)
        )

        // ✅ GRILLE D'AVATARS - RESPONSIVE
        Box(
            modifier = Modifier.height(dimensions.profilePictureSize * 2.92f)  // ✅ 280.sdp()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableAvatars) { avatarId ->
                    val isSelected = selectedAvatarResId == avatarId

                    Box(
                        modifier = Modifier
                            .size(dimensions.iconSizeLarge * 1.46f)  // ✅ 70.sdp()
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    Color(0xFF673AB7).copy(alpha = 0.1f)
                                else
                                    Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected)
                                    Color(0xFF673AB7)
                                else
                                    Color(0xFFE0E0E0),
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

        Spacer(Modifier.height(dimensions.itemSpacing * 2.5f))  // ✅ 30.sdp()

        // ✅ CHAMPS DE SAISIE - RESPONSIVE
        CustomEditField(
            value = firstName,
            onValueChange = { firstName = it },
            label = "Prénom",
            icon = Icons.Default.Person,
            errorText = if (firstName.isBlank()) "Prénom requis" else "",
            dimensions = dimensions
        )

        CustomEditField(
            value = lastName,
            onValueChange = { lastName = it },
            label = "Nom",
            icon = Icons.Default.Person,
            errorText = if (lastName.isBlank()) "Nom requis" else "",
            dimensions = dimensions
        )

        CustomEditField(
            value = redevance,
            onValueChange = { redevance = it },
            label = "Redevance unitaire (€)",
            icon = Icons.Default.ShoppingCart,
            errorText = if (redevance.toDoubleOrNull() == null) "Montant invalide" else "",
            keyboardType = KeyboardType.Decimal,
            dimensions = dimensions
        )

        Spacer(Modifier.height(dimensions.itemSpacing * 3.33f))  // ✅ 40.sdp()

        // ✅ BOUTONS - RESPONSIVE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing * 1.33f)  // ✅ 16.sdp()
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text(
                    "Annuler",
                    color = Color.Black,
                    fontSize = dimensions.bodyLarge
                )
            }

            Button(
                onClick = {
                    val redValue = redevance.toDoubleOrNull() ?: 0.0
                    onSave(firstName, lastName, selectedAvatarResId, redValue)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                enabled = !isLoading &&
                        firstName.isNotBlank() &&
                        lastName.isNotBlank() &&
                        redevance.toDoubleOrNull() != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensions.iconSizeMedium),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Enregistrer",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(dimensions.screenPaddingVertical * 2))  // ✅ 50.sdp()
    }
}

@Composable
fun CustomEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier.padding(bottom = dimensions.itemSpacing * 1.67f)  // ✅ 20.sdp()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = dimensions.bodyMedium) },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF673AB7),
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            isError = errorText.isNotEmpty(),
            supportingText = {
                if (errorText.isNotEmpty()) {
                    Text(
                        errorText,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = dimensions.bodySmall
                    )
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = dimensions.bodyLarge
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF673AB7),
                focusedLabelColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun ProfileEditorPreview() {
    MaterialTheme {
        val dimensions = rememberResponsiveDimensions()
        ProfileEditor(
            profile = com.miage.learnity.data.UserProfile(
                firstName = "Axel",
                lastName = "H",
                email = "axel@learnity.fr",
                photoUrl = "avatar_b1",
                redevanceSoutienUnitaire = 1.0
            ),
            availableAvatars = listOf(
                R.drawable.avatar_b1, R.drawable.avatar_b2, R.drawable.avatar_b3,
                R.drawable.avatar_o1, R.drawable.avatar_o2, R.drawable.avatar_o3,
                R.drawable.avatar_v1, R.drawable.avatar_v2, R.drawable.avatar_v3,
                R.drawable.avatar_r1, R.drawable.avatar_r2, R.drawable.avatar_r3,
                R.drawable.avatar_vivi1
            ),
            onSave = { _, _, _, _ -> },
            onCancel = {},
            isLoading = false,
            viewModel = viewModel(),
            dimensions = dimensions
        )
    }
}