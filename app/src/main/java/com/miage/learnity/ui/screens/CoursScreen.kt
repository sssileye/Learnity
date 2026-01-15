package com.miage.learnity.ui.screens


/*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Database
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress


// Couleurs
private val PrimaryBlue = Color(0xFF3949AB)
private val PrimaryPurple = Color(0xFF5E35B1)
private val BackgroundLight = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val CompletedGreen = Color(0xFF4CAF50)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF757575)
private val StreakOrange = Color(0xFFFF6B35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursScreen(
    course: Course,
    onChapterClick: (Chapter) -> Unit = {},
    onQuizClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    streakDays: Int = 3
){
    val progress = remember(course) {
        CourseProgress(
            completedChapters = course.chapters.count { it.isCompleted },
            totalChapters = course.chapters.size
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "LEARNITY",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(Modifier.weight(1f))

                        // Streak
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = StreakOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "J+$streakDays",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Avatar
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête du cours
            item {
                CourseHeader(
                    courseTitle = course.title,
                    moduleTitle = course.module,
                    progress = progress
                )
            }

            // Liste des chapitres
            items(course.chapters) { chapter ->
                ChapterCard(
                    chapter = chapter,
                    onClick = { onChapterClick(chapter) }
                )
            }

            // Bouton Quiz du chapitre (si progression partielle)
            if (progress.completedChapters > 0 && !progress.isAllCompleted) {
                item {
                    QuizButton(
                        text = "Lancer le Quiz du Chapitre",
                        enabled = true,
                        progress = progress.percentage,
                        onClick = onQuizClick
                    )
                }
            }

            // Quiz final (si tout complété)
            if (progress.isAllCompleted) {
                item {
                    FinalQuizCard(onClick = onQuizClick)
                }
            }

            // Espacement pour la bottom bar
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun CourseHeader(
    courseTitle: String,
    moduleTitle: String,
    progress: CourseProgress
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryBlue
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = courseTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Module : $moduleTitle",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(16.dp))

            // Barre de progression
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progression",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${progress.completedChapters}/${progress.totalChapters}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône du chapitre
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getChapterIcon(chapter),
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Titre du chapitre
            Text(
                text = chapter.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            // Indicateur de complétion
            if (chapter.isCompleted) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = CompletedGreen
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complété",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuizButton(
    text: String,
    enabled: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Barre de progression
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = PrimaryPurple,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                disabledContainerColor = Color.LightGray,
                disabledContentColor = Color.Gray
            )
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FinalQuizCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryPurple
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quiz Final Débloqué !",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tous les chapitres terminés",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

// Helper function pour les icônes
private fun getChapterIcon(chapter: Chapter): ImageVector {
    return when {
        chapter.title.contains("Extraction", ignoreCase = true) -> Icons.Default.Dataset
        chapter.title.contains("Clustering", ignoreCase = true) -> Icons.Default.AccountTree
        chapter.title.contains("Arbre", ignoreCase = true) -> Icons.Default.DeviceHub
        chapter.title.contains("Association", ignoreCase = true) -> Icons.Default.Link
        chapter.title.contains("Management", ignoreCase = true) -> Icons.Default.ManageAccounts
        else -> Icons.Default.Book
    }
}

@Preview(showBackground = true)
@Composable
fun CoursScreenPreview() {
    val sampleCourse = Course(
        id = "1",
        title = "Mes Matières MIAGE",
        module = "Architecture Logileire",
        chapters = listOf(
            Chapter("1", "Extraction de Bases de Dondaines", 0),
            Chapter("2", "Clustering", 0, isVideoWatched = false),
            Chapter("3", "Clustering", 0, isVideoWatched = true, isQuizCompleted = true),
            Chapter("4", "Arbes de Décision", 0, isContentRead = true, isQuizCompleted = true),
            Chapter("5", "Association Rules", 0, isVideoWatched = true, isQuizCompleted = true)
        ),
        iconRes = 0
    )

    MaterialTheme {
        CoursScreen(course = sampleCourse)
    }
}*/