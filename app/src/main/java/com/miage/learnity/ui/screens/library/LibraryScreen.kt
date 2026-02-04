package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import com.miage.learnity.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Course
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.*

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onCourseClick: (String) -> Unit
) {
    val dimensions = rememberResponsiveDimensions()

    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingState(dimensions)
            error != null -> ErrorState(error ?: "Erreur inconnue", { viewModel.refresh() }, dimensions)
            courses.isEmpty() && !isLoading -> EmptyState(dimensions)
            else -> CoursesList(
                courses = courses,
                currentSortOrder = currentSortOrder,
                viewModel = viewModel,
                onCourseClick = onCourseClick,
                onRefresh = { viewModel.refresh() },
                dimensions = dimensions
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoursesList(
    courses: List<Course>,
    currentSortOrder: CourseSortOrder,
    viewModel: LibraryViewModel,
    onCourseClick: (String) -> Unit,
    onRefresh: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // --- BARRE DE TRI RESPONSIVE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPaddingHorizontal, vertical = dimensions.itemSpacing / 2)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CourseSortOrder.entries.forEach { order ->
                FilterChip(
                    selected = currentSortOrder == order,
                    onClick = { viewModel.updateSortOrder(order) },
                    label = {
                        Text(
                            text = when(order) {
                                CourseSortOrder.ALPHABETICAL -> "A-Z"
                                CourseSortOrder.FAVORITES -> "Favoris"
                                CourseSortOrder.PROGRESSION -> "Progression"
                            },
                            fontSize = dimensions.bodySmall,
                            fontWeight = if (currentSortOrder == order) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                )
            }
        }

        // --- LISTE DES COURS ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = dimensions.screenPaddingHorizontal,
                end = dimensions.screenPaddingHorizontal,
                top = 4.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.itemSpacing / 2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bibliothèque",
                        fontSize = dimensions.titleLarge,
                        fontWeight = FontWeight.Black,
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

            items(courses, key = { it.id }) { course ->
                CourseLibraryCard(
                    course = course,
                    onFavoriteToggle = { viewModel.toggleFavorite(course.id, course.isFavorite) },
                    onClick = { onCourseClick(course.id) },
                    dimensions = dimensions
                )
            }
        }
    }
}

@Composable
fun CourseLibraryCard(
    course: Course,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(dimensions.iconSizeMedium * 1.4f),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSizeMedium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.itemSpacing))

            Text(
                text = course.title,
                fontSize = (dimensions.titleMedium.value * 0.85).sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                lineHeight = (dimensions.titleMedium.value * 1.0).sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(dimensions.iconSizeMedium * 1.2f)
            ) {
                Icon(
                    imageVector = if (course.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (course.isFavorite) Color(0xFFF06292) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            }
        }
    }
}

@Composable
private fun LoadingState(dimensions: ResponsiveDimensions) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(dimensions.iconSizeLarge),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
            Text(
                text = "Chargement...",
                fontSize = dimensions.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(
        modifier = Modifier.fillMaxSize().padding(dimensions.screenPaddingHorizontal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "⚠️", fontSize = dimensions.displayLarge)
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        Text(
            text = message,
            fontSize = dimensions.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(dimensions.cornerRadiusSmall)
        ) {
            Text("RÉESSAYER", fontSize = dimensions.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(dimensions: ResponsiveDimensions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📚", fontSize = dimensions.displayLarge)
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        Text(
            text = "Aucun cours ici",
            fontSize = dimensions.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}