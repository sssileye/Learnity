package com.miage.learnity.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R
import com.miage.learnity.data.FontSize
import com.miage.learnity.model.AuthViewModel
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.*

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAccountDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val dimensions = rememberResponsiveDimensions()
    val uiState by viewModel.uiState.collectAsState()

    val authState by authViewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var deletePassword by remember { mutableStateOf("") }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }


    LaunchedEffect(authState.accountDeleteSuccess) {
        if (authState.accountDeleteSuccess) {
            authViewModel.clearAccountDeleteSuccess()
            onAccountDeleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPaddingHorizontal)
            .responsiveMaxWidth(dimensions)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Text(
            text = "Paramètres",
            fontSize = dimensions.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(vertical = dimensions.itemSpacing / 2)) {
                Text(
                    "AFFICHAGE",
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = dimensions.cardPadding,
                        vertical = dimensions.itemSpacing / 2
                    )
                )

                DarkModeToggle(
                    isDarkMode = uiState.isDarkMode,
                    onToggle = { viewModel.toggleDarkMode() },
                    dimensions = dimensions
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = dimensions.cardPadding),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                FontSizeSelector(
                    selectedSize = uiState.fontSize,
                    onSizeSelected = { viewModel.setFontSize(it) },
                    dimensions = dimensions
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(vertical = dimensions.itemSpacing / 2)) {
                Text(
                    "SUPPORT",
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = dimensions.cardPadding,
                        vertical = dimensions.itemSpacing / 2
                    )
                )

                SettingsMenuItem(
                    icon = R.drawable.ic_settings_1,
                    title = "Contactez-nous",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contact@learnity.fr")
                            putExtra(Intent.EXTRA_SUBJECT, "Contact - Learnity")
                        }
                        context.startActivity(intent)
                    },
                    dimensions = dimensions,
                    showDivider = false
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            shadowElevation = dimensions.cardElevation
        ) {
            Column(modifier = Modifier.padding(vertical = dimensions.itemSpacing / 2)) {
                Text(
                    "COMPTE",
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = dimensions.cardPadding,
                        vertical = dimensions.itemSpacing / 2
                    )
                )

                SettingsMenuItem(
                    icon = R.drawable.ic_settings_1,
                    title = "Informations légales",
                    onClick = { showLegalDialog = true },
                    dimensions = dimensions
                )

                SettingsMenuItem(
                    icon = R.drawable.ic_settings_1,
                    title = "Politique de confidentialité",
                    onClick = { showPrivacyDialog = true },
                    dimensions = dimensions
                )

                SettingsMenuItem(
                    icon = R.drawable.ic_settings_1,
                    title = "À propos",
                    onClick = { showAboutDialog = true },
                    dimensions = dimensions,
                    showDivider = false
                )
            }
        }

        OutlinedButton(
            onClick = { viewModel.resetToDefaults() },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.buttonHeightSmall),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                "Réinitialiser les paramètres",
                fontSize = dimensions.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensions.cardPadding)
            ) {
                Text(
                    "ZONE DANGEREUSE",
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = dimensions.itemSpacing / 2)
                )

                Text(
                    "Cette action est irréversible",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = dimensions.itemSpacing)
                )

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_1),
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                    Spacer(Modifier.width(dimensions.itemSpacing / 2))
                    Text(
                        "Supprimer mon compte",
                        fontSize = dimensions.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo Learnity",
                    modifier = Modifier.size(dimensions.iconSizeLarge)
                )

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    text = "LEARNITY",
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Version 1.0.0",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    text = "Réviser pour soi, donner pour les autres",
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.ui.text.TextStyle(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false }, dimensions = dimensions)
    }

    if (showLegalDialog) {
        LegalInfoDialog(onDismiss = { showLegalDialog = false }, dimensions = dimensions)
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false }, dimensions = dimensions)
    }

    if (showDeleteDialog) {
        DeleteAccountDialogWithPassword(
            confirmationText = deleteConfirmationText,
            onConfirmationTextChange = { deleteConfirmationText = it },
            password = deletePassword,
            onPasswordChange = { deletePassword = it },
            isLoading = authState.isLoading,
            error = authState.error,
            onDismiss = {
                showDeleteDialog = false
                deleteConfirmationText = ""
                deletePassword = ""
                authViewModel.clearError()
            },
            onConfirmDelete = { password ->
                authViewModel.deleteAccountWithPassword(password)
            },
            dimensions = dimensions
        )
    }
}

