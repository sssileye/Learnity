package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onBackClick: () -> Unit = {},
    onSignIn: (String, String) -> Unit = { _, _ -> },
    onForgotPassword: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null
) {
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }

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

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val isButtonEnabled = email.isNotBlank() &&
            password.isNotBlank() &&
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
                .responsiveMaxWidth(dimensions)  // ✅ Limite largeur sur tablettes
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.screenPaddingVertical))  // ✅ Responsive

            // Logo réduit - RESPONSIVE
            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo",
                modifier = Modifier.size(dimensions.logoSize)  // ✅ 100.sdp()
            )

            Spacer(Modifier.height(dimensions.itemSpacing))  // ✅ Responsive

            // Titre - RESPONSIVE
            Text(
                text = "Connexion",
                fontSize = dimensions.titleLarge,  // ✅ 28.ssp()
                color = TextDark,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 2.5f))  // ✅ Responsive

            // Email Field - RESPONSIVE
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

            // Password Field - RESPONSIVE
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            // Mot de passe oublié - RESPONSIVE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensions.itemSpacing / 2)  // ✅ Responsive
            ) {
                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Mot de passe oublié ?",
                        color = Color(0xFF635BFF),
                        fontWeight = FontWeight.Medium,
                        fontSize = dimensions.bodyMedium  // ✅ 14.ssp()
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 2))  // ✅ Responsive

            // Bouton Connexion - RESPONSIVE
            Button(
                onClick = {
                    val isEmailValid = validateEmail(email)
                    if (isEmailValid) {
                        onSignIn(email.trim(), password.trim())
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
                        "Se connecter",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge  // ✅ 16.ssp()
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))  // ✅ Responsive

            // Lien vers inscription - RESPONSIVE
            val annotated = buildAnnotatedString {
                append("Pas encore de compte ? ")
                pushStringAnnotation(tag = "signup", annotation = "signup")
                withStyle(
                    SpanStyle(
                        color = Color(0xFF635BFF),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("S'inscrire")
                }
                pop()
            }
            ClickableText(
                text = annotated,
                onClick = { offset ->
                    annotated.getStringAnnotations(
                        tag = "signup",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        onNavigateToSignUp()
                    }
                },
                style = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyMedium  // ✅ 14.ssp()
                ),
                modifier = Modifier.padding(bottom = dimensions.itemSpacing * 1.5f)  // ✅ Responsive
            )
        }
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    LearnityTheme {
        SignInScreen()
    }
}