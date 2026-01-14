package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.model.AuthViewModel
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.AuthScreen
import com.miage.learnity.ui.screens.CoursScreen
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.ProfileScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import com.miage.learnity.ui.screens.SignInScreen
import com.miage.learnity.ui.screens.auth.Inscription


@Composable
fun AppNav(vm: AuthViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by vm.state.collectAsState()

    NavHost(navController = nav, startDestination = Screen.Authentication.route) {
        composable(Screen.Authentication.route) {
            AuthScreen(
                onLoginClick = { nav.navigate(Screen.SignIn.route) },
                onSignupClick = { nav.navigate(Screen.Inscription.route) }
            )
        }
        composable(Screen.Inscription.route) {
            Inscription(
                onBackClick = { nav.popBackStack() },
                onInscriptionSuccess = { email, password -> vm.signUp(email, password) },
                isLoading = state.isLoading,
                error = state.error
            )
            LaunchedEffect(state.user) {
                if (state.user != null) goToHomepage(nav)
            }
        }
        composable(Screen.SignIn.route) {
            SignInScreen(
                onBackClick = { nav.popBackStack() },
                onSignIn = { email, password -> vm.signIn(email, password) },
                onForgotPassword = { /* TODO: Implémenter mot de passe oublié */ },
                onNavigateToSignUp = { nav.navigate(Screen.Inscription.route) },
                isLoading = state.isLoading,
                error = state.error
            )
            LaunchedEffect(state.user) {
                if (state.user != null) goToHomepage(nav)
            }
        }
        // Homepage principale (après authentification)
        composable(Screen.Homepage.route) {
            MainNav(
                onLogout = {
                    vm.signOut()
                    nav.navigate(Screen.Authentication.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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

private fun goToHomepage(nav: NavHostController) {
    nav.navigate(Screen.Homepage.route) {
        popUpTo(0) { inclusive = true } // vide toute la stack
        launchSingleTop = true
    }
}