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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.ui.utils.*

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
    val dimensions = rememberResponsiveDimensions()
    val chapter by viewModel.chapter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId, chapterId) {
        viewModel.loadChapter(courseId, chapterId)
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            ChapterContentTopBar(
                title = chapter?.title ?: "Chargement...",
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
                error != null -> ErrorState(error!!, { viewModel.refresh() }, dimensions)
                chapter != null -> ChapterContentLayout(
                    chapter = chapter!!,
                    onCoursClick = onCoursClick,
                    onFdrClick = onFdrClick,
                    onVideoClick = onVideoClick,
                    onStartQuiz = onStartQuiz,
                    dimensions = dimensions
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterContentTopBar(title: String, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = dimensions.titleMedium, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(dimensions.iconSizeMedium))
            }
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun ChapterContentLayout(
    chapter: Chapter,
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = dimensions.screenPaddingHorizontal,
                end = dimensions.screenPaddingHorizontal,
                top = 4.dp,
                bottom = dimensions.screenPaddingHorizontal
            ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        Text(
            text = "CONTENU DISPONIBLE",
            fontSize = dimensions.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensions.itemSpacing / 3)
        )

        if (chapter.hasCours) {
            ContentOptionCard(
                icon = Icons.Default.MenuBook,
                title = "Cours Complet",
                isCompleted = chapter.isCoursRead,
                onClick = onCoursClick,
                dimensions = dimensions
            )
        }

        if (chapter.hasFdr) {
            ContentOptionCard(
                icon = Icons.Default.Description,
                title = "Fiche de Révision",
                isCompleted = chapter.isFdrRead,
                onClick = onFdrClick,
                dimensions = dimensions
            )
        }

        if (chapter.hasVideo) {
            ContentOptionCard(
                icon = Icons.Default.PlayCircle,
                title = "Vidéo Explicative",
                isCompleted = chapter.isVideoWatched,
                onClick = onVideoClick,
                dimensions = dimensions
            )
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        HorizontalDivider(modifier = Modifier.padding(horizontal = dimensions.itemSpacing / 1.5f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Text(
            text = "ÉVALUATION",
            fontSize = dimensions.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = dimensions.itemSpacing / 3)
        )

        // ✅ Débloqué si isQuizUnlocked est true (calculé par le VM : isCoursRead || isFdrRead)
        if (chapter.isQuizUnlocked) {
            QuizSection(
                isQuizCompleted = chapter.isQuizCompleted,
                onStartQuiz = onStartQuiz,
                dimensions = dimensions
            )
        } else {
            LockedQuizSection(dimensions)
        }
    }
}

@Composable
private fun ContentOptionCard(
    icon: ImageVector,
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isCompleted) null else CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(dimensions.iconSizeMedium * 1.67f),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconSizeMedium * 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Text(
                text = title,
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensions.iconSizeMedium * 0.83f)
            )
        }
    }
}

@Composable
private fun QuizSection(isQuizCompleted: Boolean, onStartQuiz: () -> Unit, dimensions: ResponsiveDimensions) {
    if (isQuizCompleted) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(dimensions.itemSpacing))
                Text(text = "Quiz validé !", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        Button(
            onClick = onStartQuiz,
            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
        ) {
            Icon(imageVector = Icons.Default.Quiz, contentDescription = null)
            Spacer(modifier = Modifier.width(dimensions.itemSpacing))
            Text(text = "Passer le Quiz", fontSize = dimensions.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LockedQuizSection(dimensions: ResponsiveDimensions) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensions.iconSizeLarge * 0.67f)
            )
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
            Text(text = "Quiz Verrouillé", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 3))
            // ✅ Texte mis à jour : Cours OU Fiche de Révision
            Text(
                text = "Lisez le cours ou la fiche de révision pour débloquer le quiz",
                fontSize = dimensions.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSizeLarge))
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Chargement...", fontSize = dimensions.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(dimensions.screenPaddingHorizontal * 2)) {
            Text(text = "❌", fontSize = dimensions.displayLarge)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Erreur", fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
            Text(text = message, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))
            Button(onClick = onRetry) { Text("Réessayer", fontSize = dimensions.bodyLarge) }
        }
    }
}