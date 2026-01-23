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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R
import kotlinx.coroutines.launch

private val IconBg = Color(0xfff0f1f3)
private val PrimaryText = Color(0xff1b1c1e)
private val SecondaryText = Color(0xff8a8e95)
private val MidSheet = Color(0xfff3f4f6)

@Composable
fun ProfileScreen(
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit = {},
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ⭐ Gestion du retour visuel après sauvegarde
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
        containerColor = Color.Transparent // Permet de voir notre fond personnalisé derrière
    ) { paddingValues ->
        // On applique le paddingValues du Scaffold pour ne pas cacher le contenu sous la TopBar/BottomBar
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
        ) {
            // 1. IMAGE DE FOND (ARC) - Fixe
            Image(
                painter = painterResource(id = R.drawable.arc_pic),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Espace pour aligner la photo sur l'arc
                Spacer(modifier = Modifier.height(80.dp))

                when {
                    // On affiche le loader seulement si on n'a aucune donnée
                    uiState.isLoading && uiState.profile == null -> LoadingProfileState()

                    uiState.profile != null -> {
                        ProfileContent(
                            profile = uiState.profile!!,
                            isDiscoveryMode = isDiscoveryMode,
                            onModeChange = onModeChange,
                            onLogout = onLogout,
                            onEditClick = onEditClick
                        )
                    }

                    uiState.error != null -> ErrorProfileState(uiState.error!!, { viewModel.refresh() })
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
    onEditClick: () -> Unit
) {
    // --- HEADER PROFIL (Image + Nom) ---
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.size(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape).padding(4.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = Color(0xFF673AB7),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onEditClick() },
                shadowElevation = 4.dp
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "${profile.firstName} ${profile.lastName}",
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 20.sp
        )
        Text(
            text = profile.email,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
    }

    Spacer(Modifier.height(24.dp))

    // --- ZONE SCROLLABLE (Dashboard + Paramètres) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MidSheet, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- BOX 1 : DASHBOARD ---
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("TABLEAU DE BORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryText)
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = "Unity Points",
                        value = "${profile.unityPoints}",
                        icon = R.drawable.ic_settings_1,
                        gradient = Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF00897B))),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Winstreak",
                        value = "${profile.currentStreak}",
                        subtitle = profile.bestStreak.toString(),
                        icon = R.drawable.ic_settings_1,
                        gradient = Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFE65100))),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFF9966), Color(0xFFFF5E62))))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Dette Virtuelle", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(String.format("%.2f €", profile.detteCumulee), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text("(Redevance: ${profile.redevanceSoutienUnitaire}€)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
        }

        // --- BOX 2 : PARAMÈTRES ---
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("PARAMÈTRES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryText, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                QuizModeToggleRow(isDiscoveryMode, { onModeChange(!isDiscoveryMode) })
                MenuItemRow("Mon Association", R.drawable.ic_asso) {}
                MenuItemRow("Réglages", R.drawable.ic_settings_1) {}
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = IconBg)
                MenuItemRow("Déconnexion", R.drawable.btn_6, onLogout)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun StatsCard(title: String, value: String, subtitle: String? = null, icon: Int, gradient: Brush, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .aspectRatio(1.3f)
        .clip(RoundedCornerShape(16.dp))
        .background(gradient)
        .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Icon(painterResource(id = icon), null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                if (subtitle != null) {
                    Text(" (Record : $subtitle)", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                }
            }
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun QuizModeToggleRow(isDiscoveryMode: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = if (isDiscoveryMode) Color(0xFF673AB7).copy(alpha = 0.1f) else IconBg, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(id = R.drawable.ic_settings_1), null, tint = if (isDiscoveryMode) Color(0xFF673AB7) else SecondaryText, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Mode Quiz", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Text(if (isDiscoveryMode) "Découverte" else "Révision", fontSize = 12.sp, color = SecondaryText)
        }
        Switch(checked = isDiscoveryMode, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun MenuItemRow(title: String, iconRes: Int, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = IconBg, modifier = Modifier.size(40.dp)) {
            Image(painterResource(id = iconRes), null, modifier = Modifier.padding(10.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText, modifier = Modifier.weight(1f))
        Icon(painterResource(R.drawable.arrow), null, tint = SecondaryText.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

@Composable private fun LoadingProfileState() { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF673AB7)) } }
@Composable private fun ErrorProfileState(m: String, r: () -> Unit) { Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) { Text(m); Button(onClick = r) { Text("Retry") } } }

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileContent(
            profile = com.miage.learnity.data.UserProfile(
                firstName = "Axel", lastName = "H", email = "axel@learnity.fr",
                unityPoints = 1250, currentStreak = 7, bestStreak = 15,
                detteCumulee = 4.50, redevanceSoutienUnitaire = 0.10
            ),
            isDiscoveryMode = true,
            onModeChange = {},
            onEditClick = {},
            onLogout = {}
        )
    }
}