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
import com.miage.learnity.data.Chapter
import com.miage.learnity.ui.components.PdfViewer
import com.miage.learnity.ui.theme.LearnityTheme

@Composable
fun PdfViewerScreen(
    courseId: String,
    chapterId: String,
    type: String,
    viewModel: PdfViewerViewModel = viewModel(),
    onMarkComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val chapter by viewModel.chapter.collectAsState()
    val contentUrl by viewModel.contentUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isMarkedAsRead by viewModel.isMarkedAsRead.collectAsState()
    val contentType by viewModel.contentType.collectAsState()

    // Convertir le string en ContentType
    val typeEnum = remember(type) {
        when (type) {
            "fdr" -> ContentType.FDR
            "video" -> ContentType.VIDEO
            else -> ContentType.COURS
        }
    }

    // Charger le contenu au démarrage
    LaunchedEffect(courseId, chapterId, type) {
        viewModel.loadContent(courseId, chapterId, typeEnum)
    }

    Scaffold(
        topBar = {
            PdfViewerTopBar(
                title = when (typeEnum) {
                    ContentType.COURS -> "Cours"
                    ContentType.FDR -> "Fiche de Révision"
                    ContentType.VIDEO -> "Vidéo"
                },
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            PdfViewerBottomBar(
                chapter = chapter,
                contentType = typeEnum,
                isMarkedAsRead = isMarkedAsRead,
                onMarkComplete = {
                    viewModel.markAsReadOrWatched()
                    onMarkComplete()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingContent()
                }
                contentUrl != null && typeEnum != ContentType.VIDEO -> {
                    // ✅ Afficher le PDF avec le viewer natif
                    PdfViewer(
                        url = contentUrl!!,
                        onError = { error ->
                            println("❌ Erreur PDF : $error")
                        },
                        onLoadComplete = { pages ->
                            println("✅ PDF chargé : $pages pages")
                        }
                    )
                }
                contentUrl != null && typeEnum == ContentType.VIDEO -> {
                    // TODO: Implémenter le lecteur YouTube
                    VideoPlaceholder(contentUrl!!)
                }
                else -> {
                    ErrorContent()
                }
            }
        }
    }
}

/**
 * TopBar du viewer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfViewerTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
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
 * BottomBar avec informations et bouton d'action
 */
@Composable
private fun PdfViewerBottomBar(
    chapter: Chapter?,
    contentType: ContentType,
    isMarkedAsRead: Boolean,
    onMarkComplete: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Informations du contenu
            chapter?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when (contentType) {
                        ContentType.VIDEO -> {
                            Text(
                                text = "🎥 Vidéo explicative",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "⏱️ ${it.videoDuration} min",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> {
                            Text(
                                text = "📄 ${it.pageCount} pages",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "⏱️ ${it.estimatedReadTime} min",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bouton d'action
            if (!isMarkedAsRead) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (contentType) {
                            ContentType.VIDEO -> "Marquer la vidéo comme vue"
                            else -> "J'ai terminé la lecture"
                        }
                    )
                }
            } else {
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
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (contentType) {
                                ContentType.VIDEO -> "Vidéo vue ✓"
                                else -> "Lecture terminée ✓"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Placeholder pour les vidéos YouTube (à implémenter plus tard)
 */
@Composable
private fun VideoPlaceholder(videoUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lecteur Vidéo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📱 En développement",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Le lecteur YouTube sera intégré prochainement",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "URL: ${videoUrl.take(50)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * État de chargement
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chargement du contenu...")
        }
    }
}

/**
 * État d'erreur
 */
@Composable
private fun ErrorContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Contenu non disponible",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
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