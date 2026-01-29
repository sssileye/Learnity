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
 * Repository pour gérer la progression des utilisateurs et leurs records
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
     * Marque un contenu comme terminé.
     * Note : Pour le QUIZ, les points et scores sont gérés par le UserRepository.updateStatsWithHighscore
     */
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
    // 🔥 LISTENERS TEMPS RÉEL (Sécurisés)
    // ============================================

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

            if (snapshot != null && snapshot.exists()) {
                trySend(mapSnapshotToProgress(snapshot))
            } else {
                trySend(ChapterProgressData())
            }
        }
        awaitClose { listener.remove() }
    }

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
            }
        }
        awaitClose { listener.remove() }
    }

    // ============================================
    // LECTURE SIMPLE
    // ============================================

    /**
     * ✅ CORRIGÉ : Retourne un Result pour être compatible avec QuizViewModel.onSuccess
     */
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

            if (snapshot.exists()) {
                Result.success(mapSnapshotToProgress(snapshot))
            } else {
                Result.success(ChapterProgressData())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- HELPER ---
    private fun mapSnapshotToProgress(doc: com.google.firebase.firestore.DocumentSnapshot): ChapterProgressData {
        return ChapterProgressData(
            isCoursRead = doc.getBoolean("isCoursRead") ?: false,
            isFdrRead = doc.getBoolean("isFdrRead") ?: false,
            isVideoWatched = doc.getBoolean("isVideoWatched") ?: false,
            isQuizCompleted = doc.getBoolean("isQuizCompleted") ?: false,
            // ✅ Ajout des nouveaux champs indispensables
            bestScore = doc.getLong("bestScore")?.toInt() ?: 0,
            isPerfectCompleted = doc.getBoolean("isPerfectCompleted") ?: false
        )
    }
}

/**
 * Data class enrichie pour la progression d'un chapitre
 */
data class ChapterProgressData(
    val isCoursRead: Boolean = false,
    val isFdrRead: Boolean = false,
    val isVideoWatched: Boolean = false,
    val isQuizCompleted: Boolean = false,
    val bestScore: Int = 0, // ✅ Record de l'utilisateur
    val isPerfectCompleted: Boolean = false // ✅ Flag pour le bonus unique
)