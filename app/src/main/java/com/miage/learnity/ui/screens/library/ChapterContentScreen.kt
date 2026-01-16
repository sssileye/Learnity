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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.ui.theme.LearnityTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour ChapterContentScreen
 */
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

            println("🔍 ChapterContentViewModel - Chargement : $courseId/$chapterId")

            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    _chapter.value = chapter
                    println("✅ ChapterContentViewModel - Chapitre chargé : ${chapter.title}")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Chapitre non trouvé"
                    println("❌ ChapterContentViewModel - Erreur : ${exception.message}")
                }

            _isLoading.value = false
        }
    }
}

/**
 * Écran de choix du contenu d'un chapitre
 */
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

    // ✅ Charger le chapitre depuis Firebase
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
                    ErrorState(message = error ?: "Erreur inconnue")
                }

                // État : Affichage du contenu
                chapter != null -> {
                    ChapterContentLayout(
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
}

/**
 * TopBar de l'écran
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterContentTopBar(
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
        }
    )
}

/**
 * Layout principal avec les options de contenu
 */
@Composable
private fun ChapterContentLayout(
    chapter: Chapter,
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titre de la section
        Text(
            text = "CONTENU DISPONIBLE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Cours complet (si disponible)
        if (chapter.hasCours) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                subtitle = "${chapter.pageCount} pages • ${chapter.estimatedReadTime} min", //A supprimer
                isCompleted = chapter.isContentRead,
                onClick = onCoursClick
            )
        }

        // Fiche de révision (si disponible)
        if (chapter.hasFdr) {
            ContentOptionCard(
                icon = Icons.Default.Description,
                title = "Fiche de Révision",
                subtitle = "Version condensée du cours",
                isCompleted = chapter.isContentRead,
                onClick = onFdrClick
            )
        }

        // Vidéo (si disponible)
        if (chapter.hasVideo) {
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                subtitle = "${chapter.videoDuration} min sur YouTube",
                isCompleted = chapter.isVideoWatched,
                onClick = onVideoClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        // Section Quiz
        if (chapter.isQuizUnlocked) {
            QuizSection(
                isQuizCompleted = chapter.isQuizCompleted,
                onStartQuiz = onStartQuiz
            )
        } else {
            LockedQuizSection()
        }
    }
}

/**
 * Card pour une option de contenu (Cours/FDR/Vidéo)
 */
@Composable
private fun ContentOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenu texte
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Indicateur
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complété",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Section Quiz (débloqué)
 */
@Composable
private fun QuizSection(
    isQuizCompleted: Boolean,
    onStartQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isQuizCompleted) {
            // Quiz déjà complété
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quiz complété !",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Quiz disponible
            Button(
                onClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Passer le Quiz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Section Quiz (verrouillé)
 */
@Composable
private fun LockedQuizSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quiz Verrouillé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Consultez le contenu pour débloquer le quiz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chargement du chapitre...")
        }
    }
}

/**
 * État d'erreur
 */
@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
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
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChapterContentScreenPreview() {
    LearnityTheme {
        ChapterContentScreen(
            courseId = "test",
            chapterId = "test",
            onCoursClick = {},
            onFdrClick = {},
            onVideoClick = {},
            onStartQuiz = {},
            onBackClick = {}
        )
    }
}