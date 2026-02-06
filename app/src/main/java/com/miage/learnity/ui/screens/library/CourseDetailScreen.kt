package com.miage.learnity.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // ⭐ On utilise sortedChapters (flux trié) au lieu de chapters
    val chapters by viewModel.sortedChapters.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()

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
                error != null -> ErrorState(error ?: "Erreur réseau", { viewModel.refresh(courseId) }, dimensions)
                course != null -> CourseContent(
                    course = course!!,
                    chapters = chapters,
                    currentSortOrder = currentSortOrder,
                    onSortOrderChange = { viewModel.updateSortOrder(it) },
                    progress = progress,
                    examHistory = examHistory,
                    isExamUnlocked = isExamUnlocked,
                    onToggleCourseFav = { viewModel.toggleCourseFavorite() },
                    onToggleChapterFav = { id, fav -> viewModel.toggleChapterFavorite(id, fav) },
                    onChapterClick = { chapterId -> onChapterClick(courseId, chapterId) },
                    onMegaQuizLaunch = { quizViewModel.loadMegaQuiz(courseId); onMegaQuizClick() },
                    dimensions = dimensions
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseContent(
    course: Course,
    chapters: List<Chapter>,
    currentSortOrder: CourseDetailViewModel.ChapterSortOrder,
    onSortOrderChange: (CourseDetailViewModel.ChapterSortOrder) -> Unit,
    progress: CourseProgress,
    examHistory: List<QuizHistory>,
    isExamUnlocked: Boolean,
    onToggleCourseFav: () -> Unit,
    onToggleChapterFav: (String, Boolean) -> Unit,
    onChapterClick: (String) -> Unit,
    onMegaQuizLaunch: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimensions.screenPaddingHorizontal,
            top = dimensions.itemSpacing,
            end = dimensions.screenPaddingHorizontal,
            bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        item {
            
            CourseHeader(
                course = course,
                progress = progress,
                dimensions = dimensions
            )
        }


        item {
            val examBestScore = examHistory.maxOfOrNull { it.score } ?: 0
            val examPointsCollectes = if (examBestScore == 20) 30 else examBestScore

            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExamUnlocked) Color(0xFF673AB7) else Color(0xFFBDBDBD)
                    )
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
                            color = Color.White.copy(alpha = 0.2f)
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
                                text = "EXAMEN BLANC D'UE",
                                fontSize = dimensions.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (isExamUnlocked) "Synthèse globale • 20 questions" else "Débloqué après tous les chapitres",
                                fontSize = dimensions.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        if (isExamUnlocked && examHistory.isNotEmpty()) {
                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$examPointsCollectes / 30",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF673AB7)
                                )
                            }
                        }
                    }
                }

                if (examHistory.isNotEmpty()) {
                    ExpandableHistorySection(
                        title = "HISTORIQUE DES EXAMENS",
                        history = examHistory,
                        dimensions = dimensions,
                        accentColor = Color(0xFF673AB7)
                    )
                }
            }
        }

        //  BARRE DE TRI DES CHAPITRES
        item {
            Column {
                Text(
                    text = "CHAPITRES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourseDetailViewModel.ChapterSortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = currentSortOrder == order,
                            onClick = { onSortOrderChange(order) },
                            label = {
                                Text(
                                    text = when(order) {
                                        CourseDetailViewModel.ChapterSortOrder.ORIGINAL -> "Ordre"
                                        CourseDetailViewModel.ChapterSortOrder.FAVORITES -> "Favoris"
                                        CourseDetailViewModel.ChapterSortOrder.INCOMPLETE_FIRST -> "À faire"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            }
        }

        items(chapters, key = { it.chapterId }) { chapter ->
            ChapterCard(
                chapter = chapter,
                onToggleFav = { onToggleChapterFav(chapter.chapterId, !chapter.isFavorite) },
                onClick = { onChapterClick(chapter.chapterId) },
                dimensions = dimensions
            )
        }
    }
}

@Composable
private fun CourseHeader(
    course: Course,
    progress: CourseProgress,
    dimensions: ResponsiveDimensions
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding * 1.2f)
        ) {

            Text(
                text = course.title,

                fontSize = (dimensions.titleLarge.value * 0.8).sp,
                lineHeight = (dimensions.titleLarge.value * 0.9).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,

                softWrap = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (course.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = course.description,
                    fontSize = dimensions.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "PROGRESSION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "${progress.completedChapters} / ${progress.totalChapters} CHAPITRES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    onToggleFav: () -> Unit,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val pointsCollectes = if (chapter.bestScore == 5) 8 else chapter.bestScore
    val isPerfect = pointsCollectes == 8

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "CHAPITRE ${chapter.order + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = chapter.title, fontSize = dimensions.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2)


                if (!chapter.isQuizCompleted) {
                    ChapterInfo(chapter, dimensions)
                }
            }

            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = if (chapter.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (chapter.isFavorite) Color(0xFFF06292) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (chapter.isQuizCompleted) {
                Surface(
                    color = if (isPerfect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "$pointsCollectes / 8",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPerfect) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
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
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                history.take(5).forEach { record ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${record.date} à ${record.hour}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${record.score}/${record.total}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }
            }
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
        Text(
            text = contentParts.joinToString(" • "),
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailTopBar(title: String, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Black, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, "Retour")
            }
        }
    )
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("RÉESSAYER") }
    }
}