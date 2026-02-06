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
import androidx.compose.material3.MaterialTheme
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Spacer(Modifier.height(dimensions.screenPaddingVertical))


            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo",
                modifier = Modifier.size(dimensions.logoSize)
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            Text(
                text = "Connexion",
                fontSize = dimensions.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 2.5f))


            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) validateEmail(it)
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))


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
                            modifier = Modifier.size(dimensions.iconSizeMedium)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensions.itemSpacing / 2)
            ) {
                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Mot de passe oublié ?",
                        color = Color(0xFF635BFF),
                        fontWeight = FontWeight.Medium,
                        fontSize = dimensions.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 2))


            Button(
                onClick = {
                    val isEmailValid = validateEmail(email)
                    if (isEmailValid) {
                        onSignIn(email.trim(), password.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF635BFF),
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                } else {
                    Text(
                        "Se connecter",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

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
                    fontSize = dimensions.bodyMedium
                ),
                modifier = Modifier.padding(bottom = dimensions.itemSpacing * 1.5f)
            )
        }
    }
}


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