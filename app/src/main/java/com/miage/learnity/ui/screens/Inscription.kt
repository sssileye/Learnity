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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.*

import androidx.compose.ui.text.style.TextDecoration // Pour souligner le texte
import androidx.compose.foundation.rememberScrollState // Pour le défilement des CGU
import androidx.compose.foundation.verticalScroll // Pour rendre la popup scrollable
import androidx.compose.material3.AlertDialog // Pour la fenêtre contextuelle [cite: 20]

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

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    //var acceptCGU by remember { mutableStateOf(false) }

    var acceptCGU by remember { mutableStateOf(false) } // État de la case à cocher [cite: 11]
    var showCGUDialog by remember { mutableStateOf(false) } // État d'affichage de la popup [cite: 42]

    // états d'erreur
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    // Fonctions de validation
    fun validateName(name: String, fieldName: String): Pair<Boolean, String> {
        return when {
            name.isBlank() -> false to "$fieldName requis"
            name.length < 2 -> false to "Au moins 2 caractÃ¨res"
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
            Spacer(Modifier.height(dimensions.itemSpacing))

            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo Learnity",
                modifier = Modifier.size(dimensions.logoSize)
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            Text(
                text = "Rejoignez-nous !",
                fontSize = dimensions.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 2))

            // PRENOM
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
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = firstNameError.isNotEmpty(),
                supportingText = {
                    if (firstNameError.isNotEmpty()) {
                        Text(
                            text = firstNameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            // NOM
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
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = lastNameError.isNotEmpty(),
                supportingText = {
                    if (lastNameError.isNotEmpty()) {
                        Text(
                            text = lastNameError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError.isNotEmpty()) {
                        validateEmail(it)
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            // MOT DE PASSE
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError.isNotEmpty()) {
                        validatePassword(it)
                    }
                },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall
                        )
                    } else {
                        Text(
                            text = "8 caractères min, 1 chiffre, 1 majuscule",
                            color = Color.Gray,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Masquer" else "Afficher",
                            modifier = Modifier.size(dimensions.iconSizeMedium)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing))

            // CONFIRMATION MOT DE PASSE
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError.isNotEmpty()) {
                        validateConfirmPassword(password, it)
                    }
                },
                label = { Text("Confirmer le mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = {
                    if (confirmPasswordError.isNotEmpty()) {
                        Text(
                            text = confirmPasswordError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = dimensions.bodySmall
                        )
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = dimensions.bodyLarge
                ),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Masquer" else "Afficher",
                            modifier = Modifier.size(dimensions.iconSizeMedium)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF635BFF),
                    focusedLabelColor = Color(0xFF635BFF)
                )
            )

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

            // CHECKBOX CGU
            // --- BLOC CGU ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = acceptCGU,
                    onCheckedChange = { acceptCGU = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF635BFF))
                )

                // On utilise un TextButton pour rendre le texte cliquable
                TextButton(
                    onClick = { showCGUDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "J'accepte les conditions générales d'utilisation",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = dimensions.bodyMedium,
                        textDecoration = TextDecoration.Underline // ✅ Souligne le texte
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 2))

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

                    if (isFirstNameValid && isLastNameValid && isEmailValid &&
                        isPasswordValid && isConfirmValid) {
                        // Formatage des noms
                        val formattedFirstName = firstName.trim()
                            .split(" ")
                            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                        val formattedLastName = lastName.trim().uppercase()

                        onInscriptionSuccess(
                            email.trim(),
                            password.trim(),
                            formattedFirstName,
                            formattedLastName
                        )
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
                        text = "S'inscrire",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))
        }
    }
    if (showCGUDialog) {
        AlertDialog(
            onDismissRequest = { showCGUDialog = false },
            title = { Text("Conditions Générales d'Utilisation", fontWeight = FontWeight.Bold) },
            text = {
                // La colonne permet de scroller si le texte est trop long
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = """
                        Dernière mise à jour : 30/01/2026 [cite: 3]
                        
                        1. OBJET : Learnity est une plateforme éducative utilisant la ludification pour encourager l'apprentissage[cite: 5, 6].
                        
                        2. INSCRIPTION : L'accès nécessite un compte valide. L'application est destinée aux étudiants[cite: 11, 12].
                        
                        3. IMPACT SOLIDAIRE : L'utilisateur définit une "redevance de soutien unitaire" générant une "dette virtuelle" cumulée[cite: 16, 17, 20].
                        
                        4. DONNÉES (RGPD) : Nous collectons Email, Nom, Prénom et statistiques pour le suivi de l'impact solidaire[cite: 26, 27, 28].
                        
                        5. PROPRIÉTÉ : Tous les contenus sont la propriété exclusive de Learnity[cite: 22, 23].
                    """.trimIndent(),
                        fontSize = dimensions.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCGUDialog = false }) {
                    Text("Fermer", color = Color(0xFF635BFF), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
//@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
//@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
//@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun InscriptionPreview() {
    LearnityTheme {
        Inscription()
    }
}