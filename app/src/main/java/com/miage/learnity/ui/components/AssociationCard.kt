package com.miage.learnity.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import com.miage.learnity.data.Association


@Composable
fun AssociationCard(asso: Association) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Affichage du logo depuis le dossier drawable
            Image(
                painter = painterResource(id = R.drawable.assoatena),
                contentDescription = "Logo ${asso.name}",
                modifier = Modifier
                    .size(50.dp) // Taille du logo
                    .padding(end = 12.dp)
                    .fillMaxWidth()
            )

            // 2. Nom de l'association
            Text(
                text = asso.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // 3. Bouton Donner
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(asso.websiteUrl))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Faire un Don")
            }
        }
    }
}