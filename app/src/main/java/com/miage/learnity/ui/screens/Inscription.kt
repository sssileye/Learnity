package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
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
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    // États du formulaire
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptCGU by remember { mutableStateOf(false) }

    // États d'erreur
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    // Fonctions de validation
    fun validateName(name: String, fieldName: String): Pair<Boolean, String> {
        return when {
            name.isBlank() -> false to "$fieldName requis"
            name.length < 2 -> false to "Au moins 2 caractères"
            !name.all { it.isLetter() || it.isWhitespace() } -> false to "Lettres uniquement"
            else -> true to ""
        }
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (!email.matches(emailRegex)) {
            emailError = "Format d'email invalide"
            false
        } else {
            emailError = ""
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return when {
            password.length < 8 -> {
                passwordError = "Au moins 8 caractères requis"
                false
            }
            !password.any { it.isDigit() } -> {
                passwordError = "Au moins 1 chiffre requis"
                false
            }
            !password.any { it.isUpperCase() } -> {
                passwordError = "Au moins 1 majuscule requise"
                false
            }
            else -> {
                passwordError = ""
                true
            }
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        return if (password != confirmPassword) {
            confirmPasswordError = "Les mots de passe ne correspondent pas"
            false
        } else {
            confirmPasswordError = ""
            true
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val isButtonEnabled = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            acceptCGU &&
            !isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF635BFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = dimensions.screenPaddingHorizontal)  // ✅ Responsive
                .responsiveMaxWidth(dimensions)  // ✅ Limite largeur
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // Logo - RESPONSIVE
            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo Learnity",
                modifier = Modifier.size(dimensions.logoSize)  // ✅ 100.sdp()
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // Titre - RESPONSIVE
            Text(
                text = "Rejoignez-nous !",
                fontSize = dimensions.titleLarge,  // ✅ 28.ssp()
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 2))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHAMP PRÉNOM - RESPONSIVE
            // ═══════════════════════════════════════
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    if (firstNameError.isNotEmpty()) {
                        val (_, error) = validateName(it, "Prénom")
                        firstNameError = error
                    }
                },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
                isError = firstNameError.isNotEmpty(),
                supportingText = {
                    if (firstNameError.isNotEmpty()) {
                        Text(
                            text = firstNameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHAMP NOM - RESPONSIVE
            // ═══════════════════════════════════════
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    if (lastNameError.isNotEmpty()) {
                        val (_, error) = validateName(it, "Nom")
                        lastNameError = error
                    }
                },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
                isError = lastNameError.isNotEmpty(),
                supportingText = {
                    if (lastNameError.isNotEmpty()) {
                        Text(
                            text = lastNameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHAMP EMAIL - RESPONSIVE
            // ═══════════════════════════════════════
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) validateEmail(it)
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHAMP MOT DE PASSE - RESPONSIVE
            // ═══════════════════════════════════════
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError.isNotEmpty()) validatePassword(it)
                },
                label = { Text("Mot de passe") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSizeMedium)  // ✅ 24.sdp()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    } else {
                        Text(
                            text = "8 caractères min, 1 chiffre, 1 majuscule",
                            color = Color.Gray,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHAMP CONFIRMATION MOT DE PASSE - RESPONSIVE
            // ═══════════════════════════════════════
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError.isNotEmpty()) {
                        validateConfirmPassword(password, it)
                    }
                },
                label = { Text("Confirmer le mot de passe") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showConfirmPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSizeMedium)  // ✅ 24.sdp()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = {
                    if (confirmPasswordError.isNotEmpty()) {
                        Text(
                            text = confirmPasswordError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall  // ✅ 12.ssp()
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))  // ✅ Responsive

            // ═══════════════════════════════════════
            // CHECKBOX CGU - RESPONSIVE
            // ═══════════════════════════════════════
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptCGU,
                    onCheckedChange = { acceptCGU = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF635BFF)
                    )
                )
                Text(
                    text = "J'accepte les conditions générales d'utilisation",
                    color = TextGray,
                    fontSize = dimensions.bodyMedium  // ✅ 14.ssp()
                )
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 2))  // ✅ Responsive

            // ═══════════════════════════════════════
            // BOUTON INSCRIPTION - RESPONSIVE
            // ═══════════════════════════════════════
            Button(
                onClick = {
                    // Validation complète
                    val (isFirstNameValid, firstNameErr) = validateName(firstName, "Prénom")
                    firstNameError = firstNameErr

                    val (isLastNameValid, lastNameErr) = validateName(lastName, "Nom")
                    lastNameError = lastNameErr

                    val isEmailValid = validateEmail(email)
                    val isPasswordValid = validatePassword(password)
                    val isConfirmValid = validateConfirmPassword(password, confirmPassword)

                    if (isFirstNameValid && isLastNameValid && isEmailValid &&
                        isPasswordValid && isConfirmValid) {
                        onInscriptionSuccess(
                            email.trim(),
                            password.trim(),
                            firstName.trim(),
                            lastName.trim()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),  // ✅ 56.sdp()
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),  // ✅ 16.dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF635BFF),
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(dimensions.iconSizeMedium)  // ✅ 24.sdp()
                    )
                } else {
                    Text(
                        text = "S'inscrire",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))  // ✅ Responsive
        }
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun InscriptionPreview() {
    LearnityTheme {
        Inscription()
    }
}