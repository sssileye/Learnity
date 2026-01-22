// ProfileScreen.kt - VERSION COMPLÈTE
package com.miage.learnity.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R

@Composable
fun ProfileScreen(
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    // Variables pour l'édition
    var editedFirstName by remember { mutableStateOf("") }
    var editedLastName by remember { mutableStateOf("") }

    // Initialiser les champs quand on entre en mode édition
    LaunchedEffect(uiState.isEditMode, uiState.profile) {
        if (uiState.isEditMode) {
            editedFirstName = uiState.profile?.firstName ?: ""
            editedLastName = uiState.profile?.lastName ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
        // Background header
        Image(
            painter = painterResource(id = R.drawable.arc_pic),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        // Card blanche en bas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 400.dp)
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(Color.White)
        )

        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 230.dp)
                .verticalScroll(scroll)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.profile != null -> {
                    val profile = uiState.profile!!

                    // Photo de profil
                    Surface(
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        color = Color.White,
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Mode édition ou affichage
                    if (uiState.isEditMode) {
                        // ✅ FORMULAIRE D'ÉDITION
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            OutlinedTextField(
                                value = editedFirstName,
                                onValueChange = { editedFirstName = it },
                                label = { Text("Prénom") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = editedLastName,
                                onValueChange = { editedLastName = it },
                                label = { Text("Nom") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelEdit() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Annuler")
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveProfile(
                                            editedFirstName,
                                            editedLastName
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = editedFirstName.isNotBlank() &&
                                            editedLastName.isNotBlank()
                                ) {
                                    Text("Enregistrer")
                                }
                            }
                        }
                    } else {
                        // ✅ AFFICHAGE NORMAL
                        val displayName = if (profile.firstName.isNotBlank() &&
                            profile.lastName.isNotBlank()) {
                            "${profile.firstName} ${profile.lastName}"
                        } else {
                            "Profil non complété"
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clickable { viewModel.enableEditMode() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1b1c1e)
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF673AB7)
                            )
                        }

                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF8a8e95)
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ✅ STATISTIQUES
                    if (!uiState.isEditMode) {
                        StatsSection(
                            unityPoints = profile.unityPoints,
                            currentStreak = profile.currentStreak,
                            bestStreak = profile.bestStreak,
                            detteCumulee = profile.detteCumulee
                        )

                        Spacer(Modifier.height(16.dp))
                    }

                    // ✅ PARAMÈTRES
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        QuizModeToggleRow(
                            isDiscoveryMode = isDiscoveryMode,
                            onToggle = { onModeChange(!isDiscoveryMode) }
                        )

                        MenuItemRow(
                            title = "Notifications",
                            icon = Icons.Default.Notifications,
                            onClick = { /* TODO */ }
                        )

                        MenuItemRow(
                            title = "Déconnexion",
                            icon = Icons.Default.ExitToApp,
                            onClick = {
                                viewModel.signOut()
                                onLogout()
                            }
                        )
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Erreur de chargement",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(text = uiState.error!!)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProfile() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ✅ SECTION STATISTIQUES
 */
@Composable
private fun StatsSection(
    unityPoints: Int,
    currentStreak: Int,
    bestStreak: Int,
    detteCumulee: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unity Points
        StatCard(
            modifier = Modifier.weight(1f),
            gradient = Brush.verticalGradient(
                listOf(Color(0xFF66BB6A), Color(0xFF00897B))
            ),
            icon = Icons.Default.Star,
            value = unityPoints.toString(),
            label = "Unity Points"
        )

        // Streak actuel
        StatCard(
            modifier = Modifier.weight(1f),
            gradient = Brush.verticalGradient(
                listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))
            ),
            icon = Icons.Default.LocalFireDepartment,
            value = "$currentStreak j",
            label = "Série actuelle"
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Meilleur streak
        StatCard(
            modifier = Modifier.weight(1f),
            gradient = Brush.verticalGradient(
                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
            ),
            icon = Icons.Default.EmojiEvents,
            value = "$bestStreak j",
            label = "Record"
        )

        // Dette
        StatCard(
            modifier = Modifier.weight(1f),
            gradient = Brush.verticalGradient(
                listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
            ),
            icon = Icons.Default.AccountBalance,
            value = String.format("%.2f€", detteCumulee),
            label = "Dette"
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(12.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuizModeToggleRow(
    isDiscoveryMode: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = if (isDiscoveryMode)
                Color(0xFF673AB7).copy(alpha = 0.1f)
            else
                Color(0xfff0f1f3),
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_1),
                    contentDescription = null,
                    tint = if (isDiscoveryMode) Color(0xFF673AB7) else Color(0xFF8a8e95),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mode Quiz du jour",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = if (isDiscoveryMode) "Découverte" else "Révision",
                fontSize = 13.sp,
                color = Color(0xFF8a8e95)
            )
        }

        Switch(
            checked = isDiscoveryMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF673AB7),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xfff0f1f3)
            )
        )
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = Color(0xfff0f1f3),
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF8a8e95),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(R.drawable.arrow),
            contentDescription = null,
            tint = Color(0xFF8a8e95)
        )
    }
}