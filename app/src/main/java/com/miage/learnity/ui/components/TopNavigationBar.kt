package com.miage.learnity.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miage.learnity.R
import androidx.compose.foundation.layout.statusBarsPadding
import com.miage.learnity.ui.utils.*

@Composable
fun TopNavigationBar(
    onProfileClick: () -> Unit,
    onLogoClick: () -> Unit = {}
) {
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()

    Surface(
        shadowElevation = 4.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenPaddingHorizontal,  // ✅ Responsive
                    vertical = dimensions.itemSpacing  // ✅ Responsive (12dp adaptatif)
                )
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Logo Learnity CLIQUABLE - RESPONSIVE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onLogoClick)
                    .padding(dimensions.itemSpacing / 2)  // ✅ Zone de clic responsive
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo Learnity",
                    modifier = Modifier.size(dimensions.iconSizeLarge),  // ✅ 48.sdp() (était 32.dp)
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(dimensions.itemSpacing / 2))  // ✅ Responsive
                Text(
                    text = "LEARNITY",
                    fontSize = dimensions.titleMedium,  // ✅ 20.ssp() (était 18.sp)
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF635BFF)
                )
            }

            // Bouton Profil - RESPONSIVE
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(dimensions.iconSizeLarge)  // ✅ 48.sdp() (était 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.iconSizeLarge)  // ✅ Responsive
                        .background(
                            color = Color(0xFFE8E0FF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = Color(0xFF635BFF),
                        modifier = Modifier.size(dimensions.iconSizeMedium)  // ✅ 24.sdp() (était 18.dp)
                    )
                }
            }
        }
    }
}

// ✅ PREVIEWS MULTI-TAILLES
@Preview(name = "Petit (320dp)", widthDp = 320)
@Preview(name = "Moyen (360dp)", widthDp = 360)
@Preview(name = "Grand (410dp)", widthDp = 410)
@Preview(name = "Tablette (600dp)", widthDp = 600)
@Composable
fun TopNavigationBarPreview() {
    TopNavigationBar(
        onProfileClick = { },
        onLogoClick = { }
    )
}