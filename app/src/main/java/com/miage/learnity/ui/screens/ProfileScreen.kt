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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R

private val IconBg = Color(0xfff0f1f3)
private val PrimaryText = Color(0xff1b1c1e)
private val SecondaryText = Color(0xff8a8e95)
private val MidSheet = Color(0xfff3f4f6)

@Composable
fun ProfileScreen(
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(MidSheet)) {
        // Background arc
        Image(
            painter = painterResource(id = R.drawable.arc_pic),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // White rounded container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 400.dp)
            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
            .background(Color.White)
    )

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 200.dp)
            .verticalScroll(scroll)
    ) {
        when {
            uiState.isLoading -> LoadingProfileState()
            uiState.error != null -> ErrorProfileState(
                message = uiState.error!!,
                onRetry = { viewModel.refresh() }
            )
            uiState.profile != null -> ProfileContent(
                profile = uiState.profile!!,
                isDiscoveryMode = isDiscoveryMode,
                onModeChange = onModeChange,
                onEditClick = { viewModel.toggleEditMode() },
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: com.miage.learnity.data.UserProfile,
    isDiscoveryMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column {
        // Profile picture with edit button
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                shape = CircleShape,
                shadowElevation = 6.dp,
                color = Color.White,
                modifier = Modifier.size(96.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape)
                )
            }

            // Edit button
            Surface(
                shape = CircleShape,
                color = Color(0xFF673AB7),
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onEditClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Modifier",
                    tint = Color.White,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Name
        Text(
            text = "${profile.firstName} ${profile.lastName}".takeIf { it.isNotBlank() }
                ?: "Utilisateur",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Email
        Text(
            text = profile.email,
            style = MaterialTheme.typography.bodyLarge.copy(color = SecondaryText),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Stats cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unity Points Card
            StatsCard(
                title = "Unity Points",
                value = "${profile.unityPoints}",
                icon = R.drawable.ic_settings_1,
                gradient = Brush.linearGradient(
                    listOf(Color(0xFF66BB6A), Color(0xFF00897B))
                ),
                modifier = Modifier.weight(1f)
            )

            // Streak Card
            StatsCard(
                title = "Winstreak",
                value = "${profile.currentStreak}",
                subtitle = "Record: ${profile.bestStreak}",
                icon = R.drawable.ic_settings_1,
                gradient = Brush.linearGradient(
                    listOf(Color(0xFFFFB74D), Color(0xFFE65100))
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Dette card (full width)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF9966), Color(0xFFFF5E62))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Dette Virtuelle",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = String.format("%.2f €", profile.detteCumulee),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Redevance: ${profile.redevanceSoutienUnitaire}€",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Settings section
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "PARAMÈTRES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            QuizModeToggleRow(
                isDiscoveryMode = isDiscoveryMode,
                onToggle = { onModeChange(!isDiscoveryMode) }
            )

            MenuItemRow("Notification", R.drawable.btn_1) {}
            MenuItemRow("Mon Association", R.drawable.ic_asso) {}
            MenuItemRow("Réglages", R.drawable.ic_settings_1) {}

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            MenuItemRow("Déconnexion", R.drawable.btn_6, onLogout)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: Int,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = if (isDiscoveryMode) Color(0xFF673AB7).copy(alpha = 0.1f) else IconBg,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_1),
                    contentDescription = null,
                    tint = if (isDiscoveryMode) Color(0xFF673AB7) else SecondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mode Quiz du jour",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = if (isDiscoveryMode) "Découverte" else "Révision",
                fontSize = 13.sp,
                color = SecondaryText
            )
        }

        Switch(
            checked = isDiscoveryMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF673AB7),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = IconBg
            )
        )
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 6.dp,
            color = IconBg,
            modifier = Modifier.size(50.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryText,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.arrow),
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LoadingProfileState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF673AB7))
    }
}

@Composable
private fun ErrorProfileState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 48.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Erreur",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = SecondaryText
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF673AB7)
            )
        ) {
            Text("Réessayer")
        }
    }
}