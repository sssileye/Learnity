package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// ============================================
// VIEWMODEL CORRIGÉ
// ============================================
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
            // On force la récupération fraîche depuis Firestore
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess {
                    _chapter.value = it
                    println("✅ Chapter Loaded: isContentRead=${it.isContentRead}, isQuizUnlocked=${it.isQuizUnlocked}")
                }
                .onFailure { _error.value = it.message ?: "Erreur lors du chargement" }
            _isLoading.value = false
        }
    }
}

// ============================================
// ECRAN PRINCIPAL
// ============================================
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

    // 🔥 Correction majeure : Rechargement systématique à l'affichage
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
                error != null -> ErrorState(error!!, onRetry = { viewModel.loadChapter(courseId, chapterId) })
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CONTENU DISPONIBLE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Option : Cours
        if (chapter.hasCours) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                isCompleted = chapter.isContentRead,
                onClick = onCoursClick
            )
        }

        // Option : FDR
        if (chapter.hasFdr) {
            ContentOptionCard(
                icon = Icons.Default.Description,
                title = "Fiche de Révision",
                isCompleted = chapter.isContentRead,
                onClick = onFdrClick
            )
        }

        // Option : Vidéo
        if (chapter.hasVideo) {
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                isCompleted = chapter.isVideoWatched,
                onClick = onVideoClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // SECTION QUIZ : Condition de déblocage
        // On vérifie isQuizUnlocked défini dans ta DataClass
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
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
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Quiz validé !", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
            Text("Démarrer le Quiz", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            Text("Quiz Verrouillé", fontWeight = FontWeight.Bold)
            Text("Terminez la lecture pour débloquer", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message)
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}