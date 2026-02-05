package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inscription(
    onBackClick: () -> Unit = {},
    onInscriptionSuccess: (String, String, String, String, Double) -> Unit = { _, _, _, _, _ -> },
    isLoading: Boolean = false,
    error: String? = null
) {
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val primaryColor = Color(0xFF635BFF)

    // --- ÉTATS DES CHAMPS ---
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var redevance by remember { mutableStateOf("1.00") }

    // --- ÉTATS UI ---
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptCGU by remember { mutableStateOf(false) }
    var showCGUDialog by remember { mutableStateOf(false) }
    var showRedevanceDialog by remember { mutableStateOf(false) }

    // --- ÉTATS D'ERREUR ---
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var redevanceError by remember { mutableStateOf("") }

    // --- FONCTIONS DE VALIDATION ---
    fun validateFields(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        firstNameError = if (firstName.isBlank()) "Prénom requis" else ""
        lastNameError = if (lastName.isBlank()) "Nom requis" else ""
        emailError = if (!email.matches(emailRegex)) "Email invalide" else ""
        passwordError = if (password.length < 8) "8 caractères min" else ""
        confirmPasswordError = if (password != confirmPassword) "Mots de passe différents" else ""

        val redVal = redevance.toDoubleOrNull()
        redevanceError = when {
            redVal == null -> "Montant invalide"
            redVal < 0.10 -> "Minimum 0.10€"
            else -> ""
        }
        return firstNameError.isEmpty() && lastNameError.isEmpty() && emailError.isEmpty() &&
                passwordError.isEmpty() && confirmPasswordError.isEmpty() && redevanceError.isEmpty()
    }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Retour",
                            tint = primaryColor,
                            modifier = Modifier.size(dimensions.iconSizeMedium)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = dimensions.screenPaddingHorizontal)
                .responsiveMaxWidth(dimensions) // ✅ Application du MaxWidth responsive
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.itemSpacing))

            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo",
                modifier = Modifier.size(dimensions.logoSize)
            )

            Text(
                text = "Rejoignez-nous !",
                fontSize = dimensions.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

            // 📦 BLOC UNIQUE UNIFIÉ : RESPONSIVE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                    // --- Identité ---
                    FormSectionHeader(Icons.Default.Person, "Mes informations", primaryColor, dimensions)
                    RegistrationTextField(firstName, { firstName = it }, "Prénom", firstNameError, dimensions, focusManager, accentColor = primaryColor)
                    RegistrationTextField(lastName, { lastName = it }, "Nom", lastNameError, dimensions, focusManager, accentColor = primaryColor)

                    // ✅ Séparateur Responsive
                    Spacer(Modifier.height(dimensions.itemSpacing / 2))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(Modifier.height(dimensions.itemSpacing / 2))

                    // --- Sécurité ---
                    RegistrationTextField(email, { email = it }, "Email", emailError, dimensions, focusManager, KeyboardType.Email, accentColor = primaryColor)
                    RegistrationTextField(password, { password = it }, "Mot de passe", passwordError, dimensions, focusManager, isPassword = true, isVisible = showPassword, onVisibilityChange = { showPassword = it }, accentColor = primaryColor)
                    RegistrationTextField(confirmPassword, { confirmPassword = it }, "Confirmer le mot de passe", confirmPasswordError, dimensions, focusManager, isPassword = true, isVisible = showConfirmPassword, onVisibilityChange = { showConfirmPassword = it }, accentColor = primaryColor, imeAction = ImeAction.Next)

                    // ✅ Séparateur Responsive
                    Spacer(Modifier.height(dimensions.itemSpacing / 2))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(Modifier.height(dimensions.itemSpacing / 2))

                    // --- Engagement ---
                    FormSectionHeader(Icons.Default.Favorite, "Engagement solidaire (SYMBOLIQUE)", primaryColor, dimensions, onInfoClick = { showRedevanceDialog = true })
                    RegistrationTextField(redevance, { redevance = it }, "Redevance unitaire (€)", redevanceError, dimensions, focusManager, KeyboardType.Decimal, imeAction = ImeAction.Done, accentColor = primaryColor)
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing))

            // --- CGU ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptCGU,
                    onCheckedChange = { acceptCGU = it },
                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                )
                TextButton(
                    onClick = { showCGUDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "J'accepte les CGU",
                        textDecoration = TextDecoration.Underline,
                        fontSize = dimensions.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing))

            // --- BOUTON FINAL ---
            Button(
                onClick = {
                    if (validateFields()) {
                        onInscriptionSuccess(
                            email.trim(), password.trim(),
                            firstName.trim().replaceFirstChar { it.uppercase() },
                            lastName.trim().uppercase(), redevance.toDoubleOrNull() ?: 1.0
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                enabled = acceptCGU && !isLoading,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(dimensions.iconSizeMedium))
                } else {
                    Text(
                        "S'inscrire",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }
            Spacer(Modifier.height(dimensions.screenPaddingVertical))
        }
    }

    // --- DIALOGS (VIOLETS) ---
    if (showCGUDialog) {
        AlertDialog(
            onDismissRequest = { showCGUDialog = false },
            title = { Text("CGU – LEARNITY", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge) },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.cguText), fontSize = dimensions.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCGUDialog = false }) {
                    Text("Fermer", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showRedevanceDialog) {
        RedevanceExplanationDialog(
            primaryColor = primaryColor,
            dimensions = dimensions,
            onDismiss = { showRedevanceDialog = false }
        )
    }
}

