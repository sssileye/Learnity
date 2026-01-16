package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.data.mock.MockData
import com.miage.learnity.ui.theme.LearnityTheme

/**
 * Écran de détail d'un cours : affiche les chapitres
 *
 * @param courseId ID du cours à afficher
 * @param viewModel ViewModel qui gère les données
 * @param onChapterClick Callback appelé quand l'utilisateur clique sur un chapitre
 * @param onBackClick Callback pour retourner à l'écran précédent
 */
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel = viewModel(),
    onChapterClick: (String, String) -> Unit,  // (courseId, chapterId)
    onBackClick: () -> Unit
) {
    println("🔍 CourseDetailScreen - courseId reçu : '$courseId'")

    val course by viewModel.course.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()



    // Charger les données au démarrage
    LaunchedEffect(courseId) {
        println("🔍 LaunchedEffect - Chargement du cours : '$courseId'")
        viewModel.loadCourse(courseId)
    }

    Scaffold(
        topBar = {
            CourseDetailTopBar(
                title = course?.title ?: "Chargement...",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // État : Chargement
                isLoading -> {
                    LoadingState()
                }

                // État : Erreur
                error != null -> {
                    ErrorState(
                        message = error ?: "Erreur inconnue",
                        onRetry = { viewModel.refresh(courseId) }
                    )
                }

                // État : Affichage du cours et chapitres
                course != null -> {
                    CourseContent(
                        course = course!!,
                        chapters = chapters,
                        progress = viewModel.getCourseProgress(),
                        onChapterClick = { chapterId ->
                            onChapterClick(courseId, chapterId)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Barre supérieure de l'écran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Contenu principal : header + liste des chapitres
 */
@Composable
private fun CourseContent(
    course: Course,
    chapters: List<Chapter>,
    progress: CourseProgress,
    onChapterClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // En-tête du cours avec progression
        item {
            CourseHeader(
                course = course,
                progress = progress
            )
        }

        // Séparateur
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Liste des chapitres
        items(chapters) { chapter ->
            ChapterCard(
                chapter = chapter,
                onClick = { onChapterClick(chapter.chapterId) }
            )
        }

        // Espace pour bottom bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * En-tête du cours avec titre et barre de progression
 */
@Composable
private fun CourseHeader(
    course: Course,
    progress: CourseProgress
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Titre du cours
            Text(
                text = course.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Description (si présente)
            if (course.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = course.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barre de progression
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progression",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${progress.completedChapters}/${progress.totalChapters}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Card représentant un chapitre
 */
@Composable
private fun ChapterCard(
    chapter: Chapter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône du chapitre
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = getChapterIcon(chapter),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenu du chapitre
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Numéro du chapitre
                Text(
                    text = "Chapitre ${chapter.order +1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Titre du chapitre
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Informations du chapitre (pages, temps, etc.)
                ChapterInfo(chapter = chapter)

                // État du quiz si complété
                if (chapter.isQuizCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✅ Quiz complété",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Indicateur de statut
            Icon(
                imageVector = when {
                    chapter.isCompleted -> Icons.Default.CheckCircle
                    chapter.isContentRead || chapter.isVideoWatched -> Icons.Default.Edit
                    else -> Icons.Default.ChevronRight
                },
                contentDescription = null,
                tint = when {
                    chapter.isCompleted -> MaterialTheme.colorScheme.primary
                    chapter.isContentRead || chapter.isVideoWatched -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

/**
 * Informations détaillées du chapitre (pages, temps, vidéo)
 */
@Composable
private fun ChapterInfo(chapter: Chapter) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cours PDF
        if (chapter.hasCours || chapter.hasFdr) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

            }
        }

        // Vidéo
        if (chapter.hasVideo) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            }
        }
    }
}

/**
 * Retourne une icône appropriée selon le contenu du chapitre
 */
private fun getChapterIcon(chapter: Chapter): ImageVector {
    return when {
        chapter.title.contains("Introduction", ignoreCase = true) -> Icons.Default.School
        chapter.title.contains("TP", ignoreCase = true) -> Icons.Default.Code
        chapter.hasVideo && !chapter.hasCours -> Icons.Default.PlayCircle
        else -> Icons.Default.MenuBook
    }
}

/**
 * État de chargement
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement du cours...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * État d'erreur
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "❌", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Erreur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CourseDetailScreenPreview() {
    LearnityTheme {
        CourseDetailScreen(
            courseId = "extraction_connaissances",
            onChapterClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CourseHeaderPreview() {
    LearnityTheme {
        CourseHeader(
            course = MockData.sampleCourses.first(),
            progress = CourseProgress(completedChapters = 2, totalChapters = 5)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterCardPreview() {
    LearnityTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChapterCard(
                chapter = MockData.getChaptersForCourse("extraction_connaissances")[0],
                onClick = {}
            )
            ChapterCard(
                chapter = MockData.getChaptersForCourse("extraction_connaissances")[1],
                onClick = {}
            )
            ChapterCard(
                chapter = MockData.getChaptersForCourse("extraction_connaissances")[3],
                onClick = {}
            )
        }
    }
}