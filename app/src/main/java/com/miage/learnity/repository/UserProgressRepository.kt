package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer la progression des utilisateurs
 */
class UserProgressRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Marque le contenu d'un chapitre comme lu
     * @param courseId ID du cours
     * @param chapterId ID du chapitre
     */
    suspend fun markContentAsRead(
        courseId: String,
        chapterId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non authentifié !"))

            val progressRef = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)

            progressRef.set(
                mapOf("isContentRead" to true),
                SetOptions.merge()
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marque une vidéo comme vue
     */
    suspend fun markVideoAsWatched(
        courseId: String,
        chapterId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val progressRef = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)

            progressRef.set(
                mapOf("isVideoWatched" to true),
                SetOptions.merge()
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère la progression d'un chapitre
     */
    suspend fun getChapterProgress(
        courseId: String,
        chapterId: String
    ): Result<ChapterProgressData> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val snapshot = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get()
                .await()

            val progress = ChapterProgressData(
                isContentRead = snapshot.getBoolean("isContentRead") ?: false,
                isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
            )

            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère la progression de tous les chapitres d'un cours
     */
    suspend fun getCourseProgress(
        courseId: String,
        chapterIds: List<String>
    ): Result<Map<String, ChapterProgressData>> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val progressMap = mutableMapOf<String, ChapterProgressData>()

            chapterIds.forEach { chapterId ->
                val snapshot = firestore.collection("user_progress")
                    .document(userId)
                    .collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get()
                    .await()

                progressMap[chapterId] = ChapterProgressData(
                    isContentRead = snapshot.getBoolean("isContentRead") ?: false,
                    isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                    isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
                )
            }

            Result.success(progressMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class pour la progression d'un chapitre
 */
data class ChapterProgressData(
    val isContentRead: Boolean = false,
    val isVideoWatched: Boolean = false,
    val isQuizCompleted: Boolean = false
)

