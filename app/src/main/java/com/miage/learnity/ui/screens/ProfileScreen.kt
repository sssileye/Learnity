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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.miage.learnity.data.UserProfile
import com.miage.learnity.ui.utils.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssociation: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.refreshProgressionStats()
        viewModel.refreshDailyStats() // Crucial pour savoir si un quiz est déjà fait
    }

    val dimensions = rememberResponsiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    val currentQuizMode = uiState.profile?.quizMode ?: "DISCOVERY"
    val isDiscoveryMode = currentQuizMode == "DISCOVERY"
    val isReviewUnlocked = uiState.readChaptersCount >= 5

    // ⭐ Détermine si le mode doit être verrouillé (Quiz déjà fait aujourd'hui)
    val isAlreadyDoneToday = uiState.dailyScore != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
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
                    .height(dimensions.profilePictureSize * 2.7f)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(dimensions.profilePictureSize * 0.83f))

                when {
                    uiState.isLoading && uiState.profile == null -> LoadingProfileState(dimensions)
                    uiState.profile != null -> {
                        ProfileContent(
                            profile = uiState.profile!!,
                            isDiscoveryMode = isDiscoveryMode,
                            isAlreadyDoneToday = isAlreadyDoneToday, // Passé au contenu
                            readCount = uiState.readChaptersCount,
                            totalCount = uiState.totalChaptersCount,
                            isReviewUnlocked = isReviewUnlocked,
                            onModeChange = { isDiscovery ->
                                val newMode = if (isDiscovery) "DISCOVERY" else "REVIEW"
                                viewModel.updateQuizMode(newMode)
                            },
                            onLogout = onLogout,
                            onEditClick = onEditClick,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToAssociation = onNavigateToAssociation,
                            dimensions = dimensions
                        )
                    }
                    uiState.error != null -> ErrorProfileState(
                        uiState.error!!,
                        { viewModel.refreshProgressionStats() },
                        dimensions
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    isDiscoveryMode: Boolean,
    isAlreadyDoneToday: Boolean,
    readCount: Int,
    totalCount: Int,
    isReviewUnlocked: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssociation: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val avatarResId = remember(profile.photoUrl) {
        val photoName = if (profile.photoUrl.isNullOrBlank()) "avatar_b1" else profile.photoUrl
        try {
            val id = context.resources.getIdentifier(photoName, "drawable", context.packageName)
            if (id != 0) id else R.drawable.avatar_b1
        } catch (e: Exception) { R.drawable.avatar_b1 }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                shadowElevation = dimensions.cardElevation * 4,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(dimensions.profilePictureSize * 1.15f)
            ) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(dimensions.itemSpacing / 3)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimensions.iconSizeMedium * 1.42f)
                    .clickable { onEditClick() }
                    .offset(x = (-4).dp, y = (-4).dp),
                shadowElevation = dimensions.cardElevation * 2
            ) {
                Icon(
                    Icons.Default.Edit, null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(dimensions.itemSpacing / 1.5f)
                )
            }
        }

        Spacer(Modifier.height(dimensions.itemSpacing * 1.33f))
        Text("${profile.firstName} ${profile.lastName}", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = dimensions.titleMedium * 1.1f)
        Text(profile.email, color = Color.White.copy(alpha = 0.9f), fontSize = dimensions.bodyMedium)
    }

    Spacer(Modifier.height(dimensions.itemSpacing * 2))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(topStart = dimensions.cornerRadiusLarge * 2.5f, topEnd = dimensions.cornerRadiusLarge * 2.5f)
            )
            .padding(horizontal = dimensions.screenPaddingHorizontal, vertical = dimensions.itemSpacing * 2),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing * 1.67f)
    ) {
        // --- TABLEAU DE BORD ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge * 1.5f),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                Text("TABLEAU DE BORD", fontSize = dimensions.bodySmall * 0.92f, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(dimensions.itemSpacing * 1.33f))

                ProgressionSection(readCount, totalCount, dimensions)

                Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                    StatsCard("Points", "${profile.unityPoints}", null, R.drawable.ic_settings_1, Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF00897B))), Modifier.weight(1f), dimensions)
                    StatsCard("Winstreak", "${profile.currentStreak}", profile.bestStreak.toString(), R.drawable.ic_settings_1, Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFE65100))), Modifier.weight(1f), dimensions)
                }
                Spacer(Modifier.height(dimensions.itemSpacing))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerRadiusMedium))
                        .background(Brush.linearGradient(listOf(Color(0xFFFF9966), Color(0xFFFF5E62))))
                        .padding(dimensions.itemSpacing)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dette Virtuelle", color = Color.White, fontSize = dimensions.bodySmall, fontWeight = FontWeight.Bold)
                        Text(String.format("%.2f €", profile.detteCumulee), color = Color.White, fontSize = dimensions.titleMedium * 1.2f, fontWeight = FontWeight.ExtraBold)
                        Text("(Redevance: ${profile.redevanceSoutienUnitaire}€)", color = Color.White.copy(alpha = 0.8f), fontSize = dimensions.bodySmall * 0.83f)
                    }
                }
            }
        }

        // --- PARAMÈTRES ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge * 1.5f),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(vertical = dimensions.itemSpacing / 1.5f)) {
                Text("PARAMÈTRES", fontSize = dimensions.bodySmall * 0.92f, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = dimensions.cardPadding, vertical = dimensions.itemSpacing / 1.5f))

                QuizModeToggleRow(
                    isDiscoveryMode = isDiscoveryMode,
                    isLocked = !isReviewUnlocked,
                    isAlreadyDoneToday = isAlreadyDoneToday, // Passé ici
                    readCount = readCount,
                    onToggle = { onModeChange(!isDiscoveryMode) },
                    dimensions = dimensions
                )

                MenuItemRow("Mon Association", R.drawable.ic_asso, onClick = onNavigateToAssociation, dimensions = dimensions)
                MenuItemRow("Réglages", R.drawable.ic_settings_1, onClick = onNavigateToSettings, dimensions = dimensions)

                HorizontalDivider(modifier = Modifier.padding(horizontal = dimensions.cardPadding, vertical = dimensions.itemSpacing / 1.5f), color = MaterialTheme.colorScheme.outlineVariant)

                MenuItemRow("Déconnexion", R.drawable.btn_6, onClick = onLogout, dimensions = dimensions)
            }
        }
        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight * 1.56f))
    }
}
@Composable
private fun ProgressionSection(readCount: Int, totalCount: Int, dimensions: ResponsiveDimensions) {
    val progress = if (totalCount > 0) readCount.toFloat() / totalCount else 0f

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ma progression", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("$readCount / $totalCount chapitres", fontSize = dimensions.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
@Composable
private fun QuizModeToggleRow(
    isDiscoveryMode: Boolean,
    isLocked: Boolean,
    isAlreadyDoneToday: Boolean,
    readCount: Int,
    onToggle: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    // ⭐ Le switch est désactivé si l'un des deux verrous est actif
    val isDisabled = isLocked || isAlreadyDoneToday

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isDisabled) { onToggle() }
                .padding(horizontal = dimensions.cardPadding, vertical = dimensions.itemSpacing / 1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (!isDisabled && !isDiscoveryMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(dimensions.iconSizeLarge * 0.83f)
                    .alpha(if (isDisabled) 0.5f else 1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_1),
                        contentDescription = null,
                        tint = if (!isDisabled && !isDiscoveryMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(dimensions.iconSizeSmall)
                    )
                }
            }

            Spacer(Modifier.width(dimensions.itemSpacing * 1.5f))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isDisabled) 0.5f else 1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode Quiz", fontSize = dimensions.bodyLarge * 0.94f, fontWeight = FontWeight.SemiBold)
                Text(
                    " : ${if (isDiscoveryMode) "Découverte" else "Révision"}",
                    fontSize = dimensions.bodyLarge * 0.94f,
                    color = if (isDiscoveryMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    fontWeight = if (isDiscoveryMode) FontWeight.Normal else FontWeight.Bold
                )
            }

            Switch(
                checked = !isDiscoveryMode,
                onCheckedChange = { onToggle() },
                enabled = !isDisabled,
                modifier = Modifier.scale(0.85f)
            )
        }

        // --- TEXTES D'INFORMATION ---
        if (isLocked) {
            Text(
                "Débloquez les révisions après 5 chapitres lus ($readCount/5)",
                color = MaterialTheme.colorScheme.error,
                fontSize = dimensions.bodySmall * 0.85f,
                modifier = Modifier
                    .padding(start = dimensions.cardPadding + 48.dp)
                    .offset(y = (-4).dp)
            )
        } else if (isAlreadyDoneToday) {
            Text(
                "Mode verrouillé jusqu'à demain (Quiz déjà effectué)",
                color = MaterialTheme.colorScheme.primary,
                fontSize = dimensions.bodySmall * 0.85f,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = dimensions.cardPadding + 48.dp)
                    .offset(y = (-4).dp)
            )
        }
    }
}

