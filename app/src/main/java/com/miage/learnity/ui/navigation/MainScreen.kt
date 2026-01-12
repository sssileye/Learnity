package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.miage.learnity.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("LEARNITY", color = Color(0xFF635BFF))
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_learnity),
                            contentDescription = "Mon Profil",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF635BFF)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                // ACCUEIL
                NavigationBarItem(
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.icon_learnity), contentDescription = null, modifier = Modifier.size(24.dp))
                    },
                    label = { Text("Accueil") },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )

                // COURS
                NavigationBarItem(
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.icon_learnity), contentDescription = null, modifier = Modifier.size(24.dp))
                    },
                    label = { Text("Cours") },
                    selected = false,
                    onClick = { navController.navigate("courses") }
                )

                // MON ASSO
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_learnity),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                    },
                    label = { Text("Mon Asso") },
                    selected = false,
                    onClick = { navController.navigate("association") }
                )

                // CLASSEMENT
                NavigationBarItem(
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.icon_learnity), contentDescription = null, modifier = Modifier.size(24.dp))
                    },
                    label = { Text("Ranking") },
                    selected = false,
                    onClick = { navController.navigate("ranking") }
                )

                // PARAMÈTRES
                NavigationBarItem(
                    icon = {
                        Icon(painter = painterResource(id = R.drawable.icon_learnity), contentDescription = null, modifier = Modifier.size(24.dp))
                    },
                    label = { Text("Outils") },
                    selected = false,
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { innerPadding ->
        // ATTENTION : Si ta fonction dans AppNav.kt s'appelle AppNav, change NavGraph par AppNav ici
        AppNav(navController = navController, paddingValues = innerPadding)
    }
}

