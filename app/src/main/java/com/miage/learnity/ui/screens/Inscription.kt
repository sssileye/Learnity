package com.miage.learnity.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inscription(
    onBackClick: () -> Unit,
    onInscriptionSuccess: () -> Unit
) {
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var nomPrenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptCGU by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var nomPrenomError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    fun validateForm(): Boolean {
        var isValid = true
        if (nomPrenom.isBlank()) { nomPrenomError = "Nom & Prénom obligatoires"; isValid = false } else nomPrenomError = ""

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!email.matches(emailRegex)) { emailError = "Format d'email invalide"; isValid = false } else emailError = ""

        if (password.length < 8) { passwordError = "Au moins 8 caractères"; isValid = false }
        else if (!password.any { it.isDigit() }) { passwordError = "Au moins 1 chiffre"; isValid = false }
        else passwordError = ""

        if (password != confirmPassword) { confirmPasswordError = "Les mots de passe diffèrent"; isValid = false } else confirmPasswordError = ""

        return isValid
    }

    val isButtonEnabled = nomPrenom.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() &&
            acceptCGU && !isLoading

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        // Décorations radiales

        Scaffold(
            topBar = {
                // La Box définit le fond dégradé pour toute la zone de la TopAppBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Gray1,             // Part du gris en haut
                                    BackgroundColor    // Vers la couleur de fond en bas
                                )
                            )
                        )
                ) {
                    TopAppBar(
                        title = { },
                        // Garde les insets pour que le dégradé commence bien tout en haut (sous l'heure)
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
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(35.dp)
                                        .background(
                                            color = Gray1,
                                            shape = CircleShape
                                        )
                                        // Ajout d'un petit padding pour que la flèche ne touche pas les bords du cercle
                                        .padding(6.dp)
                                )
                            }
                        },
                        // On force le container à être transparent pour voir le Brush de la Box
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            containerColor = Color.Transparent

        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier=Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo Learnity",
                    modifier = Modifier.size(140.dp), // Ajusté à une taille raisonnable
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier=Modifier.height(16.dp))
                Text("Rejoignez Learnity", fontSize = 26.sp, color = TextGray, fontWeight = FontWeight.ExtraBold ,modifier = Modifier.padding(bottom = 32.dp))

                // Champs de saisie
                OutlinedTextField(
                    value = nomPrenom, onValueChange = { nomPrenom = it },
                    label = { Text("Nom et Prénom") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), isError = nomPrenomError.isNotEmpty(),
                    supportingText = { if (nomPrenomError.isNotEmpty()) Text(nomPrenomError) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email étudiant") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp), isError = emailError.isNotEmpty(),
                    supportingText = { if (emailError.isNotEmpty()) Text(emailError) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Mot de passe") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(16.dp), isError = passwordError.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it },
                    label = { Text("Confirmer le mot de passe") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    shape = RoundedCornerShape(16.dp), isError = confirmPasswordError.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = acceptCGU, onCheckedChange = { acceptCGU = it })
                    Text("J'accepte les CGU", color = TextGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton avec Firebase
                Button(
                    onClick = {
                        if (validateForm()&& auth != null) {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Succès !", Toast.LENGTH_SHORT).show()
                                        onInscriptionSuccess()
                                    } else {
                                        Toast.makeText(context, task.exception?.localizedMessage ?: "Erreur", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isButtonEnabled,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, // Fond quand activé
                        contentColor = Color.White,        // Couleur du texte par défaut
                        disabledContainerColor = Color.LightGray, // Fond quand isLoading est vrai
                        disabledContentColor = Color.DarkGray     // Texte quand isLoading est vrai
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = if (isButtonEnabled) Brush.horizontalGradient(listOf(GradientPurple2, GradientPurple1))
                            else Brush.horizontalGradient(listOf(GradientBlue1, GradientBlue2)),
                            shape = RoundedCornerShape(28.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("S'inscrire", color = Color.White, fontWeight = FontWeight.Bold)
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
    LearnityTheme {
        Inscription(
            onBackClick = {},
            onInscriptionSuccess = {}
        )
    }
}