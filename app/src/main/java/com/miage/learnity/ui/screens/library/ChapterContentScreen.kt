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
import com.miage.learnity.repository.ProgressManager
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // Charger les données au démarrage
    LaunchedEffect(courseId, chapterId) {
        viewModel.loadChapter(courseId, chapterId)
    }

    // Rafraîchir quand on revient sur cet écran (après avoir marqué contenu comme lu)
    LaunchedEffect(Unit) {
        viewModel.refresh()
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
                isLoading -> LoadingState()
                error != null -> ErrorState(
                    message = error!!,
                    onRetry = { viewModel.refresh() }
                )
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

// ============================================
// TOP BAR
// ============================================

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

// ============================================
// CONTENT LAYOUT
// ============================================

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CONTENU DISPONIBLE",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // ✅ Cours complet - FLAG SÉPARÉ
        if (chapter.hasCours) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                isCompleted = chapter.isCoursRead,  // ✅ FLAG SÉPARÉ
                onClick = onCoursClick
            )
        }

        // ✅ Fiche de révision - FLAG SÉPARÉ
        if (chapter.hasFdr) {
            ContentOptionCard(
                icon = Icons.Default.Description,
                title = "Fiche de Révision",
                isCompleted = chapter.isFdrRead,    // ✅ FLAG SÉPARÉ
                onClick = onFdrClick
            )
        }

        // Vidéo
        if (chapter.hasVideo) {
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                isCompleted = chapter.isVideoWatched,
                onClick = onVideoClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ÉVALUATION",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

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

// Garde tous les autres composables (ContentOptionCard, QuizSection, etc.) tels quels
// ============================================
// CONTENT OPTION CARD
// ============================================

@Composable
private fun ContentOptionCard(
    icon: ImageVector,
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isCompleted) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Titre
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Indicateur de complétion
            Icon(
                imageVector = if (isCompleted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.ChevronRight
                },
                contentDescription = null,
                tint = if (isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================
// QUIZ SECTION
// ============================================

@Composable
private fun QuizSection(
    isQuizCompleted: Boolean,
    onStartQuiz: () -> Unit
) {
    if (isQuizCompleted) {
        // Quiz déjà complété
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quiz validé avec succès !",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        // Bouton pour démarrer le quiz
        Button(
            onClick = onStartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Passer le Quiz de Chapitre",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================
// LOCKED QUIZ SECTION
// ============================================

@Composable
private fun LockedQuizSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quiz Verrouillé",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complétez la lecture et/ou la vidéo pour débloquer le test",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ============================================
// LOADING & ERROR STATES
// ============================================

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement du chapitre...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayLarge
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
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}