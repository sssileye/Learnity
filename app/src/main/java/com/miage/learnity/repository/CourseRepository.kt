package com.miage.learnity.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les cours et chapitres
 * Interagit avec Firestore
 */
class CourseRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Récupère tous les cours avec statut favori
     */
    suspend fun getAllCourses(userId: String? = null): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("courses").get().await()

            // Utilisation du chemin user_progress pour la cohérence
            val favoriteIds = if (userId != null) {
                firestore.collection("user_progress").document(userId)
                    .collection("courses")
                    .whereEqualTo("isFavorite", true)
                    .get().await()
                    .documents.map { it.id }.toSet()
            } else emptySet()

            val courses = snapshot.documents.mapNotNull { doc ->
                Course(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    iconRes = doc.getLong("iconRes")?.toInt(),
                    isFavorite = favoriteIds.contains(doc.id)
                )
            }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère un cours spécifique avec son statut favori
     */
    suspend fun getCourse(courseId: String, userId: String? = null): Result<Course> =
        withContext(Dispatchers.IO) {
            try {
                if (courseId.isBlank()) return@withContext Result.failure(Exception("ID vide"))

                val courseDoc = firestore.collection("courses").document(courseId).get().await()
                if (!courseDoc.exists()) return@withContext Result.failure(Exception("Course not found"))

                var isFavorite = false
                if (userId != null) {
                    val progressDoc = firestore.collection("user_progress").document(userId)
                        .collection("courses").document(courseId).get().await()
                    isFavorite = progressDoc.getBoolean("isFavorite") ?: false
                }

                val course = Course(
                    id = courseDoc.id,
                    title = courseDoc.getString("title") ?: "",
                    description = courseDoc.getString("description") ?: "",
                    iconRes = courseDoc.getLong("iconRes")?.toInt(),
                    isFavorite = isFavorite
                )
                Result.success(course)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Récupère les chapitres d'un cours avec statut favori
     * SIGNATURE UNIQUE : userId est optionnel
     */
    suspend fun getChapters(courseId: String, userId: String? = null): Result<List<Chapter>> =
        withContext(Dispatchers.IO) {
            try {
                if (courseId.isBlank()) return@withContext Result.failure(Exception("ID vide"))

                // 1. Données globales
                val chaptersSnapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .orderBy("order")
                    .get()
                    .await()

                // 2. Favoris (depuis user_progress pour cohérence)
                val favoriteChapterIds = if (userId != null) {
                    firestore.collection("user_progress").document(userId)
                        .collection("courses").document(courseId)
                        .collection("chapters")
                        .whereEqualTo("isFavorite", true)
                        .get().await()
                        .documents.map { it.id }.toSet()
                } else emptySet()

                val chapters = chaptersSnapshot.documents.mapIndexed { index, doc ->
                    val coursUrl = doc.getString("coursUrl") ?: doc.getString("cours")
                    val fdrUrl = doc.getString("fdrUrl") ?: doc.getString("fdr")
                    val videoUrl = doc.getString("videoUrl") ?: doc.getString("video")
                    val quizId = doc.getString("quizId") ?: doc.getString("quiz")

                    Chapter(
                        chapterId = doc.id,
                        title = doc.getString("title") ?: "Chapitre ${index + 1}",
                        order = doc.getLong("order")?.toInt() ?: (index + 1),
                        coursUrl = coursUrl?.takeIf { it.isNotBlank() },
                        fdrUrl = fdrUrl?.takeIf { it.isNotBlank() },
                        videoUrl = videoUrl?.takeIf { it.isNotBlank() },
                        pageCount = doc.getLong("pageCount")?.toInt() ?: 0,
                        estimatedReadTime = doc.getLong("estimatedReadTime")?.toInt() ?: 0,
                        videoDuration = doc.getLong("videoDuration")?.toInt() ?: 0,
                        quizId = quizId?.takeIf { it.isNotBlank() },
                        isFavorite = favoriteChapterIds.contains(doc.id),
                        isCoursRead = false,
                        isFdrRead = false,
                        isVideoWatched = false,
                        isQuizCompleted = false
                    )
                }
                Result.success(chapters)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Récupère un chapitre spécifique
     */
    suspend fun getChapter(courseId: String, chapterId: String, userId: String? = null): Result<Chapter> =
        withContext(Dispatchers.IO) {
            try {
                val doc = firestore.collection("courses").document(courseId)
                    .collection("chapters").document(chapterId).get().await()

                if (!doc.exists()) return@withContext Result.failure(Exception("Not found"))

                var isFavorite = false
                if (userId != null) {
                    val favDoc = firestore.collection("user_progress").document(userId)
                        .collection("courses").document(courseId)
                        .collection("chapters").document(chapterId).get().await()
                    isFavorite = favDoc.getBoolean("isFavorite") ?: false
                }

                val coursUrl = doc.getString("coursUrl") ?: doc.getString("cours")

                Result.success(Chapter(
                    chapterId = doc.id,
                    title = doc.getString("title") ?: "",
                    order = doc.getLong("order")?.toInt() ?: 0,
                    coursUrl = coursUrl,
                    quizId = doc.getString("quizId"),
                    isFavorite = isFavorite
                ))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}