@Composable
private fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(
                horizontal = dimensions.cardPadding,
                vertical = dimensions.itemSpacing
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDarkMode) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            },
            modifier = Modifier.size(dimensions.iconSizeLarge)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isDarkMode) "🌙" else "☀️",
                    fontSize = dimensions.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.width(dimensions.itemSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mode sombre",
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isDarkMode) "Activé" else "Désactivé",
                fontSize = dimensions.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isDarkMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun FontSizeSelector(
    selectedSize: FontSize,
    onSizeSelected: (FontSize) -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.cardPadding,
                vertical = dimensions.itemSpacing
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(dimensions.iconSizeLarge)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aa",
                        fontSize = dimensions.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Taille de police",
                    fontSize = dimensions.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (selectedSize) {
                        FontSize.SMALL -> "Petite"
                        FontSize.MEDIUM -> "Moyenne"
                        FontSize.LARGE -> "Grande"
                    },
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
        ) {
            FontSizeChip(
                text = "Petit",
                isSelected = selectedSize == FontSize.SMALL,
                onClick = { onSizeSelected(FontSize.SMALL) },
                dimensions = dimensions,
                modifier = Modifier.weight(1f)
            )

            FontSizeChip(
                text = "Moyen",
                isSelected = selectedSize == FontSize.MEDIUM,
                onClick = { onSizeSelected(FontSize.MEDIUM) },
                dimensions = dimensions,
                modifier = Modifier.weight(1f)
            )

            FontSizeChip(
                text = "Grand",
                isSelected = selectedSize == FontSize.LARGE,
                onClick = { onSizeSelected(FontSize.LARGE) },
                dimensions = dimensions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FontSizeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                fontSize = dimensions.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier.height(dimensions.buttonHeightSmall),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}
@Composable
private fun SettingsMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = dimensions.cardPadding,
                    vertical = dimensions.itemSpacing
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(dimensions.iconSizeLarge)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Text(
                text = title,
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_settings_1),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensions.iconSizeMedium)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = dimensions.cardPadding),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}
@Composable
fun DeleteAccountDialogWithPassword(
    confirmationText: String,
    onConfirmationTextChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit,
    dimensions: ResponsiveDimensions
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_1),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
                Spacer(Modifier.width(dimensions.itemSpacing / 2))
                Text(
                    "Supprimer le compte",
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                Text(
                    "⚠️ Attention : Cette action est définitive !",
                    fontSize = dimensions.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Toutes vos données seront supprimées :",
                    fontSize = dimensions.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.padding(start = dimensions.itemSpacing),
                    verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 4)
                ) {
                    Text("• Votre profil", fontSize = dimensions.bodyMedium)
                    Text("• Votre progression", fontSize = dimensions.bodyMedium)
                    Text("• Vos Unity Points", fontSize = dimensions.bodyMedium)
                    Text("• Votre historique de quiz", fontSize = dimensions.bodyMedium)
                }

                Spacer(Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    "Pour confirmer, tapez SUPPRIMER ci-dessous :",
                    fontSize = dimensions.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = onConfirmationTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("SUPPRIMER") },
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                )

                Spacer(Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    "Entrez votre mot de passe actuel :",
                    fontSize = dimensions.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Mot de passe") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (confirmationText == "SUPPRIMER" && password.isNotBlank()) {
                                onConfirmDelete(password)
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.Visibility
                                } else {
                                    Icons.Filled.VisibilityOff
                                },
                                contentDescription = if (passwordVisible) {
                                    "Masquer le mot de passe"
                                } else {
                                    "Afficher le mot de passe"
                                }
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                )

                if (error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            error,
                            fontSize = dimensions.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(dimensions.itemSpacing / 2)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDelete(password) },
                enabled = confirmationText == "SUPPRIMER" && password.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensions.iconSizeMedium),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Supprimer définitivement",
                        fontSize = dimensions.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    "Annuler",
                    fontSize = dimensions.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
