package com.miage.learnity.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R
import com.miage.learnity.data.Association

@Composable
fun AssociationCard(asso: Association) {
    val context = LocalContext.current

    // Récupère l'ID du logo stocké dans drawable via son nom String
    val imageResId = context.resources.getIdentifier(
        asso.logoName,
        "drawable",
        context.packageName
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Affichage du logo propre à l'association
            Image(
                painter = painterResource(id = if (imageResId != 0) imageResId else R.drawable.logo_android),
                contentDescription = null,
                modifier = Modifier.size(60.dp).padding(end = 12.dp),
                contentScale = ContentScale.Fit
            )

            // Infos : Nom + Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asso.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = asso.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            // Bouton Redirection Web
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(asso.websiteUrl))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Donner", fontSize = 12.sp)
            }
        }
    }
}