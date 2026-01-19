package com.miage.learnity.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import com.miage.learnity.data.Association


@Composable
fun AssociationCard(asso: Association) {
    val context = LocalContext.current

    // On cherche l'ID du drawable qui porte le nom écrit dans Firebase
    val imageResId = context.resources.getIdentifier(
        asso.logoName,    // ex: "logo_amiage"
        "drawable",       // on cherche dans le dossier drawable
        context.packageName
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Affichage du logo PROPRE à l'association
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Logo ${asso.name}",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Si l'image n'est pas trouvée (erreur de nom dans Firebase),
                // on affiche un carré gris vide pour ne pas faire bugger l'app
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.LightGray))
            }

            Text(
                text = asso.name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { /* Ton code Intent pour le site */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Donner")
            }
        }
    }
}