@Composable
fun FormSectionHeader(
    icon: ImageVector,
    title: String,
    color: Color,
    dimensions: ResponsiveDimensions,
    onInfoClick: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = dimensions.itemSpacing / 2)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(dimensions.iconSizeSmall))
        Spacer(Modifier.width(dimensions.itemSpacing / 2))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = dimensions.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onInfoClick != null) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onInfoClick, modifier = Modifier.size(dimensions.iconSizeMedium)) {
                Icon(Icons.Default.Info, null, tint = color, modifier = Modifier.size(dimensions.iconSizeSmall))
            }
        }
    }
}

@Composable
fun RegistrationTextField(
    value: String, onValueChange: (String) -> Unit, label: String, errorText: String,
    dimensions: ResponsiveDimensions, focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardType: KeyboardType = KeyboardType.Text, isPassword: Boolean = false,
    isVisible: Boolean = false, onVisibilityChange: (Boolean) -> Unit = {},
    imeAction: ImeAction = ImeAction.Next, accentColor: Color
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = dimensions.bodySmall) },
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensions.itemSpacing / 4),
        isError = errorText.isNotEmpty(),
        supportingText = { if (errorText.isNotEmpty()) Text(errorText, fontSize = dimensions.bodySmall) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = dimensions.bodyLarge),
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onVisibilityChange(!isVisible) }) {
                    Icon(
                        if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus() }
        ),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedLabelColor = accentColor)
    )
}

@Composable
fun RedevanceExplanationDialog(
    primaryColor: Color,
    dimensions: ResponsiveDimensions,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Favorite, null, tint = primaryColor, modifier = Modifier.size(dimensions.iconSizeLarge)) },
        title = {
            Text(
                "Engagement solidaire",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = dimensions.titleLarge * 0.8f
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2)) {
                Text(
                    "Ce système est symbolique et pédagogique. Aucune information bancaire ne sera demandée.",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    fontSize = dimensions.bodyMedium
                )
                Text("• Erreur : Une fraction de votre redevance est ajoutée (Redevance ÷ Nb questions).", fontSize = dimensions.bodySmall)
                Text("• Absentéisme : Si vous ratez votre Quiz du Jour, le montant total est ajouté.", fontSize = dimensions.bodySmall)

                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(4.dp))

                Text(
                    "Cette dette est une jauge morale. Vous restez libre de verser ou non ce montant à l'association de votre choix. Learnity ne prélève rien.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = dimensions.bodySmall * 0.9f
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
            ) {
                Text("J'ai compris", fontSize = dimensions.bodyLarge)
            }
        }
    )
}

// --- PREVIEWS MULTI-TAILLES ---
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun InscriptionPreview() {
    LearnityTheme { Inscription() }
}