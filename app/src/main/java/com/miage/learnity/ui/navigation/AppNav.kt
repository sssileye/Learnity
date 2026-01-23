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
import com.miage.learnity.ui.theme.ThemeViewModel

@Composable
fun AppNav(
    vm: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val nav = rememberNavController()
    val state by vm.state.collectAsState()

    NavHost(
        navController = nav,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (state.user != null) {
                        nav.navigate(Screen.Homepage.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        nav.navigate(Screen.Authentication.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

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
                onForgotPassword = {  // ✅ CONNECTÉ
                    nav.navigate(Screen.ResetPassword.route)
                },
                onNavigateToSignUp = { nav.navigate(Screen.Inscription.route) },
                isLoading = state.isLoading,
                error = state.error
            )
            LaunchedEffect(state.user) {
                if (state.user != null) goToHomepage(nav)
            }
        }

        // ✅ NOUVELLE ROUTE - Reset Password
        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                onBackClick = { nav.popBackStack() },
                onResetPassword = { email ->
                    vm.resetPassword(email)
                },
                onResetSuccess = {
                    vm.clearResetPasswordSuccess()
                    nav.popBackStack()
                },
                isLoading = state.isLoading,
                error = state.error,
                success = state.resetPasswordSuccess
            )
        }

        composable(Screen.Homepage.route) {
            MainNav(
                // ✅ ON PASSE LE VIEWMODEL ICI POUR CORRIGER L'ERREUR
                themeViewModel = themeViewModel,
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