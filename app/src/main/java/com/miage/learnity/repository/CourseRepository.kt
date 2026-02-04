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
     * Récupère tous les cours
     * @return Result avec liste des cours
     */
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
     * Récupère un cours spécifique
     * @param courseId ID du cours
     * @return Result avec le cours
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
                        cours = coursUrl?.takeIf { it.isNotBlank() },
                        fdr = fdrUrl?.takeIf { it.isNotBlank() },
                        video = videoUrl?.takeIf { it.isNotBlank() },
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
     * Récupère un chapitre spécifique avec son contenu et son statut favori
     * @param courseId ID de l'UE
     * @param chapterId ID du chapitre
     * @param userId ID de l'utilisateur (pour récupérer le statut favori)
     */
    suspend fun getChapter(
        courseId: String,
        chapterId: String,
        userId: String? = null
    ): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            if (courseId.isBlank() || chapterId.isBlank()) {
                return@withContext Result.failure(Exception("IDs de cours ou chapitre manquants"))
            }

            // 1. Récupération des données du catalogue (Source de vérité)
            val snapshot = firestore.collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return@withContext Result.failure(Exception("Chapitre introuvable dans Firestore"))
            }

            // 2. Récupération du statut favori (si userId fourni)
            var isFavorite = false
            if (userId != null) {
                try {
                    val progressDoc = firestore.collection("user_progress")
                        .document(userId)
                        .collection("courses")
                        .document(courseId)
                        .collection("chapters")
                        .document(chapterId)
                        .get()
                        .await()

                    isFavorite = progressDoc.getBoolean("isFavorite") ?: false
                } catch (e: Exception) {
                    println("⚠️ CourseRepository: Impossible de charger le statut favori (${e.message})")
                }
            }

            // 3. Mapping intelligent (Anciens noms vs Nouveaux noms)
            val rawCours = snapshot.getString("cours") ?: snapshot.getString("coursUrl")
            val rawFdr = snapshot.getString("fdr") ?: snapshot.getString("fdrUrl")
            val rawVideo = snapshot.getString("video") ?: snapshot.getString("videoUrl")
            val rawQuiz = snapshot.getString("quizId") ?: snapshot.getString("quiz")

            val chapter = Chapter(
                chapterId = snapshot.id,
                title = snapshot.getString("title") ?: "Sans titre",
                order = snapshot.getLong("order")?.toInt() ?: 0,

                // Attribution aux variables simplifiées de ta DataClass
                cours = rawCours?.takeIf { it.isNotBlank() },
                fdr = rawFdr?.takeIf { it.isNotBlank() },
                video = rawVideo?.takeIf { it.isNotBlank() },

                // Métadonnées
                pageCount = snapshot.getLong("pageCount")?.toInt() ?: 0,
                estimatedReadTime = snapshot.getLong("estimatedReadTime")?.toInt() ?: 0,
                videoDuration = snapshot.getLong("videoDuration")?.toInt() ?: 0,
                quizId = rawQuiz?.takeIf { it.isNotBlank() },

                // États
                isFavorite = isFavorite,
                isCoursRead = false, // Sera mis à jour par le listener du ViewModel
                isFdrRead = false,
                isVideoWatched = false,
                isQuizCompleted = false,
                isQuizUnlocked = false
            )

            // Logs de diagnostics
            println("✅ [CourseRepository] Chapitre chargé : ${chapter.title}")
            println("   > fdr: ${chapter.fdr != null} | video: ${chapter.video != null} | fav: $isFavorite")

            Result.success(chapter)

        } catch (e: Exception) {
            println("❌ [CourseRepository] Erreur critique : ${e.message}")
            Result.failure(e)
        }
    }
}