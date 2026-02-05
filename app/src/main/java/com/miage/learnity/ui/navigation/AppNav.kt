package com.miage.learnity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.model.AuthViewModel
import com.miage.learnity.ui.screens.*

@Composable
fun AppNav(vm: AuthViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by vm.state.collectAsState()

    // ✅ ÉTAPE 1 : Déterminer la destination de départ dynamiquement
    // Si state.user n'est pas nul au lancement, on va directement à la Homepage
    val startDest = if (state.user != null) Screen.Homepage.route else Screen.Authentication.route

    NavHost(
        navController = nav,
        startDestination = startDest // ✅ Applique la destination dynamique
    ) {
        composable(Screen.Authentication.route) {
            AuthScreen(
                onLoginClick = { nav.navigate(Screen.SignIn.route) },
                onSignupClick = { nav.navigate(Screen.Inscription.route) }
            )
        }

        composable(Screen.Inscription.route) {
            Inscription(
                onBackClick = { nav.popBackStack() },
                onInscriptionSuccess = { email, password, firstName, lastName ->
                    vm.signUp(email, password, firstName, lastName, 1.0)
                },
                isLoading = state.isLoading,
                error = state.error
            )

            LaunchedEffect(state.user) {
                if (state.user != null) {
                    goToHomepage(nav)
                }
            }
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onBackClick = { nav.popBackStack() },
                onSignIn = { email, password -> vm.signIn(email, password) },
                onForgotPassword = { /* TODO */ },
                onNavigateToSignUp = { nav.navigate(Screen.Inscription.route) },
                isLoading = state.isLoading,
                error = state.error
            )
            LaunchedEffect(state.user) {
                if (state.user != null) goToHomepage(nav)
            }
        }

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
    }
}

private fun goToHomepage(nav: NavHostController) {
    nav.navigate(Screen.Homepage.route) {
        // ✅ On nettoie TOUTE la pile pour éviter que le bouton "Retour" ne ramène au Login
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}