package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authRoutes = listOf("auth", "inscription")
    val showBars = currentRoute !in authRoutes

    Scaffold(
        topBar = {
            if (showBars) {
                CenterAlignedTopAppBar(
                    title = { Text("LEARNITY", color = Color(0xFF635BFF)) },
                    actions = {
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_learnity),
                                contentDescription = "Profil",
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF635BFF)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", "Accueil", R.drawable.homepage_3),
                        Triple("courses", "Cours", R.drawable.cours_1),
                        Triple("association", "Mon Asso", R.drawable.asso),
                        Triple("ranking", "Ranking", R.drawable.ranking),
                        Triple("settings", "Outils", R.drawable.settings_1)
                    )

                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(painterResource(icon), null, Modifier.size(24.dp)) },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNav(navController = navController, paddingValues = innerPadding)
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        // On force la Preview sur l'écran Auth pour vérifier le plein écran
        MainScreen(navController = navController)
    }
}