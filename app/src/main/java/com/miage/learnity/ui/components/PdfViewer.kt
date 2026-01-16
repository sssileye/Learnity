package com.miage.learnity.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Composable pour afficher un PDF depuis une URL
 * Utilise PdfRenderer natif Android
 */
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

    // Télécharger et rendre le PDF au premier rendu
    LaunchedEffect(url) {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val file = downloadPdf(url, context.cacheDir)
                val pages = renderPdfPages(file)
                pdfPages = pages
                onLoadComplete(pages.size)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Erreur : ${e.message}"
                onError(errorMessage!!)
                isLoading = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                // État de chargement
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Téléchargement et rendu du PDF...")
                }
            }

            errorMessage != null -> {
                // État d'erreur
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "❌ Erreur",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            pdfPages.isNotEmpty() -> {
                // Affichage du PDF page par page
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(pdfPages) { index, bitmap ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Page ${index + 1}",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Numéro de page
                                Text(
                                    text = "Page ${index + 1} / ${pdfPages.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Télécharge un PDF depuis une URL et le sauvegarde en cache
 */
private suspend fun downloadPdf(url: String, cacheDir: File): File =
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Erreur HTTP : ${response.code}")
        }

        // Créer un fichier temporaire
        val fileName = "pdf_${url.hashCode()}.pdf"
        val file = File(cacheDir, fileName)

        // Sauvegarder le PDF
        response.body?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("✅ PDF téléchargé : ${file.absolutePath} (${file.length()} bytes)")
        file
    }

/**
 * Rend toutes les pages du PDF en Bitmaps
 * Utilise PdfRenderer natif Android
 */
private suspend fun renderPdfPages(file: File): List<Bitmap> =
    withContext(Dispatchers.IO) {
        val fileDescriptor = ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )

        val pdfRenderer = PdfRenderer(fileDescriptor)
        val pageCount = pdfRenderer.pageCount

        println("📄 Rendu du PDF : $pageCount pages")

        val pages = mutableListOf<Bitmap>()

        for (i in 0 until pageCount) {
            val page = pdfRenderer.openPage(i)

            // Créer un bitmap avec une bonne résolution
            val bitmap = Bitmap.createBitmap(
                page.width * 2,  // x2 pour meilleure qualité
                page.height * 2,
                Bitmap.Config.ARGB_8888
            )

            // Rendre la page
            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            pages.add(bitmap)
            page.close()

            println("✅ Page ${i + 1}/$pageCount rendue")
        }

        pdfRenderer.close()
        fileDescriptor.close()

        pages
    }