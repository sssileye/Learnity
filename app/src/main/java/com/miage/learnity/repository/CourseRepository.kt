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
    suspend fun getAllCourses(): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("courses")
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { doc ->
                Course(
                    id = doc.getString("id") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    iconRes = doc.getLong("iconRes")?.toInt() ?: 0
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
    suspend fun getCourse(courseId: String): Result<Course> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val course = Course(
                        id = snapshot.getString("id") ?: "",
                        title = snapshot.getString("title") ?: "",
                        description = snapshot.getString("description") ?: "",
                        iconRes = snapshot.getLong("iconRes")?.toInt() ?: 0
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
     * Récupère les chapitres d'un cours (sous-collection)
     * @param courseId ID du cours
     * @return Result avec liste des chapitres
     */
    suspend fun getChapters(courseId: String): Result<List<Chapter>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .orderBy("order")
                    .get()
                    .await()

                val chapters = snapshot.documents.mapNotNull { doc ->
                    Chapter(
                        chapterId = doc.getString("chapterId") ?: "",
                        title = doc.getString("title") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0,
                        coursUrl = doc.getString("coursUrl"),
                        fdrUrl = doc.getString("fdrUrl"),
                        videoUrl = doc.getString("videoUrl"),
                        pageCount = doc.getLong("pageCount")?.toInt() ?: 0,
                        estimatedReadTime = doc.getLong("estimatedReadTime")?.toInt() ?: 0,
                        videoDuration = doc.getLong("videoDuration")?.toInt() ?: 0,
                        quizId = doc.getString("quizId"),
                        // États par défaut (seront mis à jour par UserProgressRepository)
                        isVideoWatched = false,
                        isContentRead = false,
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
     * @param courseId ID du cours
     * @param chapterId ID du chapitre
     * @return Result avec le chapitre
     */
    suspend fun getChapter(courseId: String, chapterId: String): Result<Chapter> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val chapter = Chapter(
                        chapterId = snapshot.getString("chapterId") ?: "",
                        title = snapshot.getString("title") ?: "",
                        order = snapshot.getLong("order")?.toInt() ?: 0,
                        coursUrl = snapshot.getString("coursUrl"),
                        fdrUrl = snapshot.getString("fdrUrl"),
                        videoUrl = snapshot.getString("videoUrl"),
                        pageCount = snapshot.getLong("pageCount")?.toInt() ?: 0,
                        estimatedReadTime = snapshot.getLong("estimatedReadTime")?.toInt() ?: 0,
                        videoDuration = snapshot.getLong("videoDuration")?.toInt() ?: 0,
                        quizId = snapshot.getString("quizId"),
                        isVideoWatched = false,
                        isContentRead = false,
                        isQuizCompleted = false
                    )
                    Result.success(chapter)
                } else {
                    Result.failure(Exception("Chapter not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

