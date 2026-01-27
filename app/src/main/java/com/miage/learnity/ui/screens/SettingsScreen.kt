package com.miage.learnity.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.theme.ThemeViewModel

@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Paramètres",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // ---------- AFFICHAGE ----------
        SettingsSectionCard(title = "Affichage") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode sombre",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Switch(
                    checked = themeViewModel.isDarkMode.value,
                    onCheckedChange = { checked ->
                        themeViewModel.setDarkMode(checked)
                    }
                )
            }
        }

        // ---------- NOTIFICATIONS ----------
        SettingsSectionCard(title = "Notifications") {
            SettingsItemRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = "Tous les jours à 20h00"
            )
        }

        // ---------- SÉCURITÉ ----------
        SettingsSectionCard(title = "Sécurité et Données") {
            SettingsItemRow(
                icon = Icons.Outlined.Info,
                title = "Quiz sauvegardés (Offline) "
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Vider le cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Supprimer mes données",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // ---------- À PROPOS ----------
        SettingsSectionCard(title = "À propos") {
            SettingsItemRow(
                icon = Icons.Outlined.Info,
                title = "v1.0.4 - Sprint Final"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/* ---------- COMPOSANTS ---------- */

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ---------- PREVIEWS ---------- */

@Preview(showBackground = true)
@Composable
@SuppressLint("ViewModelConstructorInComposable")
fun SettingsLightPreview() {
    val vm = remember { ThemeViewModel() }
    LearnityTheme(themeViewModel = vm) {
        SettingsScreen(vm)
    }
}

@Preview(showBackground = true)
@Composable
@SuppressLint("ViewModelConstructorInComposable")
fun SettingsDarkPreview() {
    val vm = remember { ThemeViewModel().apply { setDarkMode(true) } }
    LearnityTheme(themeViewModel = vm) {
        SettingsScreen(vm)
    }
}
