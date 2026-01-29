package com.miage.learnity.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.ui.screens.quiz.QuizViewModel
import com.miage.learnity.ui.utils.*

@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseDetailViewModel = viewModel(),
    quizViewModel: QuizViewModel = viewModel(),
    onChapterClick: (String, String) -> Unit,
    onMegaQuizClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val dimensions = rememberResponsiveDimensions()
    val course by viewModel.course.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isExamUnlocked by viewModel.isExamUnlocked.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val progress by viewModel.courseProgress.collectAsState()
    val examHistory by viewModel.examHistory.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadCourse(courseId) }

    Scaffold(
        topBar = {
            CourseDetailTopBar(
                title = course?.title ?: "Chargement...",
                onBackClick = onBackClick,
                dimensions = dimensions
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when {
                isLoading -> LoadingState(dimensions)
                error != null -> ErrorState(error ?: "Erreur inconnue", { viewModel.refresh(courseId) }, dimensions)
                course != null -> CourseContent(
                    course = course!!,
                    chapters = chapters,
                    progress = progress,
                    examHistory = examHistory,
                    isExamUnlocked = isExamUnlocked,
                    onChapterClick = { chapterId -> onChapterClick(courseId, chapterId) },
                    onMegaQuizLaunch = {
                        quizViewModel.loadMegaQuiz(courseId)
                        onMegaQuizClick()
                    },
                    dimensions = dimensions
                )
            }
        }
    }
}

@Composable
private fun CourseContent(
    course: Course,
    chapters: List<Chapter>,
    progress: CourseProgress,
    examHistory: List<QuizHistory>,
    isExamUnlocked: Boolean,
    onChapterClick: (String) -> Unit,
    onMegaQuizLaunch: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensions.screenPaddingHorizontal,
            vertical = dimensions.itemSpacing
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        item { CourseHeader(course, progress, dimensions) }

        // --- SECTION EXAMEN BLANC ---
        item {
            // Calcul dynamique des points de l'examen (Score max 20 + Bonus 10 si perfect)
            val examBestScore = examHistory.maxOfOrNull { it.score } ?: 0
            val examPointsCollectes = if (examBestScore == 20) 30 else examBestScore
            val isExamPerfect = examPointsCollectes == 30

            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExamUnlocked) Color(0xFF673AB7) else Color(0xFFBDBDBD)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isExamUnlocked) 4.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isExamUnlocked) { onMegaQuizLaunch() }
                            .padding(dimensions.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(dimensions.iconSizeMedium * 1.6f),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = if (isExamUnlocked) 0.2f else 0.1f)
                        ) {
                            Icon(
                                imageVector = if (isExamUnlocked) Icons.Default.AutoAwesome else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(dimensions.itemSpacing))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Examen Blanc d'UE",
                                fontSize = dimensions.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isExamUnlocked) "20 questions • Toute l'UE" else "Validez tous les chapitres",
                                fontSize = dimensions.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // --- BADGE DE SCORE EXAMEN A DROITE ---
                        if (isExamUnlocked && examHistory.isNotEmpty()) {
                            Surface(
                                color = if (isExamPerfect) Color(0xFFE8F5E9) else Color(0xFFFFE082),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$examPointsCollectes / 30",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isExamPerfect) Color(0xFF2E7D32) else Color(0xFF795548)
                                )
                            }
                        } else if (isExamUnlocked) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                        }
                    }
                }

                if (examHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpandableHistorySection(
                        title = "HISTORIQUE DES EXAMENS",
                        history = examHistory,
                        dimensions = dimensions,
                        accentColor = Color(0xFF673AB7)
                    )
                }
            }
        }

        item {
            Text(
                text = "Chapitres (${chapters.size})",
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }

        items(chapters) { chapter ->
            ChapterCard(chapter, { onChapterClick(chapter.chapterId) }, dimensions)
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit, dimensions: ResponsiveDimensions) {
    val pointsCollectes = if (chapter.bestScore == 5) 8 else chapter.bestScore
    val isPerfect = pointsCollectes == 8

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chapitre ${chapter.order + 1}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = chapter.title, fontSize = dimensions.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2)

                if (!chapter.isQuizCompleted) {
                    ChapterInfo(chapter, dimensions)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (chapter.isQuizCompleted) {
                Surface(
                    color = if (isPerfect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPerfect) Color(0xFF4CAF50).copy(0.5f) else Color(0xFFFF9800).copy(0.5f)
                    )
                ) {
                    Text(
                        text = "$pointsCollectes / 8",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPerfect) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            } else {
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun ExpandableHistorySection(
    title: String,
    history: List<QuizHistory>,
    dimensions: ResponsiveDimensions,
    accentColor: Color
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = accentColor
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                QuizHistoryTable(history = history, dimensions = dimensions)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course, progress: CourseProgress, dimensions: ResponsiveDimensions) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(dimensions.cardPadding)) {
            Text(text = course.title, fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (course.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = course.description, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Progression des quiz", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(text = "${progress.completedChapters}/${progress.totalChapters}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun ChapterInfo(chapter: Chapter, dimensions: ResponsiveDimensions) {
    val contentParts = mutableListOf<String>()
    if (chapter.hasCours) contentParts.add("📄 Cours")
    if (chapter.hasFdr) contentParts.add("📋 FDR")
    if (chapter.hasVideo) contentParts.add("🎥 Vidéo")
    if (contentParts.isNotEmpty()) {
        Text(text = contentParts.joinToString(" • "), fontSize = 11.sp, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailTopBar(title: String, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Retour") }
        }
    )
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}