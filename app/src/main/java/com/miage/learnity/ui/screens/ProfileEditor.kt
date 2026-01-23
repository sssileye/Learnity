package com.miage.learnity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    // États pour les champs (À lier plus tard à ton ViewModel)
    var firstName by remember { mutableStateOf("Axel") }
    var lastName by remember { mutableStateOf("H") }
    var redevance by remember { mutableStateOf("0.10") }

    // État pour le dialogue de sécurité
    var showPasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Enregistrer", color = Color(0xFF673AB7), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- SECTION PHOTO ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { /* Action Galerie */ },
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.LightGray,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                // Badge caméra
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF673AB7),
                    modifier = Modifier.size(36.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- SECTION INFORMATIONS ---
            Text(
                text = "INFORMATIONS PERSONNELLES",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = redevance,
                onValueChange = { redevance = it },
                label = { Text("Redevance de soutien unitaire (€)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("€ ") }
            )

            Spacer(Modifier.height(32.dp))

            // --- SECTION SÉCURITÉ ---
            Text(
                text = "SÉCURITÉ",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
            )

            Card(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF673AB7))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Mot de passe", fontWeight = FontWeight.Bold)
                        Text("Modifier votre accès sécurisé", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // --- DIALOGUE MOT DE PASSE ---
    if (showPasswordDialog) {
        SecurityPasswordDialog(onDismiss = { showPasswordDialog = false })
    }
}

@Composable
fun SecurityPasswordDialog(onDismiss: () -> Unit) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le mot de passe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Pour votre sécurité, entrez votre ancien mot de passe avant d'en créer un nouveau.", fontSize = 13.sp)
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text("Ancien mot de passe") }
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("Nouveau mot de passe") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { /* Logique Firebase Reauth + Update */ onDismiss() }) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}