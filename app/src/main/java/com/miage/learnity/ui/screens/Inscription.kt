package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
    onInscriptionSuccess: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    isLoading: Boolean = false,
    error: String? = null
) {
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val primaryColor = Color(0xFF635BFF)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptCGU by remember { mutableStateOf(false) }
    var showCGUDialog by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    fun validateFields(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        firstNameError = if (firstName.isBlank()) "Prénom requis" else ""
        lastNameError = if (lastName.isBlank()) "Nom requis" else ""
        emailError = if (!email.matches(emailRegex)) "Email invalide" else ""
        passwordError = if (password.length < 8) "8 caractères min" else ""
        confirmPasswordError = if (password != confirmPassword) "Mots de passe différents" else ""
        return firstNameError.isEmpty() && lastNameError.isEmpty() && emailError.isEmpty() &&
                passwordError.isEmpty() && confirmPasswordError.isEmpty()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = primaryColor)
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
                .responsiveMaxWidth(dimensions)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.itemSpacing))
            Image(painter = painterResource(id = R.drawable.icon_learnity), contentDescription = "Logo", modifier = Modifier.size(dimensions.logoSize))
            Text(text = "Rejoignez-nous !", fontSize = dimensions.titleLarge, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                    FormSectionHeader(Icons.Default.Person, "Mes informations", primaryColor, dimensions)
                    RegistrationTextField(firstName, { firstName = it }, "Prénom", firstNameError, dimensions, focusManager, accentColor = primaryColor)
                    RegistrationTextField(lastName, { lastName = it }, "Nom", lastNameError, dimensions, focusManager, accentColor = primaryColor)

                    Spacer(Modifier.height(dimensions.itemSpacing / 2))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(Modifier.height(dimensions.itemSpacing / 2))

                    FormSectionHeader(Icons.Default.Lock, "Sécurité", primaryColor, dimensions)
                    RegistrationTextField(email, { email = it }, "Email", emailError, dimensions, focusManager, KeyboardType.Email, accentColor = primaryColor)
                    RegistrationTextField(password, { password = it }, "Mot de passe", passwordError, dimensions, focusManager, isPassword = true, isVisible = showPassword, onVisibilityChange = { showPassword = it }, accentColor = primaryColor)
                    RegistrationTextField(confirmPassword, { confirmPassword = it }, "Confirmer", confirmPasswordError, dimensions, focusManager, isPassword = true, isVisible = showConfirmPassword, onVisibilityChange = { showConfirmPassword = it }, accentColor = primaryColor, imeAction = ImeAction.Done)
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = acceptCGU, onCheckedChange = { acceptCGU = it }, colors = CheckboxDefaults.colors(checkedColor = primaryColor))
                TextButton(onClick = { showCGUDialog = true }, contentPadding = PaddingValues(0.dp)) {
                    Text("J'accepte les CGU", textDecoration = TextDecoration.Underline, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing))

            Button(
                onClick = { if (validateFields()) onInscriptionSuccess(email.trim(), password.trim(), firstName.trim(), lastName.trim()) },
                modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
                enabled = acceptCGU && !isLoading,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(dimensions.iconSizeMedium))
                else Text("S'inscrire", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
            }
            Spacer(Modifier.height(dimensions.screenPaddingVertical))
        }
    }

    if (showCGUDialog) {
        AlertDialog(
            onDismissRequest = { showCGUDialog = false },
            title = { Text("CGU – LEARNITY", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.cguText))
                }
            },
            confirmButton = {
                TextButton(onClick = { showCGUDialog = false }) { Text("Fermer", color = primaryColor, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

// --- Fonctions utilitaires conservées ---
@Composable
fun FormSectionHeader(icon: ImageVector, title: String, color: Color, dimensions: ResponsiveDimensions, onInfoClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = dimensions.itemSpacing / 2)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(dimensions.iconSizeSmall))
        Spacer(Modifier.width(dimensions.itemSpacing / 2))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RegistrationTextField(value: String, onValueChange: (String) -> Unit, label: String, errorText: String, dimensions: ResponsiveDimensions, focusManager: androidx.compose.ui.focus.FocusManager, keyboardType: KeyboardType = KeyboardType.Text, isPassword: Boolean = false, isVisible: Boolean = false, onVisibilityChange: (Boolean) -> Unit = {}, imeAction: ImeAction = ImeAction.Next, accentColor: Color) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, fontSize = dimensions.bodySmall) },
        modifier = Modifier.fillMaxWidth().padding(bottom = dimensions.itemSpacing / 4), isError = errorText.isNotEmpty(),
        supportingText = { if (errorText.isNotEmpty()) Text(errorText, fontSize = dimensions.bodySmall) },
        singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = dimensions.bodyLarge),
        visualTransformation = if (isPassword && !isVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = { if (isPassword) IconButton(onClick = { onVisibilityChange(!isVisible) }) { Icon(if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, modifier = Modifier.size(dimensions.iconSizeMedium)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }, onDone = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedLabelColor = accentColor)
    )
}

@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Composable
fun InscriptionPreview() {
    LearnityTheme { Inscription() }
}