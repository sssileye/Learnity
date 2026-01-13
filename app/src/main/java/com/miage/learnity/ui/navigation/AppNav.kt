package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.AuthScreen
import com.miage.learnity.ui.screens.CoursScreen
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.ProfileScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import com.miage.learnity.ui.screens.auth.Inscription


@Composable
fun AppNav(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = Modifier.padding(paddingValues) // Important pour ne pas être sous les barres
    ) {
        // --- Écrans d'Authentification ---
        composable("auth") {
            AuthScreen(
                onLoginClick = { /* navController.navigate("login") */ },
                onSignupClick = { navController.navigate("inscription") }
            )
        }

        composable("inscription") {
            Inscription(
                onBackClick = { navController.popBackStack() },
                onInscriptionSuccess = {
                    // Une fois inscrit, on va vers le Home
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true } // Empêche de revenir en arrière vers l'auth
                    }
                }
            )
        }

        composable("home") { HomeScreen() }
        composable("courses") { CoursScreen() }
        composable("association") { AssociationScreen() }
        composable("ranking") { RankingScreen() }
        composable("settings") { SettingsScreen() }
        composable("profile") { ProfileScreen() }
    }
}