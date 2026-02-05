package com.miage.learnity.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.utils.*

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val dimensions = rememberResponsiveDimensions()
    val userUiState by userViewModel.uiState.collectAsState()

    // On définit des états clairs pour la machine à états de l'UI
    val profile = userUiState.profile
    val isLoading = userUiState.isLoading || profile == null
    val isFirstLogin = profile?.isFirstLogin ?: false

    // --- 🚀 PERMISSIONS ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) userViewModel.updateFcmToken() }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            userViewModel.updateFcmToken()
        }
        userViewModel.refreshDailyStats()
        userViewModel.refreshProgressionStats()
    }

    // --- 🛡️ POPUP PÉNALITÉ ---
    userUiState.penaltyMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { userViewModel.dismissPenaltyPopup() },
            title = { Text("Rapport d'assiduité", fontWeight = FontWeight.Bold) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { userViewModel.dismissPenaltyPopup() }) { Text("J'ai compris") }
            }
        )
    }

    // --- 🎭 TRANSITION FLUIDE ---
    // Crossfade évite le chargement "sale" et les sauts de WindowInsets
    Crossfade(
        targetState = isLoading,
        animationSpec = tween(500),
        label = "home_fade"
    ) { loading ->
        if (loading) {
            // 1. Écran de chargement (Splash intégré)
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF635BFF))
            }
        } else {
            // 2. Le profil est là, on choisit entre Onboarding ou Home
            if (isFirstLogin) {
                OnboardingScreen(onFinished = { userViewModel.completeOnboarding(it) })
            } else {
                HomeContent(dimensions, userUiState, navController)
            }
        }
    }
}

@Composable
fun HomeContent(
    dimensions: ResponsiveDimensions,
    userUiState: UserUiState,
    navController: NavController
) {
    val currentQuizMode = userUiState.profile?.quizMode ?: "DISCOVERY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = dimensions.screenPaddingHorizontal)
            .responsiveMaxWidth(dimensions)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing * 1.3f)
    ) {
        Spacer(modifier = Modifier.height(dimensions.screenPaddingVertical))

        DailyQuizCard(
            dimensions = dimensions,
            isDiscoveryMode = currentQuizMode == "DISCOVERY",
            lastScore = userUiState.dailyScore,
            weeklyProgress = userUiState.weeklyProgress,
            onAction = { isReview ->
                navController.navigate("quiz/GLOBAL/$currentQuizMode?isReviewMode=$isReview")
            }
        )

        userUiState.profile?.let { profile ->
            VirtualDebtCard(
                debtAmount = profile.detteCumulee,
                onPayClick = { navController.navigate("association") },
                dimensions = dimensions
            )

            UnityPointsCard(
                currentPoints = profile.unityPoints,
                dimensions = dimensions
            )
        }

        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
    }
}
