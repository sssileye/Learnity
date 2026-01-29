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

/**
 * Écran d'accueil de LEARNITY - Version Vibrant avec WeeklyProgress
 *
 * Étape 2 Phase 1 : Intégration de la barre de progression hebdomadaire
 */
@Composable
fun HomeScreen(
    navController: NavController,
    isDiscoveryMode: Boolean = false,
    userViewModel: UserViewModel = viewModel()
) {
    val dimensions = rememberResponsiveDimensions()
    val repository = remember { QuizRepository() }
    val userUiState by userViewModel.uiState.collectAsState()

    var dailyScore by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var weeklyProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Récupérer le score du quiz du jour
    LaunchedEffect(Unit) {
        repository.getLastDailyQuizScore().onSuccess { score ->
            dailyScore = score
        }

        // ⭐ NOUVEAU : Récupérer la progression hebdomadaire
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

        // 📝 Quiz du Jour (avec progression hebdomadaire intégrée)
        DailyQuizCard(
            dimensions = dimensions,
            isDiscoveryMode = isDiscoveryMode,
            lastScore = dailyScore,
            weeklyProgress = weeklyProgress, // ⭐ NOUVEAU paramètre
            onAction = { isReview ->
                val modeId = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
                navController.navigate("quiz/GLOBAL/$modeId?isReviewMode=$isReview")
            }
        )

        // 💰 Dette Virtuelle (gradient orange-rouge ou vert)
        userUiState.profile?.let { profile ->
            VirtualDebtCard(
                debtAmount = profile.detteCumulee,
                monthsRemaining = 4,
                onPayClick = {
                    // TODO: Navigation vers écran de paiement/associations
                },
                dimensions = dimensions
            )
        }

        // ✨ Unity Points (gradient bleu vibrant)
        userUiState.profile?.let { profile ->
            UnityPointsCard(
                currentPoints = profile.unityPoints,
                nextDonationGoal = 2000,
                onViewImpactClick = {
                    // TODO: Navigation vers écran d'impact/associations
                },
                dimensions = dimensions
            )
        }

        // TODO Étape 3: Ajouter ici QuickActionsCard

        // Espace pour le bottom nav
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