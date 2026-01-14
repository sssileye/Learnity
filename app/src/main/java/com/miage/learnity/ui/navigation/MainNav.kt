package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.CoursScreen
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.ProfileScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import androidx.compose.ui.tooling.preview.Preview
import com.miage.learnity.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

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
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("courses") {
                //CoursScreen()
            }
            composable("association") {
                AssociationScreen()
            }
            composable("ranking") {
                RankingScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout // ✅ Transmet le callback de déconnexion
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainNavPreview() {
    MaterialTheme {
        MainNav()
    }
}