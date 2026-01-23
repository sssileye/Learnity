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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.ui.screens.quiz.QuizViewModel
import com.miage.learnity.ui.theme.LearnityTheme
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> LoadingState(dimensions)
                error != null -> ErrorState(error ?: "Erreur inconnue", { viewModel.refresh(courseId) }, dimensions)
                course != null -> CourseContent(
                    course = course!!,
                    chapters = chapters,
                    progress = viewModel.getCourseProgress(),
                    onChapterClick = { chapterId -> onChapterClick(courseId, chapterId) },
                    onMegaQuizLaunch = { quizViewModel.loadMegaQuiz(courseId); onMegaQuizClick() },
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
    onChapterClick: (String) -> Unit,
    onMegaQuizLaunch: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val isUEComplete = progress.percentage >= 1.0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(dimensions.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        item { CourseHeader(course, progress, dimensions) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.itemSpacing / 3),
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUEComplete) Color(0xFF673AB7) else Color(0xFFBDBDBD)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isUEComplete) 4.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.clickable(enabled = isUEComplete) { onMegaQuizLaunch() }.padding(dimensions.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(dimensions.iconSizeMedium * 1.67f),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = if (isUEComplete) 0.2f else 0.1f)
                    ) {
                        Icon(
                            imageVector = if (isUEComplete) Icons.Default.AutoAwesome else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(dimensions.itemSpacing / 1.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(dimensions.itemSpacing))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Examen Blanc d'UE", fontSize = dimensions.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (isUEComplete) "20 questions aléatoires sur toute l'UE" else "Complétez l'UE à 100% pour débloquer",
                            fontSize = dimensions.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    if (isUEComplete) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(dimensions.iconSizeMedium))
                    }
                }
            }
        }

        item {
            Text(
                text = "Chapitres (${chapters.size})",
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = dimensions.itemSpacing / 2, start = dimensions.itemSpacing / 3)
            )
        }

        items(chapters) { chapter -> ChapterCard(chapter, { onChapterClick(chapter.chapterId) }, dimensions) }

        item { Spacer(modifier = Modifier.height(dimensions.itemSpacing * 5)) }
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
                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
                Text(text = course.description, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f))
            }

            Spacer(modifier = Modifier.height(dimensions.itemSpacing))

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Progression globale", fontSize = dimensions.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    Text(
                        text = "${progress.completedChapters}/${progress.totalChapters} chapitres",
                        fontSize = dimensions.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
                LinearProgressIndicator(
                    progress = { progress.percentage },
                    modifier = Modifier.fillMaxWidth().height(dimensions.itemSpacing / 1.5f).clip(RoundedCornerShape(dimensions.cornerRadiusSmall / 2)),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                Text(
                    text = "${(progress.percentage * 100).toInt()}% complété",
                    fontSize = dimensions.bodySmall * 0.9f,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End).padding(top = dimensions.itemSpacing / 3)
                )
            }
        }
    }
}

@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit, dimensions: ResponsiveDimensions) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (chapter.isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(dimensions.iconSizeLarge), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = getChapterIcon(chapter), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dimensions.iconSizeMedium))
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Chapitre ${chapter.order + 1}", fontSize = dimensions.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(text = chapter.title, fontSize = dimensions.bodyLarge, fontWeight = FontWeight.Bold)
                ChapterInfo(chapter, dimensions)
                if (chapter.isQuizCompleted) {
                    Text(text = "✅ Quiz complété", fontSize = dimensions.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = dimensions.itemSpacing / 3))
                }
            }

            Icon(
                imageVector = if (chapter.isCompleted) Icons.Default.CheckCircle else if (chapter.isCoursRead || chapter.isFdrRead || chapter.isVideoWatched) Icons.Default.Edit else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (chapter.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensions.iconSizeMedium)
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
        Text(text = contentParts.joinToString(" • "), fontSize = dimensions.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getChapterIcon(chapter: Chapter): ImageVector {
    return if (chapter.title.contains("Introduction", ignoreCase = true)) Icons.Default.School
    else if (chapter.title.contains("TP", ignoreCase = true)) Icons.Default.Code
    else Icons.Default.MenuBook
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailTopBar(title: String, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = dimensions.titleMedium, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(dimensions.iconSizeMedium))
            }
        }
    )
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSizeLarge))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(dimensions.screenPaddingHorizontal * 2)) {
            Text(text = "❌", fontSize = dimensions.displayLarge)
            Text(text = message, fontSize = dimensions.bodyMedium)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Button(onClick = onRetry, modifier = Modifier.height(dimensions.buttonHeightSmall)) { Text("Réessayer", fontSize = dimensions.bodyLarge) }
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun CourseDetailScreenPreview() {
    LearnityTheme {
        CourseDetailScreen(
            courseId = "test",
            onChapterClick = { _, _ -> },
            onMegaQuizClick = {},
            onBackClick = {}
        )
    }
}