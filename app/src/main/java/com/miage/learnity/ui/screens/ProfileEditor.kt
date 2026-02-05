package com.miage.learnity.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.launch

@Composable
fun ProfileEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
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
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var redevance by remember { mutableStateOf(profile.redevanceSoutienUnitaire.toString()) }

    val initialAvatarResId = remember {
        val id = context.resources.getIdentifier(profile.photoUrl, "drawable", context.packageName)
        if (id != 0) id else R.drawable.avatar_b1
    }
    var selectedAvatarResId by remember { mutableStateOf(initialAvatarResId) }

    val pagerState = rememberPagerState(
        initialPage = availableAvatars.indexOf(selectedAvatarResId).coerceAtLeast(0),
        pageCount = { availableAvatars.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        selectedAvatarResId = availableAvatars[pagerState.currentPage]
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = dimensions.screenPaddingHorizontal)
    ) {
        Spacer(Modifier.height(dimensions.screenPaddingVertical * 2))

        Text(
            text = "Modifier mon profil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = dimensions.titleLarge * 0.86f
            )
        )
        Text(
            text = "Choisissez votre avatar",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = dimensions.bodyMedium
            )
        )

        Spacer(Modifier.height(dimensions.itemSpacing * 2f))

        // 🎡 CARROUSEL D'AVATARS AVEC BOUCLE INFINIE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Flèche Gauche (Reculer avec boucle)
            IconButton(
                onClick = {
                    scope.launch {
                        val targetPage = if (pagerState.currentPage > 0) pagerState.currentPage - 1
                        else availableAvatars.size - 1
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = null,
                    tint = Color(0xFF673AB7)
                )
            }

            // Pager centré sans débordement visible des voisins
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .height(dimensions.profilePictureSize * 1.1f),
                contentPadding = PaddingValues(horizontal = 0.dp), // Nettoyage du centrage
                verticalAlignment = Alignment.CenterVertically
            ) { page ->
                val avatarId = availableAvatars[page]
                val isSelected = (selectedAvatarResId == avatarId)

                val scale by animateFloatAsState(if (isSelected) 1.1f else 0.7f, label = "zoom")
                val alpha by animateFloatAsState(if (isSelected) 1f else 0.2f, label = "alpha")

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .size(dimensions.profilePictureSize * 0.9f)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF673AB7).copy(0.1f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color(0xFF673AB7) else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(page) } },
                        contentAlignment = Alignment.Center
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

            // Flèche Droite (Avancer avec boucle)
            IconButton(
                onClick = {
                    scope.launch {
                        val targetPage = if (pagerState.currentPage < availableAvatars.size - 1) pagerState.currentPage + 1
                        else 0
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF673AB7)
                )
            }
        }

        Spacer(Modifier.height(dimensions.itemSpacing * 2f))

        // --- FORMULAIRE ---
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

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
            ) {
                Text("Annuler", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = {
                    val redValue = redevance.toDoubleOrNull() ?: 0.0
                    onSave(firstName, lastName, selectedAvatarResId, redValue)
                },
                modifier = Modifier.weight(1f).height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF673AB7)) },
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensions.itemSpacing),
        shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
        isError = errorText.isNotEmpty(),
        supportingText = { if (errorText.isNotEmpty()) Text(errorText) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF673AB7),
            focusedLabelColor = Color(0xFF673AB7)
        )
    )
}

@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Composable
fun ProfileEditorPreview() {
    MaterialTheme {
        val dims = rememberResponsiveDimensions()
        ProfileEditor(
            profile = com.miage.learnity.data.UserProfile(
                firstName = "Axel",
                lastName = "H",
                photoUrl = "avatar_b1",
                redevanceSoutienUnitaire = 1.0
            ),
            availableAvatars = listOf(R.drawable.avatar_b1, R.drawable.avatar_b2, R.drawable.avatar_b3),
            onSave = { _, _, _, _ -> },
            onCancel = {},
            isLoading = false,
            dimensions = dims
        )
    }
}