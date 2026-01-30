package com.miage.learnity.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.data.Association
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * 🎗️ ASSOCIATION CARD - VERSION DÉPLIABLE
 * Au clic : Révèle la description, le pays et le bouton de don.
 */
@Composable
fun AssociationCardCustom(
    asso: Association,
    onDonClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val imageResId = remember(asso.logoname) {
        val id = context.resources.getIdentifier(asso.logoname, "drawable", context.packageName)
        if (id != 0) id else android.R.drawable.ic_menu_gallery
    }

    var isExpanded by remember { mutableStateOf(false) }

    // Animation de rotation de la flèche (0 vers 180 degrés)
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Animation fluide de la taille
            .clickable { isExpanded = !isExpanded }, // Clic sur toute la carte pour déplier
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(dimensions.cardPadding)) {

            // --- HEADER : LOGO + NOM + FLÈCHE (Toujours visible) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo dans un cercle gris clair
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nom de l'association
                Text(
                    text = asso.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Icône de déploiement
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationState), // Pivote avec l'animation
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // --- CONTENU DÉPLIABLE (Visible si isExpanded == true) ---
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Ligne de séparation subtile
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(12.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Description complète
                Text(
                    text = asso.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Boutons d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton Faire un don
                    Button(
                        onClick = onDonClick,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)), // Rose/Rouge don
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Favorite, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Donner", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 📱 PREVIEW
@Preview(showBackground = true)
@Composable
fun AssociationCardCustomPreview() {
    val sample = Association(
        name = "Les Restos du Cœur",
        logoname = "logo_restosducoeur",
        description = "Association reconnue d'utilité publique qui lutte contre la précarité sous toutes ses formes, notamment par l'aide alimentaire.",
        websiteUrl = "https://www.restosducoeur.org"
    )
    Box(modifier = Modifier.padding(16.dp)) {
        AssociationCardCustom(
            asso = sample,
            onDonClick = {},
            dimensions = rememberResponsiveDimensions()
        )
    }
}