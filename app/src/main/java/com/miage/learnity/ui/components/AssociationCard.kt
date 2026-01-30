package com.miage.learnity.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.data.Association
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

/**
 * ═══════════════════════════════════════════════════════════════
 * 🎗️ ASSOCIATION CARD - VERSION COMPLÈTE
 * ═══════════════════════════════════════════════════════════════
 *
 * Card affichant une association avec :
 * ✅ Couleurs adaptées au thème (light/dark)
 * ✅ Description expandable avec bouton "Voir plus"
 * ✅ Animation smooth lors de l'expansion
 * ✅ Bordures visibles entre les cards
 * ✅ Logo cliquable → ouvre le site
 * ✅ Nom cliquable → ouvre le site
 */
@Composable
fun AssociationCardCustom(
    asso: Association,
    onDonClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(asso.logoname, "drawable", context.packageName)

    // ✅ État pour gérer l'expansion de la description
    var isExpanded by remember { mutableStateOf(false) }

    // ✅ Fonction pour ouvrir le site de l'association
    val openWebsite = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(asso.websiteUrl))
        context.startActivity(intent)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimensions.iconSizeLarge)
            .animateContentSize(), // ✅ Animation smooth
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface  // ✅ ADAPTATIF
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),  // ✅ Élévation visible
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant  // ✅ Bordure adaptative
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding)
        ) {
            // En-tête : Logo + Titre + Bouton
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ LOGO CLIQUABLE → Ouvre le site
                Image(
                    painter = painterResource(
                        id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery
                    ),
                    contentDescription = "Logo ${asso.name}",
                    modifier = Modifier
                        .size(dimensions.iconSizeLarge)
                        .padding(end = dimensions.itemSpacing / 2)
                        .clickable { openWebsite() },  // ✅ CLIC → Site
                    contentScale = ContentScale.Fit
                )

                // ✅ NOM CLIQUABLE → Ouvre le site
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openWebsite() }  // ✅ CLIC → Site
                ) {
                    Text(
                        text = asso.name,
                        color = MaterialTheme.colorScheme.onSurface,  // ✅ ADAPTATIF
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Bouton Donner
                Button(
                    onClick = onDonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary  // ✅ ADAPTATIF
                    ),
                    shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Donner",
                        color = MaterialTheme.colorScheme.onPrimary,  // ✅ ADAPTATIF
                        fontSize = dimensions.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ✅ DESCRIPTION PLIABLE avec bouton "Voir plus"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensions.itemSpacing / 2)
            ) {
                Text(
                    text = asso.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,  // ✅ ADAPTATIF
                    fontSize = dimensions.bodySmall,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,  // ✅ PLIABLE
                    overflow = TextOverflow.Ellipsis
                )

                // ✅ Bouton "Voir plus" / "Voir moins"
                if (asso.description.length > 100) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Voir moins" else "Voir plus",
                            fontSize = dimensions.bodySmall,
                            color = MaterialTheme.colorScheme.primary,  // ✅ ADAPTATIF
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 PREVIEWS
// ═══════════════════════════════════════════════════════════════

@Preview(name = "Card Light Mode", widthDp = 360)
@Preview(name = "Card Dark Mode", widthDp = 360, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AssociationCardPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssociationCardCustom(
                    asso = Association(
                        name = "Les Restos du Cœur",
                        websiteUrl = "https://www.restosducoeur.org",
                        logoname = "logo_restosducoeur",
                        description = "Association reconnue d'utilité publique qui lutte contre la précarité et l'exclusion sociale en France. Elle propose des aides alimentaires et sociales."
                    ),
                    onDonClick = {},
                    dimensions = rememberResponsiveDimensions()
                )

                AssociationCardCustom(
                    asso = Association(
                        name = "Linkee",
                        websiteUrl = "https://linkee.co",
                        logoname = "logo_linkee",
                        description = "Solution logistique et solidaire de lutte contre le gaspillage alimentaire."
                    ),
                    onDonClick = {},
                    dimensions = rememberResponsiveDimensions()
                )
            }
        }
    }
}
