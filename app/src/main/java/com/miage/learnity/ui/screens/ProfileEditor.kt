import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.ui.screens.ProfileViewModel


@Composable
fun ProfileEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // On récupère le profil depuis le StateFlow du ViewModel
    uiState.profile?.let { currentProfile ->
        ProfileEditor(
            profile = currentProfile,
            isLoading = uiState.isLoading,
            onCancel = onNavigateBack,
            onSave = { firstName, lastName, email, redevance ->
                // On appelle la fonction de ton ViewModel
                viewModel.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    redevance = redevance
                    // Note: Ton ViewModel actuel ne prend pas l'email,
                    // tu peux l'ajouter dans la signature de updateProfile si besoin
                )
                onNavigateBack() // Retour à l'écran précédent après sauvegarde
            }
        )
    }
}

@Composable
private fun ProfileEditor(
    profile: com.miage.learnity.data.UserProfile,
    onSave: (String, String, String, Double) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    // États (Logique Claude conservée)
    var firstName by remember { mutableStateOf(profile.firstName) }
    var lastName by remember { mutableStateOf(profile.lastName) }
    var email by remember { mutableStateOf(profile.email) }
    var redevance by remember { mutableStateOf(profile.redevanceSoutienUnitaire.toString()) }

    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var redevanceError by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(60.dp))

        // Titre stylisé
        Text(
            text = "Modifier mon profil",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
        )
        Text(
            text = "Mettez à jour vos informations personnelles",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
        )

        Spacer(Modifier.height(40.dp))

        // Champ Prénom
        CustomEditField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = if (it.isBlank()) "Prénom requis" else ""
            },
            label = "Prénom",
            icon = Icons.Default.Person,
            errorText = firstNameError
        )

        // Champ Nom
        CustomEditField(
            value = lastName,
            onValueChange = {
                lastName = it
                lastNameError = if (it.isBlank()) "Nom requis" else ""
            },
            label = "Nom",
            icon = Icons.Default.Person,
            errorText = lastNameError
        )

        // Champ Email
        CustomEditField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) "Email invalide" else ""
            },
            label = "Email",
            icon = Icons.Default.Email,
            errorText = emailError,
            keyboardType = KeyboardType.Email
        )

        // Champ Redevance
        CustomEditField(
            value = redevance,
            onValueChange = {
                redevance = it
                redevanceError = if (it.toDoubleOrNull() == null) "Montant invalide" else ""
            },
            label = "Redevance unitaire (€)",
            icon = Icons.Default.ShoppingCart, // Ou une icône de monnaie
            errorText = redevanceError,
            keyboardType = KeyboardType.Decimal
        )

        Spacer(Modifier.height(48.dp))

        // BOUTONS (Même logique, look amélioré)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text("Annuler", color = Color.Black)
            }

            Button(
                onClick = {
                    // Validation finale et onSave...
                    val redValue = redevance.toDoubleOrNull() ?: 0.0
                    onSave(firstName, lastName, email, redValue)
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                // Dans le bouton Sauvegarder
                enabled = !isLoading &&
                        firstNameError.isEmpty() &&
                        lastNameError.isEmpty() &&
                        emailError.isEmpty() &&
                        redevanceError.isEmpty() &&
                        firstName.isNotBlank() &&
                        lastName.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Enregistrer")
            }
        }
    }
}

@Composable
fun CustomEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    errorText: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF673AB7)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = errorText.isNotEmpty(),
            supportingText = { if (errorText.isNotEmpty()) Text(errorText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF673AB7),
                focusedLabelColor = Color(0xFF673AB7),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}