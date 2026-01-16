package com.miage.learnity.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
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

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute by navController.currentBackStackEntryAsState();
    val route = currentRoute?.destination?.route

    NavigationBar {
        // ACCUEIL
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_homepage_1),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Accueil") },
            selected = route == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )


        // MON ASSO
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_asso),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.Unspecified
                )
            },
            label = { Text("Mon Asso") },
            selected = route == "association",
            onClick = {
                navController.navigate("association") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        // COURS
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cours_1),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Cours") },
            selected = route == "library",
            onClick = {
                navController.navigate("library") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        // CLASSEMENT
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ranking),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Ranking") },
            selected = route == "ranking",
            onClick = {
                navController.navigate("ranking") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        // PARAMÈTRES
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_1),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Outils") },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    BottomNavigationBar(
        navController = rememberNavController()
    )
}

