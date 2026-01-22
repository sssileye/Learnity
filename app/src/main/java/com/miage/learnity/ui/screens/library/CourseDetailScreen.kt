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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.ui.screens.quiz.QuizViewModel

/**
 * Écran de détail d'un cours : affiche les chapitres et le bouton Mega Quiz (conditionnel)
 */
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel = viewModel(),
    quizViewModel: QuizViewModel = viewModel(),
    onChapterClick: (String, String) -> Unit,
    onMegaQuizClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val course by viewModel.course.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId) {
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
                isLoading -> LoadingState()
                error != null -> ErrorState(
                    message = error ?: "Erreur inconnue",
                    onRetry = { viewModel.refresh(courseId) }
                )
                course != null -> {
                    CourseContent(
                        course = course!!,
                        chapters = chapters,
                        progress = viewModel.getCourseProgress(),
                        onChapterClick = { chapterId ->
                            onChapterClick(courseId, chapterId)
                        },
                        onMegaQuizLaunch = {
                            quizViewModel.loadMegaQuiz(courseId)
                            onMegaQuizClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Contenu principal : header + BOUTON MEGA QUIZ (Grisé si < 100%) + liste des chapitres
 */
@Composable
private fun CourseContent(
    course: Course,
    chapters: List<Chapter>,
    progress: CourseProgress,
    onChapterClick: (String) -> Unit,
    onMegaQuizLaunch: () -> Unit
) {
    // ⭐ Déterminer si le cours est totalement complété
    val isUEComplete = progress.percentage >= 1.0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CourseHeader(course = course, progress = progress)
        }

        // --- SECTION BOUTON MEGA QUIZ CONDITIONNEL ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    // ⭐ Grisé (Grey) si incomplet, Violet signature si 100%
                    containerColor = if (isUEComplete) Color(0xFF673AB7) else Color(0xFFBDBDBD)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isUEComplete) 4.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        // ⭐ Clic désactivé si pas 100%
                        .clickable(enabled = isUEComplete) { onMegaQuizLaunch() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = if (isUEComplete) 0.2f else 0.1f)
                    ) {
                        Icon(
                            // ⭐ Icône Cadenas si verrouillé
                            imageVector = if (isUEComplete) Icons.Default.AutoAwesome else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Examen Blanc d'UE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isUEComplete)
                                "20 questions aléatoires sur toute l'UE"
                            else
                                "Complétez l'UE à 100% pour débloquer",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    if (isUEComplete) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Chapitres (${chapters.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }

        items(chapters) { chapter ->
            ChapterCard(
                chapter = chapter,
                onClick = { onChapterClick(chapter.chapterId) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * En-tête du cours avec titre et barre de progression
 */
@Composable
private fun CourseHeader(course: Course, progress: CourseProgress) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = course.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (course.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = course.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Progression globale", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Text(text = "${progress.completedChapters}/${progress.totalChapters} chapitres", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                Text(
                    text = "${(progress.percentage * 100).toInt()}% complété",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Card représentant un chapitre
 */
@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = getChapterIcon(chapter), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chapitre ${chapter.order + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = chapter.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ChapterInfo(chapter = chapter)
                if (chapter.isQuizCompleted) {
                    Text(text = "✅ Quiz complété", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Icon(
                imageVector = if (chapter.isCompleted) Icons.Default.CheckCircle else if (chapter.isCoursRead || chapter.isFdrRead || chapter.isVideoWatched) Icons.Default.Edit else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (chapter.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(title = { Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1) }, navigationIcon = { IconButton(onClick = onBackClick) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour") } })
}

@Composable
private fun ChapterInfo(chapter: Chapter) {
    val contentParts = mutableListOf<String>()
    if (chapter.hasCours) contentParts.add("📄 Cours")
    if (chapter.hasFdr) contentParts.add("📋 FDR")
    if (chapter.hasVideo) contentParts.add("🎥 Vidéo")
    if (contentParts.isNotEmpty()) Text(text = contentParts.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun getChapterIcon(chapter: Chapter): ImageVector = if (chapter.title.contains("Introduction", ignoreCase = true)) Icons.Default.School else if (chapter.title.contains("TP", ignoreCase = true)) Icons.Default.Code else Icons.Default.MenuBook

@Composable
private fun LoadingState() = Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) = Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Text(text = "❌", fontSize = 48.sp)
        Text(text = message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}