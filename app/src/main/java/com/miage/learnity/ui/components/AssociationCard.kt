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
import com.miage.learnity.data.Association
import com.miage.learnity.ui.utils.ResponsiveDimensions

@Composable
fun AssociationCardCustom(
    asso: Association,
    onDonClick: () -> Unit,
    // ✅ On peut mettre une valeur par défaut ou s'assurer qu'il est passé depuis l'écran
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(asso.logoName, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Retrait du padding vertical ici car le LazyColumn s'en occupe déjà avec spacedBy
            .heightIn(min = dimensions.iconSizeLarge),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f) // Plus lisible sur ton fond bleu nuit
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(dimensions.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo de l'association
            Image(
                painter = painterResource(
                    id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery
                ),
                contentDescription = "Logo ${asso.name}",
                modifier = Modifier
                    .size(dimensions.iconSizeLarge)
                    .padding(end = dimensions.itemSpacing / 2),
                contentScale = ContentScale.Fit
            )

            // Infos de l'association
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asso.name,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyLarge
                )
                Text(
                    text = asso.description,
                    color = Color.DarkGray,
                    fontSize = dimensions.bodySmall,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Bouton Donner
            Button(
                onClick = onDonClick, // ✅ Correction : pas besoin de { onDonClick() }
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63) // Rose/Rouge vif
                ),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "Donner",
                    color = Color.White,
                    fontSize = dimensions.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}