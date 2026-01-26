package com.miage.learnity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R
import com.miage.learnity.ui.utils.*
import kotlinx.coroutines.launch

private val IconBg = Color(0xfff0f1f3)
private val PrimaryText = Color(0xff1b1c1e)
private val SecondaryText = Color(0xff8a8e95)
private val MidSheet = Color(0xfff3f4f6)

@Composable
fun ProfileScreen(
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Profil mis à jour avec succès !",
                    duration = SnackbarDuration.Short
                )
                viewModel.resetUpdateSuccess()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.arc_pic),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.profilePictureSize * 2.7f)  // ✅ 260.sdp()
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(dimensions.profilePictureSize * 0.83f))  // ✅ 80.sdp()

                when {
                    uiState.isLoading && uiState.profile == null -> LoadingProfileState(dimensions)
                    uiState.profile != null -> {
                        ProfileContent(
                            profile = uiState.profile!!,
                            isDiscoveryMode = isDiscoveryMode,
                            onModeChange = { onModeChange(it) },
                            onLogout = { onLogout() },
                            onEditClick = { onEditClick() },
                            dimensions = dimensions
                        )
                    }
                    uiState.error != null -> ErrorProfileState(
                        uiState.error!!,
                        { viewModel.refresh() },
                        dimensions
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: com.miage.learnity.data.UserProfile,
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current

    // ⭐ LOGIQUE DYNAMIQUE ULTRA-SÉCURISÉE
    val avatarResId = remember(profile.photoUrl) {
        val photoName = if (profile.photoUrl.isNullOrBlank()) "avatar_b1" else profile.photoUrl

        try {
            val id = context.resources.getIdentifier(photoName, "drawable", context.packageName)
            if (id != 0) id else R.drawable.avatar_b1
        } catch (e: Exception) {
            R.drawable.avatar_b1
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ AVATAR AVEC BOUTON ÉDITION - RESPONSIVE
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                shadowElevation = dimensions.cardElevation * 4,  // ✅ 8.dp
                color = Color.White,
                modifier = Modifier.size(dimensions.profilePictureSize * 1.15f)  // ✅ 110.sdp()
            ) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Avatar utilisateur",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(dimensions.itemSpacing / 3)  // ✅ 4.dp
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFF673AB7),
                modifier = Modifier
                    .size(dimensions.iconSizeMedium * 1.42f)  // ✅ 34.sdp()
                    .clickable { onEditClick() }
                    .offset(x = (-4).dp, y = (-4).dp),
                shadowElevation = dimensions.cardElevation * 2  // ✅ 4.dp
            ) {
                Icon(
                    Icons.Default.Edit,
                    null,
                    tint = Color.White,
                    modifier = Modifier.padding(dimensions.itemSpacing / 1.5f)  // ✅ 8.dp
                )
            }
        }

        Spacer(Modifier.height(dimensions.itemSpacing * 1.33f))  // ✅ 16.sdp()

        // ✅ NOM ET EMAIL - RESPONSIVE
        Text(
            text = "${profile.firstName} ${profile.lastName}",
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = dimensions.titleMedium * 1.1f  // ✅ 22.ssp()
        )
        Text(
            text = profile.email,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = dimensions.bodyMedium  // ✅ 14.ssp()
        )
    }

    Spacer(Modifier.height(dimensions.itemSpacing * 2))  // ✅ 24.sdp()

    // ✅ CONTENU SCROLLABLE - RESPONSIVE
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                MidSheet,
                RoundedCornerShape(
                    topStart = dimensions.cornerRadiusLarge * 2.5f,  // ✅ 40.dp
                    topEnd = dimensions.cornerRadiusLarge * 2.5f
                )
            )
            .padding(
                horizontal = dimensions.screenPaddingHorizontal,
                vertical = dimensions.itemSpacing * 2  // ✅ 24.sdp()
            ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing * 1.67f)  // ✅ 20.sdp()
    ) {
        // ✅ TABLEAU DE BORD
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge * 1.5f),  // ✅ 24.dp
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                Text(
                    "TABLEAU DE BORD",
                    fontSize = dimensions.bodySmall * 0.92f,  // ✅ 11.ssp()
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText
                )
                Spacer(Modifier.height(dimensions.itemSpacing * 1.33f))  // ✅ 16.sdp()

                // ✅ CARTES STATS - RESPONSIVE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
                ) {
                    StatsCard(
                        title = "Unity Points",
                        value = "${profile.unityPoints}",
                        icon = R.drawable.ic_settings_1,
                        gradient = Brush.linearGradient(
                            listOf(Color(0xFF66BB6A), Color(0xFF00897B))
                        ),
                        modifier = Modifier.weight(1f),
                        dimensions = dimensions
                    )
                    StatsCard(
                        title = "Winstreak",
                        value = "${profile.currentStreak}",
                        subtitle = profile.bestStreak.toString(),
                        icon = R.drawable.ic_settings_1,
                        gradient = Brush.linearGradient(
                            listOf(Color(0xFFFFB74D), Color(0xFFE65100))
                        ),
                        modifier = Modifier.weight(1f),
                        dimensions = dimensions
                    )
                }

                Spacer(Modifier.height(dimensions.itemSpacing))

                // ✅ DETTE VIRTUELLE - RESPONSIVE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerRadiusMedium))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
                            )
                        )
                        .padding(dimensions.itemSpacing)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Dette Virtuelle",
                            color = Color.White,
                            fontSize = dimensions.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            String.format("%.2f €", profile.detteCumulee),
                            color = Color.White,
                            fontSize = dimensions.titleMedium * 1.2f,  // ✅ 24.ssp()
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "(Redevance: ${profile.redevanceSoutienUnitaire}€)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = dimensions.bodySmall * 0.83f  // ✅ 10.ssp()
                        )
                    }
                }
            }
        }

        // ✅ PARAMÈTRES - RESPONSIVE
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge * 1.5f),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(vertical = dimensions.itemSpacing / 1.5f)) {
                Text(
                    "PARAMÈTRES",
                    fontSize = dimensions.bodySmall * 0.92f,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    modifier = Modifier.padding(
                        horizontal = dimensions.cardPadding,
                        vertical = dimensions.itemSpacing / 1.5f
                    )
                )
                QuizModeToggleRow(isDiscoveryMode, { onModeChange(!isDiscoveryMode) }, dimensions)
                MenuItemRow("Mon Association", R.drawable.ic_asso, {}, dimensions)
                MenuItemRow("Réglages", R.drawable.ic_settings_1, {}, dimensions)
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = dimensions.cardPadding,
                        vertical = dimensions.itemSpacing / 1.5f
                    ),
                    color = IconBg
                )
                MenuItemRow("Déconnexion", R.drawable.btn_6, { onLogout() }, dimensions)
            }
        }

        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight * 1.56f))  // ✅ 100.dp
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: Int,
    gradient: Brush,
    modifier: Modifier = Modifier,
    dimensions: ResponsiveDimensions
) {
    Box(
        modifier = modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(dimensions.cornerRadiusMedium))
            .background(gradient)
            .padding(dimensions.itemSpacing / 1.5f)  // ✅ 8.sdp()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painterResource(id = icon),
                null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(dimensions.iconSizeSmall)  // ✅ 20.sdp()
            )
            Spacer(Modifier.height(dimensions.itemSpacing / 3))  // ✅ 4.sdp()
            Text(
                title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = dimensions.bodySmall * 0.92f  // ✅ 11.ssp()
            )
            Text(
                value,
                color = Color.White,
                fontSize = dimensions.bodyLarge * 1.13f,  // ✅ 18.ssp()
                fontWeight = FontWeight.ExtraBold
            )
            if (subtitle != null) {
                Text(
                    "Record : $subtitle",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = dimensions.bodySmall * 0.75f  // ✅ 9.ssp()
                )
            }
        }
    }
}

