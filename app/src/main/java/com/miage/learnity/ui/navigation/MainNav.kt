package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miage.learnity.ui.components.BottomNavigationBar
import com.miage.learnity.ui.components.TopNavigationBar
import com.miage.learnity.ui.components.YouTubePlayer
import com.miage.learnity.ui.screens.*
import com.miage.learnity.ui.screens.library.*
import com.miage.learnity.ui.screens.quiz.QuizScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    var isDiscoveryMode by remember { mutableStateOf(false) }

    // ⭐ NOUVEAU : Récupérer le UserViewModel pour avoir accès au currentStreak
    val userViewModel: UserViewModel = viewModel()
    val userUiState by userViewModel.uiState.collectAsState()
    val currentStreak = userUiState.profile?.currentStreak ?: 0

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // On cache les barres pour le Quiz, le PDF, l'Éditeur de profil et la VIDÉO
    val showBars = currentRoute != null &&
            !currentRoute.contains("quiz") &&
            !currentRoute.contains("pdf") &&
            !currentRoute.contains("video") &&
            currentRoute != "profile_editor"

    Scaffold(
        topBar = {
            if (showBars) {
                TopNavigationBar(
                    currentStreak = currentStreak, // ⭐ NOUVEAU paramètre
                    onProfileClick = { navController.navigate("profile") },
                    onLogoClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(if (showBars) paddingValues else PaddingValues(0.dp))
        ) {
            composable("home") {
                HomeScreen(navController = navController, isDiscoveryMode = isDiscoveryMode)
            }

            composable("association") { AssociationScreen() }
            composable("ranking") { RankingScreen() }
            composable("settings") { SettingsScreen() }

            composable("profile") {
                ProfileScreen(
                    isDiscoveryMode = isDiscoveryMode,
                    onModeChange = { isDiscoveryMode = it },
                    onLogout = onLogout,
                    onEditClick = { navController.navigate("profile_editor") }
                )
            }

            composable("profile_editor") {
                ProfileEditorScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("library") {
                LibraryScreen(onCourseClick = { id -> navController.navigate("course/$id") })
            }

            composable(
                route = "course/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                CourseDetailScreen(
                    courseId = courseId,
                    onChapterClick = { cId, chapId -> navController.navigate("chapter/$cId/$chapId") },
                    onMegaQuizClick = { navController.navigate("quiz/$courseId/ALL_CHAPTERS") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- Contenu du Chapitre ---
            composable(
                route = "chapter/{courseId}/{chapterId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""

                val chapterViewModel: ChapterContentViewModel = viewModel()
                val chapterState by chapterViewModel.chapter.collectAsState()

                ChapterContentScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    viewModel = chapterViewModel,
                    onCoursClick = { navController.navigate("pdf/$courseId/$chapterId/cours") },
                    onFdrClick = { navController.navigate("pdf/$courseId/$chapterId/fdr") },
                    onVideoClick = {
                        chapterState?.videoUrl?.let { url ->
                            if (url.isNotBlank()) {
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                                navController.navigate("video/$encodedUrl")
                            }
                        }
                    },
                    onStartQuiz = { navController.navigate("quiz/$courseId/$chapterId") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Nouvelle Route : Lecteur Vidéo
            composable(
                route = "video/{videoUrl}",
                arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                YouTubePlayer(
                    videoUrl = videoUrl,
                    modifier = Modifier.fillMaxSize(),
                    onVideoEnd = { /* Optionnel : marquer comme vu */ }
                )
            }

            composable(
                route = "pdf/{courseId}/{chapterId}/{type}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: ""

                PdfViewerScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    type = type,
                    onMarkComplete = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "quiz/{courseId}/{chapterId}?isReviewMode={isReviewMode}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("isReviewMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                val isReviewMode = backStackEntry.arguments?.getBoolean("isReviewMode") ?: false

                QuizScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    isReviewMode = isReviewMode,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}