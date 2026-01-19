package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterContentViewModel(
    private val courseRepository: CourseRepository = CourseRepository()
) : ViewModel() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadChapter(courseId: String, chapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { _chapter.value = it }
                .onFailure { _error.value = it.message ?: "Erreur lors du chargement" }
            _isLoading.value = false
        }
    }
}

@Composable
fun ChapterContentScreen(
    courseId: String,
    chapterId: String,
    viewModel: ChapterContentViewModel = viewModel(),
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit,
    onBackClick: () -> Unit
) {
    val chapter by viewModel.chapter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId, chapterId) {
        viewModel.loadChapter(courseId, chapterId)
    }

    Scaffold(
        topBar = {
            ChapterContentTopBar(
                title = chapter?.title ?: "Chargement...",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(error!!)
                chapter != null -> ChapterContentLayout(
                    chapter = chapter!!,
                    onCoursClick = onCoursClick,
                    onFdrClick = onFdrClick,
                    onVideoClick = onVideoClick,
                    onStartQuiz = onStartQuiz
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterContentTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
        }
    )
}

@Composable
private fun ChapterContentLayout(
    chapter: Chapter,
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CONTENU DISPONIBLE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (chapter.hasCours) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                isCompleted = chapter.isContentRead,
                onClick = onCoursClick
            )
        }

        if (chapter.hasFdr) {
            ContentOptionCard(
                icon = Icons.Default.Description,
                title = "Fiche de Révision",
                isCompleted = chapter.isContentRead,
                onClick = onFdrClick
            )
        }

        if (chapter.hasVideo) {
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                isCompleted = chapter.isVideoWatched,
                onClick = onVideoClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Section Quiz dynamique
        if (chapter.isQuizUnlocked) {
            QuizSection(isQuizCompleted = chapter.isQuizCompleted, onStartQuiz = onStartQuiz)
        } else {
            LockedQuizSection()
        }
    }
}

@Composable
private fun ContentOptionCard(
    icon: ImageVector,
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isCompleted) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QuizSection(isQuizCompleted: Boolean, onStartQuiz: () -> Unit) {
    if (isQuizCompleted) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Quiz validé avec succès !", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        Button(
            onClick = onStartQuiz,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Passer le Quiz de Chapitre", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LockedQuizSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Quiz Verrouillé", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Complétez la lecture pour débloquer le test", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable private fun LoadingState() { /* ... identique ... */ }
@Composable private fun ErrorState(message: String) { /* ... identique ... */ }