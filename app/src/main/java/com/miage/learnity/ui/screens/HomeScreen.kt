package com.miage.learnity.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.utils.*

@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val dimensions = rememberResponsiveDimensions()

    // 1. On observe l'état global du UserViewModel
    val userUiState by userViewModel.uiState.collectAsState()

    // --- 🚀 GESTION DES NOTIFICATIONS ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            userViewModel.updateFcmToken()
        }
    }

    // 2. Logique de lancement
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            userViewModel.updateFcmToken()
        }

        userViewModel.refreshDailyStats()
        userViewModel.refreshProgressionStats()
    }

    // --- 🛡️ GESTION DE LA POPUP DE PÉNALITÉ ---
    // Si un message de pénalité est présent dans l'état, on affiche la boîte de dialogue
    userUiState.penaltyMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { userViewModel.dismissPenaltyPopup() },
            title = {
                Text(
                    text = "Rapport d'assiduité",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { userViewModel.dismissPenaltyPopup() }
                ) {
                    Text("J'ai compris")
                }
            }
        )
    }

    // 3. Source de vérité extraite de l'état UI
    val currentQuizMode = userUiState.profile?.quizMode ?: "DISCOVERY"
    val isDiscoveryMode = currentQuizMode == "DISCOVERY"

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

        // 🔍 Quiz du Jour
        DailyQuizCard(
            dimensions = dimensions,
            isDiscoveryMode = isDiscoveryMode,
            lastScore = userUiState.dailyScore,
            weeklyProgress = userUiState.weeklyProgress,
            onAction = { isReview ->
                navController.navigate("quiz/GLOBAL/$currentQuizMode?isReviewMode=$isReview")
            }
        )

        // 💰 Dette Virtuelle
        userUiState.profile?.let { profile ->
            VirtualDebtCard(
                debtAmount = profile.detteCumulee,
                onPayClick = { navController.navigate("association") },
                dimensions = dimensions
            )
        }

        // ✨ Unity Points
        userUiState.profile?.let { profile ->
            UnityPointsCard(
                currentPoints = profile.unityPoints,
                dimensions = dimensions
            )
        }

        Spacer(modifier = Modifier.height(dimensions.bottomNavHeight))
    }
}

@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(navController = rememberNavController())
    }
}