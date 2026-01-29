package com.miage.learnity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.utils.*

@Composable
fun HomeScreen(
    navController: NavController,
    // ⭐ Suppression du paramètre isDiscoveryMode : on utilise le ViewModel
    userViewModel: UserViewModel = viewModel()
) {
    val dimensions = rememberResponsiveDimensions()
    val repository = remember { QuizRepository() }

    // 1. On observe l'état du profil (qui contient quizMode)
    val userUiState by userViewModel.uiState.collectAsState()

    // 2. ⭐ Source de vérité dynamique
    val currentQuizMode = userUiState.profile?.quizMode ?: "DISCOVERY"
    val isDiscoveryMode = currentQuizMode == "DISCOVERY"

    var dailyScore by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var weeklyProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Récupérer les scores et progression
    LaunchedEffect(Unit) {
        repository.getLastDailyQuizScore().onSuccess { score ->
            dailyScore = score
        }
        repository.getWeeklyProgress(goalPerWeek = 4).onSuccess { progress ->
            weeklyProgress = progress
        }
    }

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

        // 📝 Quiz du Jour (Synchronisé en temps réel)
        DailyQuizCard(
            dimensions = dimensions,
            isDiscoveryMode = isDiscoveryMode, // Utilise la valeur dynamique
            lastScore = dailyScore,
            weeklyProgress = weeklyProgress,
            onAction = { isReview ->
                // Utilise le mode extrait du profil pour la navigation
                navController.navigate("quiz/GLOBAL/$currentQuizMode?isReviewMode=$isReview")
            }
        )

        // 💰 Dette Virtuelle
        userUiState.profile?.let { profile ->
            VirtualDebtCard(
                debtAmount = profile.detteCumulee,
                monthsRemaining = 4,
                onPayClick = { /* Navigation vers paiement */ },
                dimensions = dimensions
            )
        }

        // ✨ Unity Points
        userUiState.profile?.let { profile ->
            UnityPointsCard(
                currentPoints = profile.unityPoints,
                nextDonationGoal = 2000,
                onViewImpactClick = { /* Navigation vers impact */ },
                dimensions = dimensions
            )
        }

        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
    }
}


@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Composable
fun HomeScreenWithWeeklyProgressPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}