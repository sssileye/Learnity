package com.miage.learnity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.ui.screens.AssociationScreen
import com.miage.learnity.ui.screens.HomeScreen
import com.miage.learnity.ui.screens.ProfileScreen
import com.miage.learnity.ui.screens.RankingScreen
import com.miage.learnity.ui.screens.SettingsScreen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.miage.learnity.ui.components.BottomNavigationBar
import com.miage.learnity.ui.components.TopNavigationBar
import com.miage.learnity.ui.screens.library.ChapterContentScreen
import com.miage.learnity.ui.screens.library.CourseDetailScreen
import com.miage.learnity.ui.screens.library.LibraryScreen
import com.miage.learnity.ui.screens.library.PdfViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopNavigationBar(
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("library") {
                LibraryScreen(
                    onCourseClick = { courseId ->
                        navController.navigate("course/$courseId")
                    }
                )
            }
            composable(
                route = "course/{courseId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable

                CourseDetailScreen(
                    courseId = courseId,
                    onChapterClick = { courseId, chapterId ->
                        navController.navigate("chapter/$courseId/$chapterId")
                        println("Clic sur chapitre: $courseId/$chapterId")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "chapter/{courseId}/{chapterId}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val chapterId =
                    backStackEntry.arguments?.getString("chapterId") ?: return@composable

                ChapterContentScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    onCoursClick = {
                        navController.navigate("pdf/$courseId/$chapterId/cours")
                    },
                    onFdrClick = {
                        navController.navigate("pdf/$courseId/$chapterId/fdr")
                    },
                    onVideoClick = {
                        navController.navigate("pdf/$courseId/$chapterId/video")
                    },
                    onStartQuiz = {
                        // TODO: Navigation vers quiz (Étape 5)
                        println("Start quiz: $courseId/$chapterId")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // ⭐ NOUVEAU - Route pour le viewer PDF/Vidéo
            composable(
                route = "pdf/{courseId}/{chapterId}/{type}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val chapterId =
                    backStackEntry.arguments?.getString("chapterId") ?: return@composable
                val type = backStackEntry.arguments?.getString("type") ?: return@composable

                PdfViewerScreen(
                    courseId = courseId,
                    chapterId = chapterId,
                    type = type,
                    onMarkComplete = {
                        // Retour vers ChapterContent après avoir marqué comme lu
                        navController.popBackStack()
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable("association") {
                AssociationScreen()
            }
            composable("ranking") {
                RankingScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout // Transmet le callback de déconnexion
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainNavPreview() {
    MaterialTheme {
        MainNav()
    }
}