@Composable
private fun QuizModeToggleRow(
    isDiscoveryMode: Boolean,
    onToggle: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(
                horizontal = dimensions.cardPadding,
                vertical = dimensions.itemSpacing / 1.5f
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDiscoveryMode)
                Color(0xFF673AB7).copy(alpha = 0.1f)
            else
                IconBg,
            modifier = Modifier.size(dimensions.iconSizeLarge * 0.83f)  // ✅ 40.sdp()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(id = R.drawable.ic_settings_1),
                    null,
                    tint = if (isDiscoveryMode) Color(0xFF673AB7) else SecondaryText,
                    modifier = Modifier.size(dimensions.iconSizeSmall)
                )
            }
        }
        Spacer(Modifier.width(dimensions.itemSpacing))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Mode Quiz",
                fontSize = dimensions.bodyLarge * 0.94f,  // ✅ 15.ssp()
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                if (isDiscoveryMode) "Découverte" else "Révision",
                fontSize = dimensions.bodySmall,
                color = SecondaryText
            )
        }
        Switch(checked = isDiscoveryMode, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = dimensions.cardPadding,
                vertical = dimensions.itemSpacing * 0.83f  // ✅ 10.sdp()
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = IconBg,
            modifier = Modifier.size(dimensions.iconSizeLarge * 0.83f)  // ✅ 40.sdp()
        ) {
            Image(
                painterResource(id = iconRes),
                null,
                modifier = Modifier.padding(dimensions.itemSpacing * 0.83f)  // ✅ 10.dp
            )
        }
        Spacer(Modifier.width(dimensions.itemSpacing))
        Text(
            title,
            fontSize = dimensions.bodyLarge * 0.94f,  // ✅ 15.ssp()
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painterResource(R.drawable.arrow),
            null,
            tint = SecondaryText.copy(alpha = 0.5f),
            modifier = Modifier.size(dimensions.iconSizeMedium * 0.67f)  // ✅ 16.sdp()
        )
    }
}

@Composable
private fun LoadingProfileState(dimensions: ResponsiveDimensions) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(
            color = Color(0xFF673AB7),
            modifier = Modifier.size(dimensions.iconSizeLarge)
        )
    }
}

@Composable
private fun ErrorProfileState(
    message: String,
    onRetry: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(
            message,
            fontSize = dimensions.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(dimensions.itemSpacing))
        Button(
            onClick = onRetry,
            modifier = Modifier.height(dimensions.buttonHeightSmall)
        ) {
            Text("Réessayer", fontSize = dimensions.bodyMedium)
        }
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        val dimensions = rememberResponsiveDimensions()
        ProfileContent(
            profile = com.miage.learnity.data.UserProfile(
                firstName = "Axel",
                lastName = "H",
                email = "axel@learnity.fr",
                unityPoints = 1250,
                currentStreak = 7,
                bestStreak = 15,
                detteCumulee = 4.50,
                redevanceSoutienUnitaire = 0.10,
                photoUrl = "avatar_b1"
            ),
            isDiscoveryMode = true,
            onModeChange = {},
            onLogout = {},
            onEditClick = {},
            dimensions = dimensions
        )
    }
}