// ... (Les autres sous-composants MenuItemRow, StatsCard, etc. restent identiques)

@Composable
private fun MenuItemRow(title: String, iconRes: Int, onClick: () -> Unit, dimensions: ResponsiveDimensions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = dimensions.cardPadding, vertical = dimensions.itemSpacing * 0.83f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(dimensions.iconSizeLarge * 0.83f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeSmall)
                )
            }
        }
        Spacer(Modifier.width(dimensions.itemSpacing * 1.5f))
        Text(title, fontSize = dimensions.bodyLarge * 0.94f, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.arrow),
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(dimensions.iconSizeMedium * 0.6f)
        )
    }
}

@Composable
private fun StatsCard(title: String, value: String, subtitle: String? = null, icon: Int, gradient: Brush, modifier: Modifier = Modifier, dimensions: ResponsiveDimensions) {
    Box(modifier = modifier.aspectRatio(1.3f).clip(RoundedCornerShape(dimensions.cornerRadiusMedium)).background(gradient).padding(dimensions.itemSpacing / 1.5f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Icon(painterResource(icon), null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(dimensions.iconSizeSmall))
            Spacer(Modifier.height(dimensions.itemSpacing / 3))
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = dimensions.bodySmall * 0.92f)
            Text(value, color = Color.White, fontSize = dimensions.bodyLarge * 1.13f, fontWeight = FontWeight.ExtraBold)
            if (subtitle != null) Text("Record : $subtitle", color = Color.White.copy(alpha = 0.7f), fontSize = dimensions.bodySmall * 0.75f)
        }
    }
}

@Composable
private fun LoadingProfileState(dimensions: ResponsiveDimensions) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dimensions.iconSizeLarge)) }
}

@Composable
private fun ErrorProfileState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(message, fontSize = dimensions.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(dimensions.itemSpacing))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            profile = UserProfile(
                firstName = "Axel",
                lastName = "H",
                email = "axel@miage.fr",
                unityPoints = 1250,
                currentStreak = 7,
                bestStreak = 15,
                detteCumulee = 4.50,
                redevanceSoutienUnitaire = 0.10,
                photoUrl = "avatar_b1"
            ),
            isDiscoveryMode = true,
            isAlreadyDoneToday = false,
            readCount = 3,
            totalCount = 50,
            isReviewUnlocked = false,
            onModeChange = {},
            onLogout = {},
            onEditClick = {},
            onNavigateToSettings = {},
            onNavigateToAssociation = {},
            dimensions = rememberResponsiveDimensions()
        )
    }
}