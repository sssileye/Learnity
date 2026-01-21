package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // On cache les barres pendant le quiz et le PDF
    // Le "contains" permet de détecter "quiz/{courseId}/{chapterId}"
    // MAIS AUSSI notre futur "quiz_mega/{courseId}"
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
            // ... Destinations Standards (Identiques) ...
            composable("home") { HomeScreen() }
            composable("association") { AssociationScreen() }
            composable("ranking") { RankingScreen() }
            composable("settings") { SettingsScreen() }
            composable("profile") { ProfileScreen(onLogout = onLogout) }

            // --- Bibliothèque ---
            composable("library") {
                LibraryScreen(onCourseClick = { id -> navController.navigate("course/$id") })
            }

            // --- Détail du Cours (UE) ---
            composable(
                route = "course/{courseId}",
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""

                CourseDetailScreen(
                    courseId = courseId,
                    onChapterClick = { cId, chapId -> navController.navigate("chapter/$cId/$chapId") },
                    // ✅ AJOUT : Action pour le Mega Quiz
                    onMegaQuizClick = {
                        // On navigue vers l'écran quiz en passant "ALL_CHAPTERS" comme ID de chapitre
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

            // ... PDF Viewer (Identique) ...
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

            // --- Quiz Screen (Supporte Chapitre ET Mega Quiz) ---
            composable(
                route = "quiz/{courseId}/{chapterId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""

                QuizScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}