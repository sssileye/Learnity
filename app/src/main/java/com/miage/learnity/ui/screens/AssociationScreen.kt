package com.miage.learnity.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationScreen() {
    // liste des assos pour l'instant mais qui est amené à etre dans le back
    val associations = listOf(
        "Fédération ATENA", "Le comptoir d'ALIENOR", "Restaurant du coeur","Epicerie solidaire")
    // Variable
    var expanded by remember { mutableStateOf(false) }

    // variable pour retenir l'association séléctionnée
    var selectedAssociation by remember { mutableStateOf(associations[0]) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) //Marge interne tout autour
            .background(Color(0xFFF5F7FA)) // Fond gris clair
    ) {
        //Text(text = "👥 Mon Association : Vie étudiante")

        //On laisse un espace vide de 16 pixels
        Spacer(modifier = Modifier.height(16.dp))

        // Une carte pour afficher le solde de points
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Tes points disponibles", color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    text = "1540 pts",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "👥 Choisir une association : ", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        //conteneur du menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }, // ouvre et ferme au clic
            modifier = Modifier.fillMaxWidth()
        ) {
            // Le champ de texte qui affiche le choix actuel
            OutlinedTextField(
                value = selectedAssociation,
                onValueChange = {},
                readOnly = true, // L'utilisateur ne peut pas écrire dedans
                label = { Text("Associations partenaires") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            // Ce qui apparaît quand on clique (la liste réelle)
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                associations.forEach { asso ->
                    DropdownMenuItem(
                        text = { Text(text = asso) },
                        onClick = {
                            selectedAssociation = asso // On change l'association choisie
                            expanded = false    // On ferme le menu
                        }
                    )
                }
            }
        }

    }
}
@Preview(showBackground = true)
@Composable
fun AssociationScreenPreview() {
    AssociationScreen()
}


