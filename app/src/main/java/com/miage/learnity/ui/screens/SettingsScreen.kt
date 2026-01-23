package com.miage.learnity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.ui.theme.ThemeViewModel
import com.miage.learnity.ui.theme.LearnityTheme

@Composable
fun SettingsScreen(
    // Utilisation d'un paramètre par défaut pour éviter l'erreur de construction
    themeViewModel: ThemeViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Dynamique
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Paramètres",
            style = MaterialTheme.typography.headlineMedium, // Dynamique
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // --- SECTION AFFICHAGE ---
        SettingsSectionCard(title = "Affichage") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode Sombre", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = themeViewModel.isDarkMode.value, // État global
                    onCheckedChange = { themeViewModel.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF9155FD)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Taille de police : " + when {
                    themeViewModel.fontScale.value < 0.3f -> "Petite"
                    themeViewModel.fontScale.value > 0.7f -> "Grande"
                    else -> "Normale"
                },
                fontSize = 14.sp,
                color = Color.Gray
            )
            Slider(
                value = themeViewModel.fontScale.value, // État global
                onValueChange = { themeViewModel.fontScale.value = it },
                steps = 1, // 3 positions : 0, 0.5, 1
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF9155FD),
                    activeTrackColor = Color(0xFF9155FD)
                )
            )
        }

        // --- SECTION NOTIFICATIONS ---
        SettingsSectionCard(title = "Notifications") {
            SettingsItemRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = "Tous les jours à 20h00"
            )
        }

        /// --- SECTION SÉCURITÉ ---
        SettingsSectionCard(title = "Sécurité et Données") {
            SettingsItemRow(
                icon = Icons.Outlined.Info,
                title = "Quiz sauvegardés (Offline)"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.4f)
            )

            // Ligne Vider le cache
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Vider le cache", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    // Texte en rouge pour l'action critique
                    Text(
                        text = "Supprimer mes données",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // --- SECTION À PROPOS ---
        SettingsSectionCard(title = "À propos") {
            SettingsItemRow(
                icon = Icons.Outlined.Info,
                title = "v1.0.4 - Sprint Final"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        color = Color(0xFF2D3142)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItemRow(icon: ImageVector, title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
@Preview(showBackground = true, name = "Mode Clair")
@Composable
fun SettingsScreenLightPreview() {
    // On utilise le thème pour voir les vraies couleurs
    LearnityTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, name = "Mode Sombre")
@Composable
fun SettingsScreenDarkPreview() {
    // On crée un ViewModel temporaire juste pour forcer le mode sombre dans l'aperçu
    val darkVM: ThemeViewModel = viewModel()
    darkVM.isDarkMode.value = true

    LearnityTheme(themeViewModel = darkVM) {
        SettingsScreen(themeViewModel = darkVM)
    }
}