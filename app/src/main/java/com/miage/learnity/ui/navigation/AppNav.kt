package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun AppNav(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues) // Important pour ne pas être sous les barres
    ) {
        composable("home") { Screen.HomeScreen.route }
        composable("courses") { Screen.CoursesScreen.route}
        composable("association") { Screen.AssociationScreen.route }
        composable("ranking") { Screen.RankingScreen.route }
        composable("settings") { Screen.SettingsScreen.route }
        composable("profile") { Screen.ProfileScreen.route }
    }
}