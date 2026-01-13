package com.miage.learnity.ui.navigation

sealed class Screen (val route: String ){
    data object HomeScreen: Screen("")
    data object LoginOptions: Screen("login_options")
    data object CoursesScreen: Screen("")
    data object AssociationScreen: Screen("")
    data object RankingScreen: Screen("")
    data object SettingsScreen: Screen("")
    data object ProfileScreen: Screen("")


}