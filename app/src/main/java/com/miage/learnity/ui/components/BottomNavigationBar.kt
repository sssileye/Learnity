package com.miage.learnity.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.ui.utils.*

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val dimensions = rememberResponsiveDimensions()

    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route

    NavigationBar(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface  // ✅ CHANGÉ
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_homepage_1),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = {
                Text(
                    "Accueil",
                    fontSize = dimensions.bodySmall
                )
            },
            selected = route == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_asso),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium),
                    tint = Color.Unspecified
                )
            },
            label = {
                Text(
                    "Asso",
                    fontSize = dimensions.bodySmall
                )
            },
            selected = route == "association",
            onClick = {
                navController.navigate("association") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cours_1),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = {
                Text(
                    "Cours",
                    fontSize = dimensions.bodySmall
                )
            },
            selected = route == "library",
            onClick = {
                navController.navigate("library") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ranking),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = {
                Text(
                    "Ranking",
                    fontSize = dimensions.bodySmall
                )
            },
            selected = route == "ranking",
            onClick = {
                navController.navigate("ranking") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_1),
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            },
            label = {
                Text(
                    "Outils",
                    fontSize = dimensions.bodySmall
                )
            },
            selected = route == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
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