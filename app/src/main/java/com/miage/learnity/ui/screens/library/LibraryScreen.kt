package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.R
import com.miage.learnity.data.Course
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.*

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onCourseClick: (String) -> Unit
) {
    // ✅ DIMENSIONS RESPONSIVES
    val dimensions = rememberResponsiveDimensions()

    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ PAS DE SCAFFOLD - Utilise le TopNavigationBar global
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingState(dimensions)
            error != null -> ErrorState(error ?: "Erreur inconnue", { viewModel.refresh() }, dimensions)
            courses.isEmpty() -> EmptyState(dimensions)
            else -> CoursesList(courses, onCourseClick, viewModel::refresh, dimensions)
        }
    }
}

@Composable
private fun CoursesList(
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    onRefresh: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimensions.screenPaddingHorizontal,
            end = dimensions.screenPaddingHorizontal,
            top = 4.dp,  // ✅ ESPACE MINIMAL pour coller à la TopBar
            bottom = dimensions.screenPaddingHorizontal
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // ✅ HEADER avec titre et bouton refresh
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),  // ✅ RÉDUIT pour moins d'espace
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bibliothèque de Cours",
                    fontSize = dimensions.titleLarge,  // ✅ 28.ssp()
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        modifier = Modifier.size(dimensions.iconSizeMedium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items(courses) { course ->
            CourseLibraryCard(
                course = course,
                onClick = {
                    println("📕 Clic sur cours - ID : '${course.id}'")
                    onCourseClick(course.id)
                },
                dimensions = dimensions
            )
        }

        item {
            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 5))  // ✅ Responsive
        }
    }
}

@Composable
fun CourseLibraryCard(
    course: Course,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),  // ✅ 12.dp
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.cardPadding),  // ✅ Responsive
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(dimensions.iconSizeLarge * 1.2f),  // ✅ 56.sdp()
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (course.iconRes != null && course.iconRes != 0) {
                        Icon(
                            painter = painterResource(id = course.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSizeLarge),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSizeLarge),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = dimensions.titleMedium
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 4))

                if (course.description.isNotEmpty()) {
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = dimensions.bodyMedium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

                Text(
                    text = "Commencer le cours →",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = dimensions.bodyLarge
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSizeLarge))
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(
                text = "Chargement des cours...",
                fontSize = dimensions.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(dimensions.screenPaddingHorizontal * 2)
        ) {
            Text(text = "❌", fontSize = dimensions.displayLarge)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Erreur", fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
            Text(text = message, fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))
            Button(onClick = onRetry, modifier = Modifier.height(dimensions.buttonHeightSmall)) {
                Text("Réessayer", fontSize = dimensions.bodyLarge)
            }
        }
    }
}

@Composable
private fun EmptyState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(dimensions.screenPaddingHorizontal * 2)
        ) {
            Text(text = "📚", fontSize = dimensions.displayLarge)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(text = "Aucun cours disponible", fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
            Text(text = "Les cours seront bientôt disponibles", fontSize = dimensions.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun LibraryScreenPreview() {
    LearnityTheme {
        LibraryScreen(onCourseClick = {})
    }
}