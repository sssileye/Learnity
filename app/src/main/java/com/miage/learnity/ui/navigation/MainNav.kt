package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.R
import com.miage.learnity.ui.components.BottomNavigationBar
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.ProfileScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import com.miage.learnity.ui.theme.LearnityTheme

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
            BottomNavigationBar(navController)
        }
    ) {

            paddingValues ->
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

                )
            }
        }

    }
}


// --- SECTION PREVIEW ---
// Cette fonction permet de voir ton interface sans lancer l'émulateur
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    LearnityTheme {
        // Dans une Preview, on crée un NavController "fictif"
        val navController = rememberNavController()
        MainScreen(navController = navController)
    }
}