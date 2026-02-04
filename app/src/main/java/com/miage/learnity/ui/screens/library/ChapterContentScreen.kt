package com.miage.learnity.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.QuizHistory
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
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId, chapterId) {
        viewModel.loadChapter(courseId, chapterId)
    }

    Scaffold(
        topBar = {
            ChapterContentTopBar(
                title = "Contenu du Chapitre",
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
            val currentChapter = chapter // Smart-cast local pour la sécurité

            when {
                // ✅ On affiche le contenu en priorité dès que 'chapter' n'est plus null
                currentChapter != null -> ChapterContentLayout(
                    chapter = currentChapter,
                    history = history,
                    onCoursClick = onCoursClick,
                    onFdrClick = onFdrClick,
                    onVideoClick = onVideoClick,
                    onStartQuiz = onStartQuiz,
                    dimensions = dimensions
                )
                // ✅ On ne montre le loader que si on n'a vraiment aucune donnée
                isLoading -> LoadingState(dimensions)
                // ✅ Gestion de l'erreur
                error != null -> ErrorState(error!!, { viewModel.refresh() }, dimensions)
                // ✅ Fallback sécurité
                else -> LoadingState(dimensions)
            }
        }
    }
}

@Composable
private fun ChapterContentLayout(
    chapter: Chapter,
    history: List<QuizHistory>,
    onCoursClick: () -> Unit,
    onFdrClick: () -> Unit,
    onVideoClick: () -> Unit,
    onStartQuiz: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = dimensions.screenPaddingHorizontal,
                end = dimensions.screenPaddingHorizontal,
                top = 16.dp, // Un peu plus d'espace en haut
                bottom = 32.dp
            ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // ✅ LE NOM DU CHAPITRE S'AFFICHE ICI (EN ENTIER)
        Text(
            text = chapter.title,
            fontSize = (dimensions.titleLarge.value * 0.85).sp,
            fontWeight = FontWeight.Black,
            lineHeight = (dimensions.titleLarge.value * 1.1).sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        Text(
            text = "CONTENU DISPONIBLE",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )

        if (chapter.hasCours) {
            ContentOptionCard(Icons.Default.MenuBook, "Cours Complet", chapter.isCoursRead, onCoursClick, dimensions)
        }
        if (chapter.hasFdr) {
            ContentOptionCard(Icons.Default.Description, "Fiche de Révision", chapter.isFdrRead, onFdrClick, dimensions)
        }
        if (chapter.hasVideo) {
            ContentOptionCard(Icons.Default.PlayCircle, "Vidéo Explicative", chapter.isVideoWatched, onVideoClick, dimensions)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ÉVALUATION",
            fontSize = dimensions.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (chapter.isQuizUnlocked) {
            QuizSection(chapter = chapter, onStartQuiz = onStartQuiz, dimensions = dimensions)
        } else {
            LockedQuizSection(dimensions)
        }

        // --- ⭐ SECTION HISTORIQUE DÉPLIABLE ---
        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ExpandableHistorySection(
                title = "HISTORIQUE DU CHAPITRE",
                history = history,
                dimensions = dimensions,
                accentColor = MaterialTheme.colorScheme.primary
            )
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
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
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
private fun QuizSection(
    chapter: Chapter,
    onStartQuiz: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    val isPerfect = chapter.bestScore == 5

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onStartQuiz,
            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (chapter.isQuizCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(if (chapter.isQuizCompleted) Icons.Default.Replay else Icons.Default.Quiz, null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (chapter.isQuizCompleted) "Refaire le Quiz" else "Passer le Quiz",
                fontWeight = FontWeight.Bold
            )
        }

        if (chapter.isQuizCompleted) {
            Text(
                text = if (isPerfect)
                    "🏆 Score maximum atteint ! Tu as déjà récolté tous les Unity Points de ce chapitre."
                else
                    "✅ Quiz déjà validé. Améliore ton score pour gagner des UP supplémentaires !",
                fontSize = 12.sp,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center,
                fontWeight = if (isPerfect) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun QuizHistoryTable(
    history: List<QuizHistory>,
    dimensions: ResponsiveDimensions
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 5
    val pageCount = (history.size + pageSize - 1) / pageSize
    val currentItems = history.chunked(pageSize).getOrNull(currentPage) ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date / Heure", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Score", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center)
            Text("Gain UP", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.End)
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        currentItems.forEach { attempt ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(attempt.date, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(attempt.hour, fontSize = 10.sp, color = Color.Gray)
                }

                Text(
                    text = "${attempt.score}/${attempt.total}",
                    modifier = Modifier.weight(0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (attempt.pointsGained > 0) "+${attempt.pointsGained}" else "0",
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Black,
                    color = if (attempt.pointsGained > 0) Color(0xFF2E7D32) else Color.Gray
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        if (pageCount > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentPage > 0) currentPage-- }, enabled = currentPage > 0) {
                    Icon(Icons.Default.ChevronLeft, null, tint = if (currentPage > 0) MaterialTheme.colorScheme.primary else Color.LightGray)
                }
                Text("${currentPage + 1} / $pageCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (currentPage < pageCount - 1) currentPage++ }, enabled = currentPage < pageCount - 1) {
                    Icon(Icons.Default.ChevronRight, null, tint = if (currentPage < pageCount - 1) MaterialTheme.colorScheme.primary else Color.LightGray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterContentTopBar(
    title: String,
    onBackClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp // Taille standard fixe pour la barre
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            }
        }
    )
}

@Composable
private fun ContentOptionCard(icon: ImageVector, title: String, isCompleted: Boolean, onClick: () -> Unit, dimensions: ResponsiveDimensions) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
        border = if (isCompleted) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(modifier = Modifier.padding(dimensions.cardPadding), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(dimensions.iconSizeMedium * 1.5f), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dimensions.iconSizeMedium)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Icon(if (isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight, null, tint = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray)
        }
    }
}

@Composable
private fun LockedQuizSection(dimensions: ResponsiveDimensions) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // ✅ Indispensable pour que la colonne occupe toute la largeur
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, // ✅ Centre les éléments horizontalement
            verticalArrangement = Arrangement.Center // ✅ Centre verticalement si une hauteur est définie
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(dimensions.iconSizeMedium) // Utilise tes dimensions
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Quiz Verrouillé",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, // ✅ Centre le texte lui-même
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Lisez le cours pour débloquer",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center, // ✅ Centre le texte lui-même
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() } }

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Erreur : $message")
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}