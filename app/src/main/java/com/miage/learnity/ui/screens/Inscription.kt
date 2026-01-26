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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inscription(
    onBackClick: () -> Unit = {},
    onInscriptionSuccess: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    isLoading: Boolean = false,
    error: String? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ============================================
    // ÉTATS DU FORMULAIRE
    // ============================================

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

    // ============================================
    // FONCTIONS DE VALIDATION
    // ============================================

    fun validateName(name: String, fieldName: String): Pair<Boolean, String> {
        return when {
            name.isBlank() -> false to "$fieldName requis"
            name.length < 2 -> false to "Au moins 2 caractères"
            !name.all { it.isLetter() || it.isWhitespace() || it == '-' } -> false to "Lettres uniquement"
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

    // ============================================
    // AFFICHAGE DES ERREURS
    // ============================================

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

    // ============================================
    // UI
    // ============================================

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
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo Learnity",
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Rejoignez-nous !",
                fontSize = 28.sp,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(32.dp))

            // PRÉNOM : Majuscule automatique + Touche Suivant
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = firstNameError.isNotEmpty(),
                supportingText = { if (firstNameError.isNotEmpty()) Text(firstNameError, color = MaterialTheme.colorScheme.error) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF), focusedLabelColor = Color(0xFF635BFF))
            )

            Spacer(Modifier.height(16.dp))

            // NOM : Majuscule automatique + Touche Suivant
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = lastNameError.isNotEmpty(),
                supportingText = { if (lastNameError.isNotEmpty()) Text(lastNameError, color = MaterialTheme.colorScheme.error) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF), focusedLabelColor = Color(0xFF635BFF))
            )

            Spacer(Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = emailError.isNotEmpty(),
                supportingText = { if (emailError.isNotEmpty()) Text(emailError, color = MaterialTheme.colorScheme.error) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF), focusedLabelColor = Color(0xFF635BFF))
            )

            Spacer(Modifier.height(16.dp))

            // MOT DE PASSE
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) Text(passwordError, color = MaterialTheme.colorScheme.error)
                    else Text("8 caractères min, 1 chiffre, 1 majuscule", color = Color.Gray, fontSize = 12.sp)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF), focusedLabelColor = Color(0xFF635BFF))
            )

            Spacer(Modifier.height(16.dp))

            // CONFIRMATION MOT DE PASSE : Touche OK/Done
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le mot de passe") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = { if (confirmPasswordError.isNotEmpty()) Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF), focusedLabelColor = Color(0xFF635BFF))
            )

            Spacer(Modifier.height(24.dp))

            // CHECKBOX CGU
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = acceptCGU, onCheckedChange = { acceptCGU = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF635BFF)))
                Text(text = "J'accepte les conditions générales d'utilisation", color = TextGray, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            // BOUTON INSCRIPTION
            Button(
                onClick = {
                    val (isFirstNameValid, firstNameErr) = validateName(firstName, "Prénom")
                    firstNameError = firstNameErr
                    val (isLastNameValid, lastNameErr) = validateName(lastName, "Nom")
                    lastNameError = lastNameErr
                    val isEmailValid = validateEmail(email)
                    val isPasswordValid = validatePassword(password)
                    val isConfirmValid = validateConfirmPassword(password, confirmPassword)

                    if (isFirstNameValid && isLastNameValid && isEmailValid && isPasswordValid && isConfirmValid) {
                        // On formate les noms proprement (Majuscule au début) avant l'envoi
                        val formattedFirstName = firstName.trim().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        val formattedLastName = lastName.trim().uppercase() // Souvent le nom est tout en majuscules ou juste la première lettre

                        onInscriptionSuccess(
                            email.trim(),
                            password.trim(),
                            formattedFirstName,
                            formattedLastName
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF), disabledContainerColor = Color.LightGray)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "S'inscrire", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}