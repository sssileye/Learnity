package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.components.PdfViewer
import com.miage.learnity.ui.components.YouTubePlayer
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.*

@Composable
fun PdfViewerScreen(
    courseId: String,
    chapterId: String,
    type: String,
    viewModel: PdfViewerViewModel = viewModel(),
    onMarkComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val dimensions = rememberResponsiveDimensions()
    val contentUrl by viewModel.contentUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isMarkedAsRead by viewModel.isMarkedAsRead.collectAsState()
    val contentType by viewModel.contentType.collectAsState()

    var videoEnded by remember { mutableStateOf(false) }

    val typeEnum = remember(type) {
        when (type) {
            "fdr" -> UserProgressRepository.ContentType.FDR
            "video" -> UserProgressRepository.ContentType.VIDEO
            else -> UserProgressRepository.ContentType.COURS
        }
    }

    LaunchedEffect(courseId, chapterId, type) {
        viewModel.loadContent(courseId, chapterId, typeEnum)
    }

    LaunchedEffect(videoEnded) {
        if (videoEnded && !isMarkedAsRead) {
            viewModel.markAsReadOrWatched()
        }
    }

    Scaffold(
        topBar = {
            PdfViewerTopBar(
                title = when (typeEnum) {
                    UserProgressRepository.ContentType.COURS -> "Cours"
                    UserProgressRepository.ContentType.FDR -> "Fiche de Révision"
                    UserProgressRepository.ContentType.VIDEO -> "Vidéo"
                },
                onBackClick = onBackClick,
                dimensions = dimensions
            )
        },
        bottomBar = {
            PdfViewerBottomBar(
                contentType = typeEnum,
                isMarkedAsRead = isMarkedAsRead,
                onMarkComplete = {
                    viewModel.markAsReadOrWatched()
                    onMarkComplete()
                },
                dimensions = dimensions
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),      // ✅ TopBar spacing
                    bottom = paddingValues.calculateBottomPadding()  // ✅ BottomBar spacing
                )
        ) {
            when {
                isLoading -> LoadingContent(dimensions)
                contentUrl != null && typeEnum == UserProgressRepository.ContentType.VIDEO -> {
                    YouTubePlayer(
                        videoUrl = contentUrl!!,
                        onVideoEnd = { videoEnded = true },
                        onError = { error -> println("❌ Erreur YouTube: $error") }
                    )
                }
                contentUrl != null && typeEnum != UserProgressRepository.ContentType.VIDEO -> {
                    PdfViewer(
                        url = contentUrl!!,
                        onError = { error -> println("❌ Erreur PDF : $error") },
                        onLoadComplete = { pages -> println("✅ PDF chargé : $pages pages") }
                    )
                }
                else -> ErrorContent(dimensions)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfViewerTopBar(title: String, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = dimensions.titleMedium) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour", modifier = Modifier.size(dimensions.iconSizeMedium))
            }
        },
        windowInsets = WindowInsets(0.dp)  // ✅ Supprime l'espace système par défaut
    )
}

@Composable
private fun PdfViewerBottomBar(
    contentType: UserProgressRepository.ContentType,
    isMarkedAsRead: Boolean,
    onMarkComplete: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPaddingHorizontal, vertical = dimensions.itemSpacing / 1.5f)) {
            if (!isMarkedAsRead) {
                Button(onClick = onMarkComplete, modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(dimensions.itemSpacing / 1.5f))
                    Text(
                        text = when (contentType) {
                            UserProgressRepository.ContentType.VIDEO -> "Marquer la vidéo comme vue"
                            else -> "J'ai terminé la lecture"
                        },
                        fontSize = dimensions.bodyLarge
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(dimensions.cardPadding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(dimensions.itemSpacing / 1.5f))
                        Text(
                            text = when (contentType) {
                                UserProgressRepository.ContentType.VIDEO -> "Vidéo vue ✓"
                                else -> "Lecture terminée ✓"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = dimensions.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSizeLarge))
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text("Chargement du contenu...", fontSize = dimensions.bodyLarge)
        }
    }
}

@Composable
private fun ErrorContent(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Error, contentDescription = null, modifier = Modifier.size(dimensions.iconSizeLarge), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Contenu non disponible", fontSize = dimensions.titleMedium)
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun PdfViewerScreenPreview() {
    LearnityTheme {
        PdfViewerScreen(
            courseId = "test",
            chapterId = "test",
            type = "cours",
            onMarkComplete = {},
            onBackClick = {}
        )
    }
}