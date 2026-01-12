package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Import de tes écrans (vérifie bien que les noms de packages correspondent)
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.CoursScreen
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import com.miage.learnity.ui.screens.ProfileScreen

@Composable
fun AppNav(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues) // Important pour ne pas être sous les barres
    ) {
        composable("home") { HomeScreen() }
        composable("courses") { CoursScreen() }
        composable("association") { AssociationScreen() }
        composable("ranking") { RankingScreen() }
        composable("settings") { SettingsScreen() }
        composable("profile") { ProfileScreen() }
    }
}