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
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    iconRes = doc.getLong("iconRes")?.toInt()
                )
            }

            println("✅ CourseRepository - ${courses.size} cours chargés depuis Firebase")
            courses.forEach {
                println("   📚 ${it.id} - ${it.title}")
            }

            Result.success(courses)
        } catch (e: Exception) {
            println("❌ CourseRepository - Erreur : ${e.message}")
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
                if (courseId.isBlank()) {
                    return@withContext Result.failure(Exception("Course ID vide"))
                }

                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val course = Course(
                        id = snapshot.id,
                        title = snapshot.getString("title") ?: "",
                        description = snapshot.getString("description") ?: "",
                        iconRes = snapshot.getLong("iconRes")?.toInt()
                    )
                    println("✅ CourseRepository - Cours chargé : ${course.title}")
                    Result.success(course)
                } else {
                    println("❌ CourseRepository - Cours non trouvé : $courseId")
                    Result.failure(Exception("Course not found"))
                }
            } catch (e: Exception) {
                println("❌ CourseRepository - Erreur : ${e.message}")
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
                if (courseId.isBlank()) {
                    return@withContext Result.failure(Exception("Course ID vide"))
                }

                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .orderBy("order")
                    .get()
                    .await()

                val chapters = snapshot.documents.mapIndexed { index, doc ->
                    // Adaptation : Lire anciens ET nouveaux noms de champs
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
                        // ✅ NOUVEAUX CHAMPS - Initialisés à false (progression chargée ailleurs)
                        isCoursRead = false,
                        isFdrRead = false,
                        isVideoWatched = false,
                        isQuizCompleted = false
                    )
                }

                println("✅ CourseRepository - ${chapters.size} chapitres chargés pour $courseId")
                chapters.forEach { chapter ->
                    println("   📖 ${chapter.title} - cours=${chapter.hasCours}, fdr=${chapter.hasFdr}, video=${chapter.hasVideo}")
                }

                Result.success(chapters)
            } catch (e: Exception) {
                println("❌ CourseRepository - Erreur chapitres : ${e.message}")
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
                if (courseId.isBlank() || chapterId.isBlank()) {
                    return@withContext Result.failure(Exception("IDs vides"))
                }

                val snapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    // Adaptation : Lire anciens ET nouveaux noms de champs
                    val coursUrl = snapshot.getString("coursUrl") ?: snapshot.getString("cours")
                    val fdrUrl = snapshot.getString("fdrUrl") ?: snapshot.getString("fdr")
                    val videoUrl = snapshot.getString("videoUrl") ?: snapshot.getString("video")
                    val quizId = snapshot.getString("quizId") ?: snapshot.getString("quiz")

                    val chapter = Chapter(
                        chapterId = snapshot.id,
                        title = snapshot.getString("title") ?: "",
                        order = snapshot.getLong("order")?.toInt() ?: 0,
                        coursUrl = coursUrl?.takeIf { it.isNotBlank() },
                        fdrUrl = fdrUrl?.takeIf { it.isNotBlank() },
                        videoUrl = videoUrl?.takeIf { it.isNotBlank() },
                        pageCount = snapshot.getLong("pageCount")?.toInt() ?: 0,
                        estimatedReadTime = snapshot.getLong("estimatedReadTime")?.toInt() ?: 0,
                        videoDuration = snapshot.getLong("videoDuration")?.toInt() ?: 0,
                        quizId = quizId?.takeIf { it.isNotBlank() },
                        // ✅ NOUVEAUX CHAMPS - Initialisés à false (progression chargée ailleurs)
                        isCoursRead = false,
                        isFdrRead = false,
                        isVideoWatched = false,
                        isQuizCompleted = false
                    )

                    println("✅ CourseRepository - Chapitre chargé : ${chapter.title}")
                    println("   📄 coursUrl=${chapter.coursUrl}")
                    println("   📋 fdrUrl=${chapter.fdrUrl}")
                    println("   🎥 videoUrl=${chapter.videoUrl}")
                    println("   ❓ quizId=${chapter.quizId}")

                    Result.success(chapter)
                } else {
                    println("❌ CourseRepository - Chapitre non trouvé")
                    Result.failure(Exception("Chapter not found"))
                }
            } catch (e: Exception) {
                println("❌ CourseRepository - Erreur : ${e.message}")
                Result.failure(e)
            }
        }
}