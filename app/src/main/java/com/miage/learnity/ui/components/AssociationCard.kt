package com.miage.learnity.ui.components

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
import com.miage.learnity.data.Association

@Composable
fun AssociationCardCustom(
    asso: Association,
    onDonClick: () -> Unit // Ajout du paramètre pour gérer le clic
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(asso.logoName, "drawable", context.packageName)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).heightIn(min = 80.dp), // Définit une hauteur minimale mais permet de s'agrandir,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)) // Style transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                modifier = Modifier.size(60.dp).padding(end = 12.dp),
                contentScale = ContentScale.Fit
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = asso.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = asso.description, color = Color.Gray, fontSize = 12.sp)
            }

            Button(
                onClick = { onDonClick() }, // Déclenche la popup
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Donner", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}