package com.miage.learnity.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview

// Couleurs de la charte graphique
val BackgroundColor = Color(0xFFF5F5F5)
val PrimaryPurple = Color(0xFF9E5ECE)
val GradientBlue1 = Color(0xFF4E54C8)
val GradientBlue2 = Color(0xFF8F94FB)
val GradientOrange1 = Color(0xFFFF4B2B)
val GradientOrange2 = Color(0xFFFF8C37)
val TextDark = Color(0xFF1A1A2E)
val TextGray = Color(0xFF4A4A4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inscription(
    onBackClick: () -> Unit,
    onInscriptionSuccess: () -> Unit
) {
    var nomPrenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptCGU by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // États d'erreur
    var nomPrenomError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    // Validation
    fun validateForm(): Boolean {
        var isValid = true

        if (nomPrenom.isBlank()) {
            nomPrenomError = "Le nom et prénom sont obligatoires"
            isValid = false
        } else {
            nomPrenomError = ""
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (email.isBlank()) {
            emailError = "L'email est obligatoire"
            isValid = false
        } else if (!email.matches(emailRegex)) {
            emailError = "Format d'email invalide"
            isValid = false
        } else {
            emailError = ""
        }

        if (password.isBlank()) {
            passwordError = "Le mot de passe est obligatoire"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "Au moins 8 caractères"
            isValid = false
        } else if (!password.any { it.isDigit() }) {
            passwordError = "Au moins 1 chiffre"
            isValid = false
        } else if (!password.any { it.isUpperCase() }) {
            passwordError = "Au moins 1 majuscule"
            isValid = false
        } else {
            passwordError = ""
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirmez le mot de passe"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Les mots de passe diffèrent"
            isValid = false
        } else {
            confirmPasswordError = ""
        }

        return isValid
    }

    val isButtonEnabled = nomPrenom.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            acceptCGU

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Décorations colorées (coins)
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset((-20).dp, (-20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientOrange1.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .offset(40.dp, 40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GradientBlue1.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Retour",
                                tint = TextDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Titre
                Text(
                    text = "Créer mon compte",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = "Rejoignez Learnity",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                // Champ Nom & Prénom
                OutlinedTextField(
                    value = nomPrenom,
                    onValueChange = {
                        nomPrenom = it
                        nomPrenomError = ""
                    },
                    label = { Text("Nom & Prénom") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nomPrenomError.isNotEmpty(),
                    supportingText = if (nomPrenomError.isNotEmpty()) {
                        { Text(nomPrenomError, fontSize = 12.sp) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryPurple
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Champ Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = ""
                    },
                    label = { Text("Email étudiant") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError.isNotEmpty(),
                    supportingText = if (emailError.isNotEmpty()) {
                        { Text(emailError, fontSize = 12.sp) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryPurple
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Champ Mot de passe
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = ""
                    },
                    label = { Text("Mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    isError = passwordError.isNotEmpty(),
                    supportingText = if (passwordError.isNotEmpty()) {
                        { Text(passwordError, fontSize = 12.sp) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryPurple
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Champ Confirmer
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    label = { Text("Confirmer mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    isError = confirmPasswordError.isNotEmpty(),
                    supportingText = if (confirmPasswordError.isNotEmpty()) {
                        { Text(confirmPasswordError, fontSize = 12.sp) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryPurple
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Checkbox CGU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptCGU,
                        onCheckedChange = { acceptCGU = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryPurple
                        )
                    )
                    Text(
                        text = "J'accepte les CGU",
                        fontSize = 14.sp,
                        color = TextGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton S'inscrire
                Button(
                    onClick = {
                        if (validateForm()) {
                            onInscriptionSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (isButtonEnabled) {
                                    Brush.horizontalGradient(
                                        colors = listOf(GradientBlue1, GradientBlue2)
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Gray, Color.Gray)
                                    )
                                },
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S'inscrire",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InscriptionPreview() {
    MaterialTheme {
        Inscription(
            onBackClick = {},
            onInscriptionSuccess = {}
        )
    }
}
