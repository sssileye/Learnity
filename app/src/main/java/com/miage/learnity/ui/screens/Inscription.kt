package com.miage.learnity.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onInscriptionSuccess: (String, String, String, String, Double) -> Unit = { _, _, _, _, _ -> },
    isLoading: Boolean = false,
    error: String? = null
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var redevance by remember { mutableStateOf("1.00") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptCGU by remember { mutableStateOf(false) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var redevanceError by remember { mutableStateOf("") }

    // ✨ NOUVEAU : État pour le dialog explicatif
    var showRedevanceDialog by remember { mutableStateOf(false) }

    fun validateFirstName(name: String): Boolean {
        return if (name.isBlank()) {
            firstNameError = "Prénom requis"
            false
        } else {
            firstNameError = ""
            true
        }
    }

    fun validateLastName(name: String): Boolean {
        return if (name.isBlank()) {
            lastNameError = "Nom requis"
            false
        } else {
            lastNameError = ""
            true
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

    fun validateRedevance(redevance: String): Boolean {
        val value = redevance.toDoubleOrNull()
        return when {
            value == null -> {
                redevanceError = "Montant invalide"
                false
            }
            value < 0.10 -> {
                redevanceError = "Minimum 0.10€"
                false
            }
            value > 100.0 -> {
                redevanceError = "Maximum 100€"
                false
            }
            else -> {
                redevanceError = ""
                true
            }
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
            redevance.isNotBlank() &&
            acceptCGU &&
            !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                ) {
                    TopAppBar(
                        title = { },
                        windowInsets = WindowInsets.statusBars,
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(35.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        )
                                        .padding(6.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
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
                Spacer(Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Rejoignez-nous",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Créez votre compte Learnity",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 📝 INFORMATIONS PERSONNELLES
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Informations personnelles",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Prénom
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                if (firstNameError.isNotEmpty()) validateFirstName(it)
                            },
                            label = { Text("Prénom") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = firstNameError.isNotEmpty(),
                            supportingText = {
                                if (firstNameError.isNotEmpty()) Text(firstNameError)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                focusedLabelColor = Color(0xFF673AB7),
                                focusedLeadingIconColor = Color(0xFF673AB7)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Nom
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                if (lastNameError.isNotEmpty()) validateLastName(it)
                            },
                            label = { Text("Nom") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = lastNameError.isNotEmpty(),
                            supportingText = {
                                if (lastNameError.isNotEmpty()) Text(lastNameError)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                focusedLabelColor = Color(0xFF673AB7),
                                focusedLeadingIconColor = Color(0xFF673AB7)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 🔐 SÉCURITÉ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sécurité",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError.isNotEmpty()) validateEmail(it)
                            },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = emailError.isNotEmpty(),
                            supportingText = {
                                if (emailError.isNotEmpty()) Text(emailError)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                focusedLabelColor = Color(0xFF673AB7),
                                focusedLeadingIconColor = Color(0xFF673AB7)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError.isNotEmpty()) validatePassword(it)
                            },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
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
                            shape = RoundedCornerShape(12.dp),
                            isError = passwordError.isNotEmpty(),
                            supportingText = {
                                if (passwordError.isNotEmpty()) Text(passwordError)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                focusedLabelColor = Color(0xFF673AB7),
                                focusedLeadingIconColor = Color(0xFF673AB7)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                if (confirmPasswordError.isNotEmpty()) {
                                    validateConfirmPassword(password, it)
                                }
                            },
                            label = { Text("Confirmer le mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
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
                            shape = RoundedCornerShape(12.dp),
                            isError = confirmPasswordError.isNotEmpty(),
                            supportingText = {
                                if (confirmPasswordError.isNotEmpty()) Text(confirmPasswordError)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                focusedLabelColor = Color(0xFF673AB7),
                                focusedLeadingIconColor = Color(0xFF673AB7)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 💰 REDEVANCE SOLIDAIRE (VERSION COMPACTE)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Engagement solidaire",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            // ✨ ICÔNE INFO CLIQUABLE
                            IconButton(
                                onClick = { showRedevanceDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "En savoir plus",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = redevance,
                            onValueChange = {
                                redevance = it
                                if (redevanceError.isNotEmpty()) validateRedevance(it)
                            },
                            label = { Text("Redevance par erreur (€)") },
                            leadingIcon = {
                                Icon(Icons.Default.EuroSymbol, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = redevanceError.isNotEmpty(),
                            supportingText = {
                                if (redevanceError.isNotEmpty()) {
                                    Text(redevanceError)
                                } else {
                                    Text(
                                        "Suggéré : 1.00€",
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50),
                                focusedLeadingIconColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Checkbox CGU
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = acceptCGU,
                            onCheckedChange = { acceptCGU = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF673AB7)
                            )
                        )
                        Text(
                            "J'accepte les conditions générales d'utilisation",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Sign Up Button
                Button(
                    onClick = {
                        val isFirstNameValid = validateFirstName(firstName)
                        val isLastNameValid = validateLastName(lastName)
                        val isEmailValid = validateEmail(email)
                        val isPasswordValid = validatePassword(password)
                        val isConfirmValid = validateConfirmPassword(password, confirmPassword)
                        val isRedevanceValid = validateRedevance(redevance)

                        if (isFirstNameValid && isLastNameValid && isEmailValid &&
                            isPasswordValid && isConfirmValid && isRedevanceValid) {
                            val redevanceValue = redevance.toDoubleOrNull() ?: 1.0
                            onInscriptionSuccess(
                                email.trim(),
                                password.trim(),
                                firstName.trim(),
                                lastName.trim(),
                                redevanceValue
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isButtonEnabled,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (isButtonEnabled) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF673AB7), Color(0xFF9C27B0))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Créer mon compte",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        // ✨ DIALOG EXPLICATIF REDEVANCE
        if (showRedevanceDialog) {
            RedevanceExplanationDialog(
                onDismiss = { showRedevanceDialog = false }
            )
        }
    }
}

// ✨ NOUVEAU COMPOSANT : Dialog Explicatif
@Composable
fun RedevanceExplanationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Engagement solidaire",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Comment ça marche ?",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )

                Text(
                    "À chaque mauvaise réponse dans un quiz, vous accumulez une petite dette virtuelle.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Text(
                    "Le montant que vous définissez ici sera reversé à l'association de votre choix.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Exemple",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "1.00€ × 5 erreurs = 5€ reversés",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    "💡 Vous pouvez modifier ce montant à tout moment dans votre profil.",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("J'ai compris")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// ✅ PREVIEWS
@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Composable
fun InscriptionScreenPreview() {
    MaterialTheme {
        Inscription()
    }
}

@Preview(name = "Dark Mode", showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InscriptionScreenDarkPreview() {
    MaterialTheme {
        Inscription()
    }
}

@Preview(name = "Dialog Preview", showBackground = true)
@Composable
fun RedevanceDialogPreview() {
    MaterialTheme {
        RedevanceExplanationDialog(onDismiss = {})
    }
}