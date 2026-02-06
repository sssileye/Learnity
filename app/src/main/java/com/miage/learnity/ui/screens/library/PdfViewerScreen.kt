package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.components.PdfViewer
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


    val typeEnum = remember(type) {
        when (type) {
            "fdr" -> UserProgressRepository.ContentType.FDR
            else -> UserProgressRepository.ContentType.COURS
        }
    }


    LaunchedEffect(courseId, chapterId, type) {
        viewModel.loadContent(courseId, chapterId, typeEnum)
    }

    val forcedLightColors = lightColorScheme(
        background = Color.White,
        surface = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black
    )


    MaterialTheme(colorScheme = forcedLightColors) {
        Scaffold(
            containerColor = Color.White,
            topBar = {
                PdfViewerTopBar(
                    title = if (typeEnum == UserProgressRepository.ContentType.FDR) "Fiche de Révision" else "Cours Complet",
                    onBackClick = onBackClick,
                    dimensions = dimensions
                )
            },
            bottomBar = {

                PdfViewerBottomBar(
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
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> LoadingContent(dimensions)
                    contentUrl != null -> {

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
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun PdfViewerBottomBar(
    isMarkedAsRead: Boolean,
    onMarkComplete: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.screenPaddingHorizontal)
                .navigationBarsPadding() // Évite les chevauchements avec la barre système
        ) {
            if (!isMarkedAsRead) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.buttonHeight),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "J'ai terminé la lecture", fontSize = dimensions.bodyLarge)
                }
            } else {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.buttonHeight),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lecture validée ✓",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
            Text("Ouverture du document...", fontSize = dimensions.bodyLarge)
        }
    }
}

@Composable
private fun ErrorContent(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Error, contentDescription = null, modifier = Modifier.size(dimensions.iconSizeLarge), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Le lien du PDF est introuvable", fontSize = dimensions.titleMedium)
        }
    }
}