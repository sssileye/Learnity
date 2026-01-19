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
    var pdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // État de la page actuelle
    var currentPageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(url) {
        isLoading = true
        scope.launch {
            try {
                val file = downloadPdf(url, context.cacheDir)
                val pages = renderPdfPages(file)
                pdfPages = pages
                onLoadComplete(pages.size)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                onError(e.message ?: "Erreur inconnue")
                isLoading = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingView()
            errorMessage != null -> ErrorView(errorMessage!!)
            pdfPages.isNotEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 1. Barre de navigation (Boutons + Indicateur)
                    PdfControlBar(
                        currentPage = currentPageIndex + 1,
                        totalPages = pdfPages.size,
                        onPrevious = { if (currentPageIndex > 0) currentPageIndex-- },
                        onNext = { if (currentPageIndex < pdfPages.size - 1) currentPageIndex++ }
                    )

                    // 2. Zone d'affichage de la page (Une seule page à la fois)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PdfPageRenderer(bitmap = pdfPages[currentPageIndex])
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

// --- Fonctions utilitaires (Download & Render) ---

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

private suspend fun renderPdfPages(file: File): List<Bitmap> = withContext(Dispatchers.IO) {
    val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(fd)
    val pages = (0 until renderer.pageCount).map { i ->
        val page = renderer.openPage(i)
        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap
    }
    renderer.close()
    fd.close()
    pages
}

@Composable private fun LoadingView() { /* Ton code existant */ }
@Composable private fun ErrorView(msg: String) { /* Ton code existant */ }