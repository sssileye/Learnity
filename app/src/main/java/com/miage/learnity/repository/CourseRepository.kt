package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // On récupère l'ID de l'utilisateur actuel pour chercher SA progression
    private val userId: String? get() = auth.currentUser?.uid

    /**
     * Récupère tous les cours
     */
    suspend fun getAllCourses(): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("courses").get().await()
            val courses = snapshot.documents.mapNotNull { doc ->
                Course(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    iconRes = doc.getLong("iconRes")?.toInt()
                )
            }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère un cours spécifique
     */
    suspend fun getCourse(courseId: String): Result<Course> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("courses").document(courseId).get().await()
            if (snapshot.exists()) {
                val course = Course(
                    id = snapshot.id,
                    title = snapshot.getString("title") ?: "",
                    description = snapshot.getString("description") ?: "",
                    iconRes = snapshot.getLong("iconRes")?.toInt()
                )
                Result.success(course)
            } else {
                Result.failure(Exception("Course not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère les chapitres d'un cours AVEC la progression utilisateur
     */
    suspend fun getChapters(courseId: String): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        try {
            val uId = userId ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            // 1. On récupère les chapitres (infos fixes)
            val chaptersSnapshot = firestore.collection("courses")
                .document(courseId)
                .collection("chapters")
                .orderBy("order")
                .get().await()

            // 2. On récupère TOUTE la progression de l'utilisateur pour ce cours d'un coup
            val progressSnapshot = firestore.collection("user_progress")
                .document(uId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .get().await()

            // On crée une map [ID_CHAPITRE -> DOCUMENT_PROGRESSION] pour un accès rapide
            val progressMap = progressSnapshot.documents.associateBy { it.id }

            val chapters = chaptersSnapshot.documents.mapIndexed { index, doc ->
                mapToChapter(doc, index, progressMap[doc.id])
            }

            Result.success(chapters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère un chapitre spécifique AVEC sa progression
     */
    suspend fun getChapter(courseId: String, chapterId: String): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            val uId = userId ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            // Récupération simultanée (conceptuelle) des deux documents
            val chapterDoc = firestore.collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get().await()

            val progressDoc = firestore.collection("user_progress")
                .document(uId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get().await()

            if (chapterDoc.exists()) {
                Result.success(mapToChapter(chapterDoc, 0, progressDoc))
            } else {
                Result.failure(Exception("Chapter not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logique de mapping unifiée pour fusionner Infos Fixes + Progression
     */
    private fun mapToChapter(
        doc: DocumentSnapshot,
        index: Int,
        progressDoc: DocumentSnapshot?
    ): Chapter {
        val coursUrl = doc.getString("coursUrl") ?: doc.getString("cours")
        val fdrUrl = doc.getString("fdrUrl") ?: doc.getString("fdr")
        val videoUrl = doc.getString("videoUrl") ?: doc.getString("video")
        val quizId = doc.getString("quizId") ?: doc.getString("quiz")

        return Chapter(
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

            // ⭐ LA CORRECTION EST ICI : On lit la vraie progression
            isContentRead = progressDoc?.getBoolean("isContentRead") ?: false,
            isVideoWatched = progressDoc?.getBoolean("isVideoWatched") ?: false,
            isQuizCompleted = progressDoc?.getBoolean("isQuizCompleted") ?: false
        )
    }
}