package com.miage.learnity.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.ui.utils.*

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    photoUrl: String? = null // ⭐ On ajoute le paramètre photoUrl
) {
    val dimensions = rememberResponsiveDimensions()
    val context = LocalContext.current

    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route

    // ⭐ Logique de récupération de l'avatar
    val avatarResId = remember(photoUrl) {
        val photoName = if (photoUrl.isNullOrBlank()) "avatar_b1" else photoUrl
        try {
            val id = context.resources.getIdentifier(photoName, "drawable", context.packageName)
            if (id != 0) id else R.drawable.avatar_b1
        } catch (e: Exception) { R.drawable.avatar_b1 }
    }

    NavigationBar(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // 1️⃣ ACCUEIL
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_homepage_1),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = { Text("Accueil", fontSize = dimensions.bodySmall) },
            selected = route == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        // 2️⃣ ASSOCIATION
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_asso),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Asso", fontSize = dimensions.bodySmall) },
            selected = route == "association",
            onClick = {
                navController.navigate("association") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )

        // 3️⃣ COURS
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cours_1),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = { Text("Cours", fontSize = dimensions.bodySmall) },
            selected = route == "library",
            onClick = {
                navController.navigate("library") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            }
        )

        // 5️⃣ PROFIL (Avatar dynamique)
        val isProfileSelected = route == "profile"
        NavigationBarItem(
            icon = {
                // ⭐ On remplace l'icône Person par l'Image de l'avatar
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "Profil",
                    modifier = Modifier
                        .size(dimensions.iconSizeMedium)
                        .clip(CircleShape)
                        // On ajoute une petite bordure si l'onglet est sélectionné
                        .border(
                            width = if (isProfileSelected) 2.dp else 0.dp,
                            color = if (isProfileSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            },
            label = {
                Text(
                    "Profil",
                    fontSize = dimensions.bodySmall,
                    color = if (isProfileSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selected = isProfileSelected,
            onClick = {
                navController.navigate("profile") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            // Optionnel : Enlever l'indicateur (bulle) derrière l'image pour un look plus "social media"
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, showBackground = true)
@Preview(name = "Moyen (360dp)", widthDp = 360, showBackground = true)
@Preview(name = "Grand (410dp)", widthDp = 410, showBackground = true)
@Preview(name = "Tablette (600dp)", widthDp = 600, showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    MaterialTheme {
        BottomNavigationBar(
            navController = rememberNavController()
        )
    }
}