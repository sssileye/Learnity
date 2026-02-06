package com.miage.learnity.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.ui.utils.ResponsiveDimensions
import com.miage.learnity.ui.utils.rememberResponsiveDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryFavoritesScreen(
    onBack: () -> Unit,
    onNavigateToChapter: (courseId: String, chapterId: String) -> Unit,
    onNavigateToCourse: (courseId: String) -> Unit,
    viewModel: LibraryFavoritesViewModel = viewModel()
) {
    val chapters by viewModel.favoriteChapters.collectAsState()
    val courses by viewModel.favoriteCourses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dims = rememberResponsiveDimensions()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chapitres", "Matières")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(
                            "Ma Bibliothèque",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = dims.titleLarge
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                modifier = Modifier.size(dims.iconSizeMedium)
                            )
                        }
                    }
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = dims.bodyLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (selectedTabIndex) {
                    0 -> FavoriteChaptersModule(chapters, dims, onNavigateToChapter)
                    1 -> FavoriteCoursesModule(courses, dims, onNavigateToCourse)
                }
            }
        }
    }
}

@Composable
fun FavoriteChaptersModule(
    chapters: List<Chapter>,
    dims: ResponsiveDimensions,
    onNavigate: (String, String) -> Unit
) {
    if (chapters.isEmpty()) {
        EmptyFavoritesState(dims, "Aucun chapitre favori", Icons.Default.Bookmark)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(dims.screenPaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(dims.itemSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            items(chapters) { chapter ->
                FavoriteItemCard(
                    title = chapter.title,
                    subtitle = "Chapitre",
                    dims = dims,

                    onClick = { onNavigate(chapter.courseId, chapter.chapterId) },
                    trailingContent = {

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (chapter.hasCours) Icon(Icons.Default.Description, null, Modifier.size(dims.iconSizeSmall), tint = MaterialTheme.colorScheme.primary)
                            if (chapter.hasVideo) Icon(Icons.Default.PlayCircle, null, Modifier.size(dims.iconSizeSmall), tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteCoursesModule(
    courses: List<Course>,
    dims: ResponsiveDimensions,
    onNavigate: (String) -> Unit
) {
    if (courses.isEmpty()) {
        EmptyFavoritesState(dims, "Aucune matière favorite", Icons.Default.Book)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(dims.screenPaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(dims.itemSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            items(courses) { course ->
                FavoriteItemCard(
                    title = course.title,
                    subtitle = "Matière",
                    dims = dims,
                    onClick = { onNavigate(course.id) }
                )
            }
        }
    }
}

@Composable
fun FavoriteItemCard(
    title: String,
    subtitle: String,
    dims: ResponsiveDimensions,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dims.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(dims.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dims.iconSizeLarge * 0.8f)
                    .clip(CircleShape)
                    .background(Color(0xFFF06292).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFF06292),
                    modifier = Modifier.size(dims.iconSizeMedium)
                )
            }
            Spacer(modifier = Modifier.width(dims.itemSpacing))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = dims.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subtitle, fontSize = dims.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(dims: ResponsiveDimensions, message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, Modifier.size(dims.iconSizeLarge * 1.5f), tint = Color.Gray.copy(0.3f))
        Spacer(modifier = Modifier.height(dims.itemSpacing))
        Text(message, fontSize = dims.bodyLarge, color = Color.Gray)
    }
}