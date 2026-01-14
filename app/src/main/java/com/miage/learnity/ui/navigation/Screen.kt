package com.miage.learnity.ui.navigation

sealed class Screen (val route: String ){
    data object Authentication: Screen("Authentication Screen")
    data object Inscription: Screen("Page inscription")
    data object SignIn: Screen("Page de connexion")
    data object Homepage: Screen("Page d'accueil")
    data object Cours: Screen("Page de Cours")
    data object AssociationScreen: Screen("")
    data object RankingScreen: Screen("")
    data object SettingsScreen: Screen("")
    data object ProfileScreen: Screen("")


}