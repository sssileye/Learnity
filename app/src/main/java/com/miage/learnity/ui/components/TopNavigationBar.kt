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
    val dimensions = rememberResponsiveDimensions()

    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,  // ✅ CHANGÉ
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenPaddingHorizontal,
                    vertical = dimensions.itemSpacing
                )
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(onClick = onLogoClick)
                    .padding(dimensions.itemSpacing / 2)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_learnity),
                    contentDescription = "Logo Learnity",
                    modifier = Modifier.size(dimensions.iconSizeLarge),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(dimensions.itemSpacing / 2))
                Text(
                    text = "LEARNITY",
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary  // ✅ CHANGÉ
                )
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(dimensions.iconSizeLarge)
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.iconSizeLarge)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,  // ✅ CHANGÉ
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,  // ✅ CHANGÉ
                        modifier = Modifier.size(dimensions.iconSizeMedium)
                    )
                }
            }
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320)
@Preview(name = "Moyen (360dp)", widthDp = 360)
@Preview(name = "Grand (410dp)", widthDp = 410)
@Preview(name = "Tablette (600dp)", widthDp = 600)
@Composable
fun TopNavigationBarPreview() {
    MaterialTheme {
        TopNavigationBar(
            onProfileClick = { },
            onLogoClick = { }
        )
    }
}