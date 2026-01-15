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
import com.miage.learnity.ui.theme.LearnityTheme

/**
 * Écran de visualisation du contenu (PDF ou Vidéo)
 *
 * Version actuelle : Placeholder pour le développement front-end
 * TODO: Intégrer un vrai viewer PDF (WebView ou bibliothèque Android PDF)
 *
 * @param courseId ID du cours
 * @param chapterId ID du chapitre
 * @param type Type de contenu (cours, fdr, video)
 * @param viewModel ViewModel pour gérer les données
 * @param onMarkComplete Callback quand l'utilisateur marque le contenu comme terminé
 * @param onBackClick Callback pour retourner
 */
@Composable
fun PdfViewerScreen(
    courseId: String,
    chapterId: String,
    type: String,  // "cours", "fdr", ou "video"
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
                contentUrl != null -> {
                    ContentPlaceholder(
                        contentType = typeEnum,
                        contentUrl = contentUrl!!,
                        chapter = chapter
                    )
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
                                text = "📹 Vidéo explicative",
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
 * Placeholder pour le contenu (en attendant le vrai viewer)
 */
@Composable
private fun ContentPlaceholder(
    contentType: ContentType,
    contentUrl: String,
    chapter: Chapter?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône selon le type
        Icon(
            imageVector = when (contentType) {
                ContentType.VIDEO -> Icons.Default.PlayCircle
                else -> Icons.Default.PictureAsPdf
            },
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (contentType) {
                ContentType.VIDEO -> "Lecteur Vidéo"
                ContentType.FDR -> "Fiche de Révision"
                ContentType.COURS -> "Cours Complet"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        chapter?.let {
            when (contentType) {
                ContentType.VIDEO -> {
                    Text(
                        text = "Durée : ${it.videoDuration} minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "${it.pageCount} pages • ${it.estimatedReadTime} min de lecture",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    text = "📱 Version de développement",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (contentType) {
                        ContentType.VIDEO -> "Le lecteur YouTube sera intégré plus tard"
                        else -> "Le viewer PDF sera intégré plus tard"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "URL: ${contentUrl.take(50)}...",
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

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PdfViewerScreenPreview() {
    LearnityTheme {
        PdfViewerScreen(
            courseId = "extraction_connaissances",
            chapterId = "ec_chap1",
            type = "cours",
            onMarkComplete = {},
            onBackClick = {}
        )
    }
}