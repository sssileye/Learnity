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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.mock.MockData
import com.miage.learnity.ui.theme.LearnityTheme

/**
 * Écran de choix du contenu d'un chapitre
 * Permet de choisir entre : Cours PDF, Fiche de Révision, ou Vidéo
 *
 * @param courseId ID du cours
 * @param chapterId ID du chapitre
 * @param viewModel ViewModel pour gérer les données
 * @param onCoursClick Callback pour ouvrir le cours PDF
 * @param onFdrClick Callback pour ouvrir la fiche de révision
 * @param onVideoClick Callback pour ouvrir la vidéo
 * @param onStartQuiz Callback pour démarrer le quiz
 * @param onBackClick Callback pour retourner
 */
@Composable
fun ChapterContentScreen(
    courseId: String,
    chapterId: String,
    viewModel: CourseDetailViewModel = viewModel(),
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit,
    onBackClick: () -> Unit
) {
    // Charger le chapitre
    val chapter = remember(courseId, chapterId) {
        MockData.getChapter(courseId, chapterId)
    }

    Scaffold(
        topBar = {
            ChapterContentTopBar(
                title = chapter?.title ?: "Chargement...",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (chapter != null) {
            ChapterContentLayout(
                chapter = chapter,
                onCoursClick = onCoursClick,
                onFdrClick = onFdrClick,
                onVideoClick = onVideoClick,
                onStartQuiz = onStartQuiz,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Erreur : chapitre non trouvé
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Chapitre non trouvé")
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
                subtitle = "${chapter.pageCount} pages • ${chapter.estimatedReadTime} min",
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

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChapterContentScreenPreview() {
    LearnityTheme {
        ChapterContentScreen(
            courseId = "extraction_connaissances",
            chapterId = "ec_chap1",
            onCoursClick = {},
            onFdrClick = {},
            onVideoClick = {},
            onStartQuiz = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContentOptionCardPreview() {
    LearnityTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                subtitle = "15 pages • 20 min",
                isCompleted = false,
                onClick = {}
            )
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                subtitle = "25 min sur YouTube",
                isCompleted = true,
                onClick = {}
            )
        }
    }
}