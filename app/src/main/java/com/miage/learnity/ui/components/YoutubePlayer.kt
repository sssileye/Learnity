package com.miage.learnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Composable pour afficher une vidéo YouTube
 *
 * @param videoUrl URL YouTube complète (ex: "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
 * @param onVideoEnd Callback appelé quand la vidéo se termine
 * @param onError Callback en cas d'erreur
 */
@Composable
fun YouTubePlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onVideoEnd: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Extraire l'ID YouTube de l'URL
    val videoId = remember(videoUrl) {
        extractYouTubeId(videoUrl)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (videoId == null) {
            // Erreur : URL invalide
            ErrorState(message = "URL YouTube invalide")
        } else if (isError) {
            // Erreur de chargement
            ErrorState(message = errorMessage)
        } else {
            // Afficher le lecteur YouTube
            AndroidView(
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        lifecycleOwner.lifecycle.addObserver(this)

                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                super.onReady(youTubePlayer)
                                youTubePlayer.loadVideo(videoId, 0f)
                                println("✅ YouTube Player ready - Video ID: $videoId")
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
                            ) {
                                super.onStateChange(youTubePlayer, state)

                                // Détecter la fin de la vidéo
                                if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED) {
                                    println("✅ Vidéo terminée")
                                    onVideoEnd()
                                }
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError
                            ) {
                                super.onError(youTubePlayer, error)
                                errorMessage = "Erreur de lecture : $error"
                                isError = true
                                onError(errorMessage)
                                println("❌ YouTube Player error: $error")
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Gérer le lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Le YouTubePlayerView gère automatiquement le lifecycle
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/**
 * Extrait l'ID YouTube depuis différents formats d'URL
 *
 * Formats supportés :
 * - https://www.youtube.com/watch?v=VIDEO_ID
 * - https://youtu.be/VIDEO_ID
 * - https://www.youtube.com/embed/VIDEO_ID
 * - https://www.youtube.com/v/VIDEO_ID
 */
private fun extractYouTubeId(url: String): String? {
    return try {
        when {
            // Format: youtube.com/watch?v=VIDEO_ID
            url.contains("youtube.com/watch?v=") -> {
                url.substringAfter("watch?v=").substringBefore("&")
            }
            // Format: youtu.be/VIDEO_ID
            url.contains("youtu.be/") -> {
                url.substringAfter("youtu.be/").substringBefore("?")
            }
            // Format: youtube.com/embed/VIDEO_ID
            url.contains("youtube.com/embed/") -> {
                url.substringAfter("embed/").substringBefore("?")
            }
            // Format: youtube.com/v/VIDEO_ID
            url.contains("youtube.com/v/") -> {
                url.substringAfter("v/").substringBefore("?")
            }
            // Si c'est déjà juste l'ID (11 caractères)
            url.length == 11 && !url.contains("/") -> {
                url
            }
            else -> null
        }
    } catch (e: Exception) {
        println("❌ Erreur extraction YouTube ID: ${e.message}")
        null
    }
}

/**
 * État d'erreur
 */
@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Erreur de lecture",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}