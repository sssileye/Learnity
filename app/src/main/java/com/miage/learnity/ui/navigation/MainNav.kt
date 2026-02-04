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
import com.google.firebase.auth.FirebaseAuth
import com.miage.learnity.ui.components.*
import com.miage.learnity.ui.screens.*
import com.miage.learnity.ui.screens.library.*
import com.miage.learnity.ui.screens.quiz.QuizScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNav(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    // ⭐ Initialisation de Firebase Auth pour l'accès au UID
    val auth = remember { FirebaseAuth.getInstance() }

    // Source de vérité globale pour le profil
    val userViewModel: UserViewModel = viewModel()
    val userUiState by userViewModel.uiState.collectAsState()

    // Données extraites
    val profile = userUiState.profile
    val currentStreak = profile?.currentStreak ?: 0
    val photoUrl = profile?.photoUrl

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Gestion de l'affichage des barres
    val showBars = currentRoute != null &&
            !currentRoute.contains("quiz") &&
            !currentRoute.contains("pdf") &&
            !currentRoute.contains("video") &&
            currentRoute != "profile_editor" &&
            currentRoute != "favorites" // On cache souvent les barres sur les sous-pages

    // États pour les dialogs d'aide
    var showHelpDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }
    var showQuizDuJourDialog by remember { mutableStateOf(false) }
    var showDetteDialog by remember { mutableStateOf(false) }
    var showUnityPointsDialog by remember { mutableStateOf(false) }
    var showTypesQuizDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showBars) {
                TopNavigationBar(
                    currentStreak = currentStreak,
                    onLogoClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onStreakClick = { showStreakDialog = true },
                    onHelpClick = { showHelpDialog = true }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(
                    navController = navController,
                    photoUrl = photoUrl
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(if (showBars) paddingValues else PaddingValues(0.dp))
        ) {
            // --- ACCUEIL ---
            composable("home") {
                HomeScreen(navController = navController, userViewModel = userViewModel)
            }

            composable("association") { AssociationScreen() }

            composable("settings") {
                SettingsScreen(
                    authViewModel = viewModel(),
                    onAccountDeleted = { onLogout() }
                )
            }

            // --- PROFIL & FAVORIS ---
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout,
                    onEditClick = { navController.navigate("profile_editor") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToLibrary = { navController.navigate("favorites") },
                    viewModel = userViewModel
                )
            }

            composable("favorites") {
                val favViewModel: LibraryFavoritesViewModel = viewModel()

                // ✅ Déclenchement automatique du chargement des favoris
                LaunchedEffect(Unit) {
                    auth.currentUser?.uid?.let { uid ->
                        favViewModel.loadFavorites(uid)
                    }
                }

                LibraryFavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToChapter = { cId, chapId ->
                        navController.navigate("chapter/$cId/$chapId")
                    },
                    onNavigateToCourse = { courseId ->
                        navController.navigate("course/$courseId")
                    },
                    viewModel = favViewModel
                )
            }

            composable("profile_editor") {
                ProfileEditorScreen(onNavigateBack = { navController.popBackStack() })
            }

            // --- BIBLIOTHÈQUE CLASSIQUE ET LECTEURS ---
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
                        chapterState?.video?.let { url ->
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

            // --- LECTEURS MÉDIAS ---
            composable(
                route = "video/{videoUrl}",
                arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                YouTubePlayer(
                    videoUrl = videoUrl,
                    modifier = Modifier.fillMaxSize(),
                    onVideoEnd = { /* ... */ }
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

            // --- QUIZ ---
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

    // --- LOGIQUE DES DIALOGS (Identique à ton code original) ---
    if (showStreakDialog) {
        val currentMultiplier = when {
            currentStreak >= 30 -> 2.0
            currentStreak >= 15 -> 1.5
            currentStreak >= 7 -> 1.2
            currentStreak >= 3 -> 1.1
            else -> 1.0
        }
        StreakHelpDialog(
            currentStreak = currentStreak,
            multiplier = currentMultiplier,
            onDismiss = { showStreakDialog = false }
        )
    }

    if (showHelpDialog) {
        HomeScreenHelpDialog(
            onQuizDuJourClick = { showHelpDialog = false; showQuizDuJourDialog = true },
            onDetteClick = { showHelpDialog = false; showDetteDialog = true },
            onUnityPointsClick = { showHelpDialog = false; showUnityPointsDialog = true },
            onTypesQuizClick = { showHelpDialog = false; showTypesQuizDialog = true },
            onDismiss = { showHelpDialog = false }
        )
    }
    if (showQuizDuJourDialog) QuizDuJourHelpDialog(onDismiss = { showQuizDuJourDialog = false })
    if (showDetteDialog) DetteVirtuelleHelpDialog(onDismiss = { showDetteDialog = false })
    if (showUnityPointsDialog) UnityPointsHelpDialog(onDismiss = { showUnityPointsDialog = false })
    if (showTypesQuizDialog) TypesQuizHelpDialog(onDismiss = { showTypesQuizDialog = false })
}