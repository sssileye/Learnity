package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.model.PointsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository gérant la progression, les records et les FAVORIS
 */
class UserProgressRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    enum class ContentType(val fieldName: String) {
        COURS("isCoursRead"),
        FDR("isFdrRead"),
        VIDEO("isVideoWatched"),
        QUIZ("isQuizCompleted")
    }

    // ============================================
    // ⭐ GESTION DES FAVORIS
    // ============================================

    /**
     * Alterne l'état favori d'un cours (UE)
     * Stocké dans : user_progress/{userId}/courses/{courseId}
     */
    suspend fun toggleCourseFavorite(courseId: String, isFavorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .set(mapOf("isFavorite" to isFavorite), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Alterne l'état favori d'un chapitre
     * Stocké dans : user_progress/{userId}/courses/{courseId}/chapters/{chapterId}
     */
    suspend fun toggleChapterFavorite(courseId: String, chapterId: String, isFavorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .set(mapOf("isFavorite" to isFavorite), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // ÉCRITURE PROGRESSION
    // ============================================

    suspend fun markContentAsCompleted(
        courseId: String,
        chapterId: String,
        contentType: ContentType,
        quizType: PointsManager.QuizType? = null
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
                mapOf(contentType.fieldName to true),
                SetOptions.merge()
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // 🔥 LISTENERS TEMPS RÉEL (FLUX)
    // ============================================

    /**
     * Observe tous les chapitres d'un cours spécifique
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
                    doc.id to mapSnapshotToProgress(doc)
                }
                trySend(progressMap)
            } else {
                trySend(emptyMap())
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observe un seul chapitre
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
                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return@addSnapshotListener
                close(error)
                return@addSnapshotListener
            }
            val data = if (snapshot != null && snapshot.exists()) mapSnapshotToProgress(snapshot) else ChapterProgressData()
            trySend(data)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observe si une UE (Course) est en favori
     */
    fun observeCourseFavorite(courseId: String): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(false)
            return@callbackFlow
        }

        val docRef = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)

        val listener = docRef.addSnapshotListener { snapshot, _ ->
            val isFav = snapshot?.getBoolean("isFavorite") ?: false
            trySend(isFav)
        }
        awaitClose { listener.remove() }
    }

    // ============================================
    // LECTURE SIMPLE (SUSPEND)
    // ============================================

    suspend fun getChapterProgress(courseId: String, chapterId: String): Result<ChapterProgressData> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            val snapshot = firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get()
                .await()

            Result.success(if (snapshot.exists()) mapSnapshotToProgress(snapshot) else ChapterProgressData())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HELPER MAPPING ---
    private fun mapSnapshotToProgress(doc: com.google.firebase.firestore.DocumentSnapshot): ChapterProgressData {
        return ChapterProgressData(
            isCoursRead = doc.getBoolean("isCoursRead") ?: false,
            isFdrRead = doc.getBoolean("isFdrRead") ?: false,
            isVideoWatched = doc.getBoolean("isVideoWatched") ?: false,
            isQuizCompleted = doc.getBoolean("isQuizCompleted") ?: false,
            bestScore = doc.getLong("bestScore")?.toInt() ?: 0,
            isPerfectCompleted = doc.getBoolean("isPerfectCompleted") ?: false,
            isFavorite = doc.getBoolean("isFavorite") ?: false
        )
    }
}

/**
 * Data class enrichie pour la progression et le statut favori d'un chapitre
 */
data class ChapterProgressData(
    val isCoursRead: Boolean = false,
    val isFdrRead: Boolean = false,
    val isVideoWatched: Boolean = false,
    val isQuizCompleted: Boolean = false,
    val bestScore: Int = 0,
    val isPerfectCompleted: Boolean = false,
    val isFavorite: Boolean = false
)