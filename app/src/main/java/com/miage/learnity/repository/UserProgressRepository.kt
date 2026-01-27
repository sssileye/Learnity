package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
    enum class ContentType(val fieldName: String) {
        COURS("isCoursRead"),
        FDR("isFdrRead"),
        VIDEO("isVideoWatched"),
        QUIZ("isQuizCompleted")
    }

    // ============================================
    // ÉCRITURE
    // ============================================

    /**
     * Marque un contenu spécifique comme terminé (Cours, FDR, Vidéo, Quiz)
     */
    suspend fun markContentAsCompleted(
        courseId: String,
        chapterId: String,
        contentType: ContentType,
        quizType: com.miage.learnity.model.PointsManager.QuizType? = null // ✅ Ajout optionnel du type de quiz
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

            // Mise à jour Firestore
            progressRef.set(
                mapOf(contentType.fieldName to true),
                SetOptions.merge()
            ).await()

            // ✅ Notification enrichie pour le ProgressManager
            val progressType = when (contentType) {
                ContentType.QUIZ -> ProgressManager.ProgressType.QUIZ_COMPLETED
                ContentType.VIDEO -> ProgressManager.ProgressType.VIDEO_WATCHED
                else -> ProgressManager.ProgressType.CONTENT_READ
            }

            ProgressManager.notifyProgressChanged(
                courseId = courseId,
                chapterId = chapterId,
                type = progressType,
                quizType = quizType // Transmet le type (Chapter, Daily, Exam)
            )

            println("✅ UserProgressRepo - ${contentType.fieldName} validé pour $chapterId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ UserProgressRepo - Erreur écriture: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // 🔥 LISTENERS TEMPS RÉEL (Sécurisés)
    // ============================================

    /**
     * 🔥 Observe la progression d'un chapitre en temps réel
     */
    fun observeChapterProgress(
        courseId: String,
        chapterId: String
    ): Flow<ChapterProgressData> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(ChapterProgressData())
            return@callbackFlow
        }

        val docRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")
            .document(chapterId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Gestion sécurisée de la déconnexion
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    println("ℹ️ UserProgress - Accès révoqué (déconnexion)")
                    return@addSnapshotListener
                }
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
                trySend(progress)
            } else {
                trySend(ChapterProgressData()) // Retourne des flags à false par défaut
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * 🔥 Observe la progression de TOUS les chapitres d'un cours (utile pour l'Examen Blanc)
     */
    fun observeCourseProgress(
        courseId: String
    ): Flow<Map<String, ChapterProgressData>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyMap())
            return@callbackFlow
        }

        val collectionRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")

        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return@addSnapshotListener
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val progressMap = snapshot.documents.associate { doc ->
                    doc.id to ChapterProgressData(
                        isCoursRead = doc.getBoolean("isCoursRead") ?: false,
                        isFdrRead = doc.getBoolean("isFdrRead") ?: false,
                        isVideoWatched = doc.getBoolean("isVideoWatched") ?: false,
                        isQuizCompleted = doc.getBoolean("isQuizCompleted") ?: false
                    )
                }
                trySend(progressMap)
            }
        }

        awaitClose { listener.remove() }
    }

    // ============================================
    // LECTURE SIMPLE
    // ============================================

    suspend fun getChapterProgress(courseId: String, chapterId: String): ChapterProgressData {
        val userId = auth.currentUser?.uid ?: return ChapterProgressData()
        return try {
            val snapshot = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get()
                .await()

            ChapterProgressData(
                isCoursRead = snapshot.getBoolean("isCoursRead") ?: false,
                isFdrRead = snapshot.getBoolean("isFdrRead") ?: false,
                isVideoWatched = snapshot.getBoolean("isVideoWatched") ?: false,
                isQuizCompleted = snapshot.getBoolean("isQuizCompleted") ?: false
            )
        } catch (e: Exception) {
            ChapterProgressData()
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