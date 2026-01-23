package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miage.learnity.ui.components.BottomNavigationBar
import com.miage.learnity.ui.components.TopNavigationBar
import com.miage.learnity.ui.screens.*
import com.miage.learnity.ui.screens.library.*
import com.miage.learnity.ui.screens.quiz.QuizScreen
import com.miage.learnity.ui.theme.ThemeViewModel // AJOUTER CET IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(
    themeViewModel: ThemeViewModel, // AJOUTER CE PARAMÈTRE
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    // État partagé pour le mode du quiz (Découverte ou Révision)
    var isDiscoveryMode by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute != null &&
            !currentRoute.contains("quiz") &&
            !currentRoute.contains("pdf")

    Scaffold(
        topBar = {
            if (showBars) {
                TopNavigationBar(
                    onProfileClick = { navController.navigate("profile") }
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
            // --- Destinations Standards ---

            composable("home") {
                HomeScreen(
                    navController = navController,
                    isDiscoveryMode = isDiscoveryMode
                )
            }

            composable("association") { AssociationScreen() }
            composable("ranking") { RankingScreen() }

            // ⭐ MISE À JOUR : Passage du ViewModel au SettingsScreen
            composable("settings") {
                SettingsScreen(themeViewModel = themeViewModel) // AJOUTER CE PARAMÈTRE
            }

            composable("profile") {
                ProfileScreen(
                    isDiscoveryMode = isDiscoveryMode,
                    onModeChange = { isDiscoveryMode = it },
                    onLogout = onLogout
                )
            }

            // --- Bibliothèque et Cours ---
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
                    onMegaQuizClick = {
                        navController.navigate("quiz/$courseId/ALL_CHAPTERS")
                    },
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

                ChapterContentScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    onCoursClick = { navController.navigate("pdf/$courseId/$chapterId/cours") },
                    onFdrClick = { navController.navigate("pdf/$courseId/$chapterId/fdr") },
                    onVideoClick = { /* Navigation Vidéo */ },
                    onStartQuiz = { navController.navigate("quiz/$courseId/$chapterId") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- PDF Viewer ---
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

            // --- Quiz Screen ---
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