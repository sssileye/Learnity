package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inscription(
    onBackClick: () -> Unit = {},
    onInscriptionSuccess: (String, String) -> Unit = { _, _ -> },
    isLoading: Boolean = false,
    error: String? = null
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptCGU by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

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
                passwordError = "Au moins 8 caractères"
                false
            }
            !password.any { it.isDigit() } -> {
                passwordError = "Au moins 1 chiffre"
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
            confirmPasswordError = "Les mots de passe diffèrent"
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

    val isButtonEnabled = email.isNotBlank() &&
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
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Logo réduit
            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Titre
            Text(
                text = "Rejoignez nous!",
                fontSize = 28.sp,
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) validateEmail(it)
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) Text(emailError)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError.isNotEmpty()) validatePassword(it)
                },
                label = { Text("Mot de passe") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) Text(passwordError)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Confirm Password Field
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
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = {
                    if (confirmPasswordError.isNotEmpty()) Text(confirmPasswordError)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(24.dp))

            // Checkbox CGU
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
                    "J'accepte les conditions générales d'utilisation",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            // Bouton Inscription
            Button(
                onClick = {
                    val isEmailValid = validateEmail(email)
                    val isPasswordValid = validatePassword(password)
                    val isConfirmValid = validateConfirmPassword(password, confirmPassword)

                    if (isEmailValid && isPasswordValid && isConfirmValid) {
                        onInscriptionSuccess(email.trim(), password.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF635BFF),
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "S'inscrire",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun InscriptionScreenPreview() {
//    LearnityTheme {
//        Inscription()
//    }
//}