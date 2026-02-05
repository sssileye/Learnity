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

    NavHost(
        navController = nav,
        startDestination = Screen.Authentication.route
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
                onInscriptionSuccess = { email, password, firstName, lastName, redevance ->
                    // ✅ CORRIGÉ : 5 paramètres (email, password, firstName, lastName, redevance)
                    vm.signUp(email, password, firstName, lastName, redevance)
                },
                isLoading = state.isLoading,
                error = state.error
            )

            LaunchedEffect(state.user) {
                if (state.user != null) {
                    nav.navigate(Screen.Homepage.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}