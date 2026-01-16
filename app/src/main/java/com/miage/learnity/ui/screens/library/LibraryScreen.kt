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
import com.miage.learnity.data.Course
import com.miage.learnity.data.mock.MockData
import com.miage.learnity.ui.theme.LearnityTheme

/**
 * Écran Bibliothèque : Affiche la liste de tous les cours disponibles
 *
 * @param viewModel ViewModel qui gère les données
 * @param onCourseClick Callback appelé quand l'utilisateur clique sur un cours
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onCourseClick: (String) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            LibraryTopBar(
                onRefreshClick = { viewModel.refresh() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // État : Chargement
                isLoading -> {
                    LoadingState()
                }

                // État : Erreur
                error != null -> {
                    ErrorState(
                        message = error ?: "Erreur inconnue",
                        onRetry = { viewModel.refresh() }
                    )
                }

                // État : Liste vide
                courses.isEmpty() -> {
                    EmptyState()
                }

                // État : Affichage des cours
                else -> {
                    CoursesList(
                        courses = courses,
                        onCourseClick = onCourseClick
                    )
                }
            }
        }
    }
}

/**
 * Barre supérieure de l'écran Bibliothèque
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
    onRefreshClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Bibliothèque de Cours",
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualiser"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Liste des cours avec LazyColumn
 */
@Composable
private fun CoursesList(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(courses) { course ->
            CourseLibraryCard(
                course = course,
                onClick = { onCourseClick(course.id) }
            )
        }

        // Espace pour éviter que le dernier élément soit caché par la bottom bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Card représentant un cours dans la bibliothèque
 */
@Composable
fun CourseLibraryCard(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône du cours
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
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
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        ) // Icône par défaut }

                    }

                }


                Spacer(modifier = Modifier.width(16.dp))

                // Contenu texte
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Titre du cours
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description
                    if (course.description.isNotEmpty()) {
                        Text(
                            text = course.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bouton "Commencer"
                    Text(
                        text = "Commencer le cours →",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

    /**
     * État de chargement
     */
    @Composable
    private fun LoadingState() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chargement des cours...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    /**
     * État d'erreur
     */
    @Composable
    private fun ErrorState(
        message: String,
        onRetry: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "❌",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Erreur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRetry) {
                    Text("Réessayer")
                }
            }
        }
    }

    /**
     * État liste vide
     */
    @Composable
    private fun EmptyState() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "📚",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aucun cours disponible",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Les cours seront bientôt disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

// ============================================
// PREVIEWS
// ============================================

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun LibraryScreenPreview() {
        LearnityTheme {
            LibraryScreen(
                onCourseClick = {}
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun CourseLibraryCardPreview() {
        LearnityTheme {
            CourseLibraryCard(
                course = MockData.sampleCourses.first(),
                onClick = {}
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoadingStatePreview() {
        LearnityTheme {
            LoadingState()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ErrorStatePreview() {
        LearnityTheme {
            ErrorState(
                message = "Impossible de charger les cours. Vérifiez votre connexion.",
                onRetry = {}
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun EmptyStatePreview() {
        LearnityTheme {
            EmptyState()
        }
    }