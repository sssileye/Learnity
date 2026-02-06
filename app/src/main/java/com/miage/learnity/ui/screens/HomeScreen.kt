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


    val userUiState by userViewModel.uiState.collectAsState()


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            userViewModel.updateFcmToken()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            userViewModel.updateFcmToken()
        }

        userViewModel.refreshDailyStats()
        userViewModel.refreshProgressionStats()
    }



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


        DailyQuizCard(
            dimensions = dimensions,
            isDiscoveryMode = isDiscoveryMode,
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
        }


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