@Composable
fun AboutDialog(onDismiss: () -> Unit, dimensions: ResponsiveDimensions) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo",
                    modifier = Modifier.size(dimensions.iconSizeLarge * 1.5f)
                )
                Spacer(Modifier.height(dimensions.itemSpacing))
                Text(
                    "À propos de Learnity",
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center // Sécurité supplémentaire
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(), // Centrage aussi pour le corps du texte
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Version 1.0.0",
                    fontSize = dimensions.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Réviser pour soi, donner pour les autres",
                    fontSize = dimensions.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    """
                    Learnity est une application mobile d'apprentissage qui combine éducation et solidarité.
                    
                    Chaque quiz complété vous rapporte des Unity Points.
                    
                    Votre engagement profite aux associations partenaires.
                    
                    © 2026 - Projet M2 MIAGE Bordeaux
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text("Fermer", fontSize = dimensions.bodyMedium)
                }
            }
        }
    )
}

@Composable
fun LegalInfoDialog(onDismiss: () -> Unit, dimensions: ResponsiveDimensions) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Informations légales",
                fontSize = dimensions.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                Text(
                    "ÉDITEUR",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Application : LEARNITY
                    Type : Application mobile d'apprentissage
                    Projet pédagogique : M2 MIAGE - Université de Bordeaux
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "PROPRIÉTÉ INTELLECTUELLE",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    L'ensemble du contenu de cette application (textes, images, logos) est protégé par le droit d'auteur.
                    
                    Toute reproduction, distribution ou utilisation non autorisée est interdite.
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "DONNÉES PERSONNELLES",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Vos données sont traitées conformément au RGPD.
                    
                    Pour plus d'informations, consultez notre Politique de confidentialité.
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "CONTACT",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    "Email : contact@learnity.fr",
                    fontSize = dimensions.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontSize = dimensions.bodyMedium)
            }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit, dimensions: ResponsiveDimensions) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Politique de confidentialité",
                fontSize = dimensions.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                Text(
                    "COLLECTE DES DONNÉES",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Nous collectons les données suivantes :
                    • Nom et prénom
                    • Adresse email
                    • Progression dans les cours
                    • Résultats des quiz
                    • Unity Points et streaks
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "UTILISATION DES DONNÉES",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Vos données sont utilisées pour :
                    • Personnaliser votre expérience d'apprentissage
                    • Suivre votre progression
                    • Calculer votre dette virtuelle
                    • Améliorer nos services
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "SÉCURITÉ",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Vos données sont stockées de manière sécurisée sur Firebase et ne sont jamais partagées avec des tiers sans votre consentement.
                    
                    Nous utilisons des protocoles de sécurité standard pour protéger vos informations.
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )

                HorizontalDivider()

                Text(
                    "VOS DROITS",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    """
                    Conformément au RGPD, vous disposez des droits suivants :
                    • Droit d'accès à vos données
                    • Droit de rectification
                    • Droit à l'effacement
                    • Droit d'opposition
                    
                    Pour exercer ces droits : contact@learnity.fr
                    """.trimIndent(),
                    fontSize = dimensions.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontSize = dimensions.bodyMedium)
            }
        }
    )
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun SettingsScreenPreview() {
    LearnityTheme {
        SettingsScreen()
    }
}