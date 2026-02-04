package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.model.PointsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository gérant la progression, les records et les FAVORIS
 * Centralise les données utilisateur dans la collection "user_progress"
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
    // ⭐ GESTION DES FAVORIS (Dénormalisée)
    // ============================================

    /**
     * Alterne l'état favori d'un cours (UE)
     * On duplique le titre pour que la bibliothèque soit autonome
     */
    suspend fun toggleCourseFavorite(course: Course, isFavorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            val data = mapOf(
                "isFavorite" to isFavorite,
                "title" to course.title,
                "description" to course.description
            )

            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(course.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Alterne l'état favori d'un chapitre
     * On duplique toutes les infos pour éviter les "Sans titre" dans la bibliothèque
     */
    suspend fun toggleChapterFavorite(
        courseId: String,
        chapter: Chapter,
        isFavorite: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("User non connecté"))
        try {
            // ✅ Enregistrement complet pour la requête CollectionGroup de la bibliothèque
            val data = mapOf(
                "isFavorite" to isFavorite,
                "title" to chapter.title,
                "cours" to chapter.cours,
                "fdr" to chapter.fdr,
                "video" to chapter.video,
                "courseId" to courseId
            )

            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapter.chapterId)
                .set(data, SetOptions.merge())
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
        quizType: PointsManager.QuizType? = null // Conservé pour compatibilité
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

    fun observeCourseProgress(courseId: String): Flow<Map<String, ChapterProgressData>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(emptyMap())
            return@callbackFlow
        }

        val listener = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return@addSnapshotListener
                    close(error)
                    return@addSnapshotListener
                }

                val progressMap = snapshot?.documents?.associate { doc ->
                    doc.id to mapSnapshotToProgress(doc)
                } ?: emptyMap()
                trySend(progressMap)
            }
        awaitClose { listener.remove() }
    }

    fun observeChapterProgress(courseId: String, chapterId: String): Flow<ChapterProgressData> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(ChapterProgressData())
            return@callbackFlow
        }

        val listener = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .collection("chapters")
            .document(chapterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val data = if (snapshot != null && snapshot.exists()) mapSnapshotToProgress(snapshot) else ChapterProgressData()
                trySend(data)
            }
        awaitClose { listener.remove() }
    }

    fun observeCourseFavorite(courseId: String): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(false)
            return@callbackFlow
        }

        val listener = firestore.collection("user_progress")
            .document(userId)
            .collection("courses")
            .document(courseId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.getBoolean("isFavorite") ?: false)
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