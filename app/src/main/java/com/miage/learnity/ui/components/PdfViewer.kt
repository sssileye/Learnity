package com.miage.learnity.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@Composable
fun PdfViewer(
    url: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
    onLoadComplete: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ ON STOCKE LE RENDERER, PAS TOUTES LES PAGES
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var parcelFileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }

    // Page courante
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Télécharger et initialiser le renderer
    LaunchedEffect(url) {
        isLoading = true
        scope.launch {
            try {
                val file = downloadPdf(url, context.cacheDir)

                withContext(Dispatchers.IO) {
                    val fd = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(fd)

                    withContext(Dispatchers.Main) {
                        parcelFileDescriptor = fd
                        pdfRenderer = renderer
                        totalPages = renderer.pageCount
                        onLoadComplete(renderer.pageCount)
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onError(e.message ?: "Erreur inconnue")
                isLoading = false
            }
        }
    }

    // ✅ CHARGER UNIQUEMENT LA PAGE COURANTE
    LaunchedEffect(pdfRenderer, currentPageIndex) {
        pdfRenderer?.let { renderer ->
            scope.launch(Dispatchers.IO) {
                try {
                    // Libérer l'ancien bitmap
                    currentBitmap?.recycle()

                    val page = renderer.openPage(currentPageIndex)

                    // ✅ HAUTE RÉSOLUTION pour texte net
                    val scaleFactor = 3 // Ajuste selon tes besoins (2-4)
                    val bitmap = Bitmap.createBitmap(
                        page.width * scaleFactor,
                        page.height * scaleFactor,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    page.close()

                    withContext(Dispatchers.Main) {
                        currentBitmap = bitmap
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onError("Erreur de rendu: ${e.message}")
                    }
                }
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            currentBitmap?.recycle()
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingView()
            errorMessage != null -> ErrorView(errorMessage!!)
            currentBitmap != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Barre de contrôle
                    PdfControlBar(
                        currentPage = currentPageIndex + 1,
                        totalPages = totalPages,
                        onPrevious = {
                            if (currentPageIndex > 0) currentPageIndex--
                        },
                        onNext = {
                            if (currentPageIndex < totalPages - 1) currentPageIndex++
                        }
                    )

                    // Zone d'affichage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PdfPageRenderer(bitmap = currentBitmap!!)
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageRenderer(bitmap: Bitmap) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 4f)
                    scale = newScale
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@Composable
private fun PdfControlBar(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPrevious,
                enabled = currentPage > 1
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, "Précédent")
            }

            Text(
                text = "Page $currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FilledTonalIconButton(
                onClick = onNext,
                enabled = currentPage < totalPages
            ) {
                Icon(Icons.Default.KeyboardArrowRight, "Suivant")
            }
        }
    }
}

// Fonctions utilitaires inchangées
private suspend fun downloadPdf(url: String, cacheDir: File): File = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    if (!response.isSuccessful) throw Exception("Erreur téléchargement")
    val file = File(cacheDir, "temp_pdf_${url.hashCode()}.pdf")
    response.body?.byteStream()?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    file
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chargement du PDF...")
        }
    }
}

@Composable
private fun ErrorView(msg: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("❌", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Erreur de chargement", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(msg, style = MaterialTheme.typography.bodyMedium)
        }
    }
}