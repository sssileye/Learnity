package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer la progression des utilisateurs
 */
class UserProgressRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ============================================
    // ENUM pour types de contenu
    // ============================================
    enum class ContentType {
        COURS, FDR, VIDEO
    }

    // ============================================
    // ÉCRITURE (avec notifications)
    // ============================================

    /**
     * Marque le contenu d'un chapitre comme lu
     */
    suspend fun markContentAsRead(
        courseId: String,
        chapterId: String,
        contentType: ContentType
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

            // ✅ Choisir le bon champ selon le type
            val fieldToUpdate = when (contentType) {
                ContentType.COURS -> "isCoursRead"
                ContentType.FDR -> "isFdrRead"
                ContentType.VIDEO -> return@withContext Result.failure(
                    Exception("Use markVideoAsWatched for videos")
                )
            }

            progressRef.set(
                mapOf(fieldToUpdate to true),
                SetOptions.merge()
            ).await()

            // Notification locale immédiate
            ProgressManager.notifyProgressChanged(
                courseId,
                chapterId,
                ProgressManager.ProgressType.CONTENT_READ
            )

            println("✅ UserProgressRepo - $fieldToUpdate marked for $courseId/$chapterId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ UserProgressRepo - Error: ${e.message}")
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

            // Notification locale immédiate
            ProgressManager.notifyProgressChanged(
                courseId,
                chapterId,
                ProgressManager.ProgressType.VIDEO_WATCHED
            )

            println("✅ UserProgressRepo - Video marked as watched: $courseId/$chapterId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ UserProgressRepo - Error: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // LECTURE SIMPLE (une seule fois)
    // ============================================

    /**
     * Récupère la progression d'un chapitre (lecture unique)
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
                isCoursRead = snapshot.getBoolean("isCoursRead") ?: false,
                isFdrRead = snapshot.getBoolean("isFdrRead") ?: false,
                isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
            )

            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère la progression de tous les chapitres d'un cours (lecture unique)
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
                    isCoursRead = snapshot.getBoolean("isCoursRead") ?: false,
                    isFdrRead = snapshot.getBoolean("isFdrRead") ?: false,
                    isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                    isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
                )
            }

            Result.success(progressMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 🔥 LISTENERS TEMPS RÉEL
    // ============================================

    /**
     * 🔥 Observe la progression d'un chapitre en temps réel
     */
    fun observeChapterProgress(
        courseId: String,
        chapterId: String
    ): Flow<ChapterProgressData> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        val docRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")
            .document(chapterId)

        // 🔥 Ajouter le listener
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Listener error: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val progress = ChapterProgressData(
                    isCoursRead = snapshot.getBoolean("isCoursRead") ?: false,
                    isFdrRead = snapshot.getBoolean("isFdrRead") ?: false,
                    isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                    isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
                )
                println("🔥 Firebase Listener - Chapter progress updated: $courseId/$chapterId")
                trySend(progress)
            } else {
                // Pas encore de progression
                trySend(ChapterProgressData())
            }
        }

        // Cleanup quand le flow est annulé
        awaitClose {
            println("🔥 Firebase Listener - Removed for $courseId/$chapterId")
            listener.remove()
        }
    }

    /**
     * 🔥 Observe la progression de tous les chapitres d'un cours en temps réel
     */
    fun observeCourseProgress(
        courseId: String
    ): Flow<Map<String, ChapterProgressData>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        val collectionRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")

        // 🔥 Ajouter le listener sur toute la collection
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Listener error: ${error.message}")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val progressMap = mutableMapOf<String, ChapterProgressData>()

                snapshot.documents.forEach { doc ->
                    val chapterId = doc.id
                    progressMap[chapterId] = ChapterProgressData(
                        isCoursRead = doc.getBoolean("isCoursRead") ?: false,
                        isFdrRead = doc.getBoolean("isFdrRead") ?: false,
                        isVideoWatched = doc.getBoolean("isVideoWatched") ?: false,
                        isQuizCompleted = doc.getBoolean("isQuizCompleted") ?: false
                    )
                }

                println("🔥 Firebase Listener - Course progress updated: $courseId (${progressMap.size} chapters)")
                trySend(progressMap)
            }
        }

        // Cleanup
        awaitClose {
            println("🔥 Firebase Listener - Removed for course $courseId")
            listener.remove()
        }
    }
}

/**
 * Data class pour la progression d'un chapitre
 */
data class ChapterProgressData(
    val isCoursRead: Boolean = false,
    val isFdrRead: Boolean = false,
    val isVideoWatched: Boolean = false,
    val isQuizCompleted: Boolean = false
)