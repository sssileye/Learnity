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
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(asso.logoName, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.itemSpacing / 2)
            .heightIn(min = dimensions.iconSizeLarge * 1.67f),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
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
                contentDescription = null,
                modifier = Modifier
                    .size(dimensions.iconSizeLarge * 1.25f)
                    .padding(end = dimensions.itemSpacing),
                contentScale = ContentScale.Fit
            )

            // Infos de l'association
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asso.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyLarge
                )
                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 3))
                Text(
                    text = asso.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = dimensions.bodySmall,
                    maxLines = 2
                )
            }

            // Bouton Donner
            Button(
                onClick = { onDonClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                modifier = Modifier.height(dimensions.buttonHeightSmall * 0.75f),
                contentPadding = PaddingValues(
                    horizontal = dimensions.itemSpacing,
                    vertical = dimensions.itemSpacing / 2
                )
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