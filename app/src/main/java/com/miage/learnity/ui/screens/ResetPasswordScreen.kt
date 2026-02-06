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
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onBackClick: () -> Unit = {},
    onResetPassword: (String) -> Unit = {},
    onResetSuccess: () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    success: Boolean = false
) {

    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
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


    LaunchedEffect(success) {
        if (success) {
            Toast.makeText(
                context,
                "Email de réinitialisation envoyÃ© ! Vérifiez votre boîte mail.",
                Toast.LENGTH_LONG
            ).show()
            onResetSuccess()
        }
    }

    val isButtonEnabled = email.isNotBlank() && !isLoading

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
            Spacer(Modifier.height(dimensions.screenPaddingVertical * 2))


            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo",
                modifier = Modifier.size(dimensions.logoSize)
            )

            Spacer(Modifier.height(dimensions.itemSpacing))


            Text(
                text = "Mot de passe oubliÃ© ?",
                fontSize = dimensions.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(dimensions.itemSpacing))


            Text(
                text = "Entrez votre adresse email et nous vous enverrons un lien pour réinitialiser votre mot de passe.",
                fontSize = dimensions.bodyMedium,  // … 14.ssp()
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = dimensions.itemSpacing)
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

            Spacer(Modifier.height(dimensions.itemSpacing * 2))


            Button(
                onClick = {
                    val isEmailValid = validateEmail(email)
                    if (isEmailValid) {
                        onResetPassword(email.trim())
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
                        "Envoyer le lien",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))


            TextButton(onClick = onBackClick) {
                Text(
                    text = "Retour à la connexion",
                    color = Color(0xFF635BFF),
                    fontWeight = FontWeight.Medium,
                    fontSize = dimensions.bodyMedium
                )
            }
        }
    }
}


@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun ResetPasswordScreenPreview() {
    LearnityTheme {
        ResetPasswordScreen()
    }
}