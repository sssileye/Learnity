package com.miage.learnity.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.data.Course
import com.miage.learnity.data.Chapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    // ⭐ Correction format : On utilise le même format que celui envoyé par le ViewModel
    private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    // Format pour l'ID du cache (doit rester sans slash)
    private val cacheDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getCourseDetails(courseId: String): Result<Course> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("courses").document(courseId).get().await()
            if (!doc.exists()) return@withContext Result.failure(Exception("Cours non trouvé"))

            Result.success(Course(
                id = doc.id,
                title = doc.getString("title") ?: "UE Inconnue",
                description = doc.getString("description") ?: ""
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getChapterDetails(courseId: String, chapterId: String): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("courses").document(courseId)
                .collection("chapters").document(chapterId).get().await()
            if (!doc.exists()) return@withContext Result.failure(Exception("Chapitre non trouvé"))

            Result.success(Chapter(
                chapterId = doc.id,
                title = doc.getString("title") ?: "Chapitre Sans Titre"
            ))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getQuizForChapter(courseId: String, chapterId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val chapterDoc = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get().await()

                if (!chapterDoc.exists()) return@withContext Result.failure(Exception("Chapitre non trouvé"))

                val quizJson = chapterDoc.getString("quiz")
                if (quizJson.isNullOrEmpty()) return@withContext Result.failure(Exception("Pas de quiz disponible"))

                val chapterTitle = chapterDoc.getString("title") ?: "Quiz de chapitre"

                val selectedQuestions = parseQuestions(quizJson)
                    .map { it.copy(chapterTitle = chapterTitle) }
                    .shuffled()
                    .take(5)

                Result.success(Quiz(
                    quizId = "${chapterId}_quiz",
                    courseId = courseId,
                    chapterId = chapterId,
                    title = chapterTitle,
                    questions = selectedQuestions
                ))
            } catch (e: Exception) { Result.failure(e) }
        }

    suspend fun getMegaQuizForCourse(courseId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val chaptersSnapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters").get().await()

                val allUEQuestions = mutableListOf<Question>()

                for (doc in chaptersSnapshot.documents) {
                    val chapterTitle = doc.getString("title") ?: "Chapitre inconnu"
                    doc.getString("quiz")?.let { json ->
                        val questions = parseQuestions(json).map { it.copy(chapterTitle = chapterTitle) }
                        allUEQuestions.addAll(questions)
                    }
                }

                if (allUEQuestions.isEmpty()) return@withContext Result.failure(Exception("Aucune question trouvée"))

                Result.success(Quiz(
                    quizId = "MEGA_$courseId",
                    courseId = courseId,
                    chapterId = "ALL_CHAPTERS",
                    title = "Grand Quiz de Synthèse",
                    questions = allUEQuestions.shuffled().take(20)
                ))
            } catch (e: Exception) { Result.failure(e) }
        }

    suspend fun getDailyQuiz(
        isDiscoveryMode: Boolean,
        onProgress: (Float) -> Unit
    ): Result<Quiz> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val todayCache = cacheDateFormat.format(Date())
            val mode = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
            val dailyDocId = "${userId}_${todayCache}_${mode}"

            val existingDaily = firestore.collection("daily_quizzes_generated").document(dailyDocId).get().await()

            if (existingDaily.exists()) {
                val questionsJson = existingDaily.getString("questionsJson")
                if (!questionsJson.isNullOrEmpty()) {
                    val cachedQuestions = parseQuestions(questionsJson)
                    val isCacheValid = cachedQuestions.firstOrNull()?.courseTitle != null

                    if (isCacheValid) {
                        onProgress(1.0f)
                        return@withContext Result.success(
                            Quiz("DAILY_QUIZ", "GLOBAL", mode, "Quiz du Jour", cachedQuestions)
                        )
                    }
                }
            }

            val allQuestionsPool = mutableListOf<Question>()
            val globalCoursesSnapshot = firestore.collection("courses").get().await()
            val totalCourses = globalCoursesSnapshot.size()

            globalCoursesSnapshot.documents.forEachIndexed { index, courseDoc ->
                onProgress((index.toFloat() / totalCourses.toFloat()))
                val courseTitle = courseDoc.getString("title") ?: "UE Inconnue"
                val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()

                for (chapterDoc in chaptersSnapshot.documents) {
                    var shouldInclude = isDiscoveryMode

                    if (!isDiscoveryMode) {
                        val progressDoc = firestore.collection("user_progress")
                            .document(userId).collection("courses").document(courseDoc.id)
                            .collection("chapters").document(chapterDoc.id).get().await()

                        if (progressDoc.exists()) {
                            val isCoursRead = progressDoc.getBoolean("isCoursRead") ?: false
                            val isFdrRead = progressDoc.getBoolean("isFdrRead") ?: false
                            if (isCoursRead || isFdrRead) shouldInclude = true
                        }
                    }

                    if (shouldInclude) {
                        val chapterTitle = chapterDoc.getString("title") ?: "Chapitre"
                        chapterDoc.getString("quiz")?.let { json ->
                            val questions = parseQuestions(json).map {
                                it.copy(courseTitle = courseTitle, chapterTitle = chapterTitle)
                            }
                            allQuestionsPool.addAll(questions)
                        }
                    }
                }
            }

            if (allQuestionsPool.isEmpty()) {
                val msg = if (isDiscoveryMode) "Contenu insuffisant"
                else "Il faut lire au moins un cours pour débloquer les Révisions ! 😉"
                return@withContext Result.failure(Exception(msg))
            }

            val selectedQuestions = allQuestionsPool.shuffled().take(10)
            firestore.collection("daily_quizzes_generated").document(dailyDocId).set(mapOf(
                "questionsJson" to gson.toJson(selectedQuestions),
                "date" to todayCache,
                "userId" to userId
            )).await()

            onProgress(1.0f)
            Result.success(Quiz("DAILY_QUIZ", "GLOBAL", mode, "Quiz du Jour", selectedQuestions))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun saveQuizHistory(
        courseId: String,
        chapterId: String,
        historyEntry: QuizHistory,
        userAnswers: Map<Int, Int>? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))

            // ⭐ SÉCURITÉ : Pour le Quiz du Jour, on vérifie si un score existe déjà
            if (chapterId == "DISCOVERY" || chapterId == "REVIEW") {
                val existing = firestore.collection("quiz_results")
                    .document(userId)
                    .collection("history")
                    .whereEqualTo("date", historyEntry.date)
                    .whereEqualTo("chapterId", chapterId)
                    .get()
                    .await()

                // Si la liste n'est pas vide, on n'enregistre rien (on arrête la fonction ici)
                if (!existing.isEmpty) {
                    Log.d("QuizRepository", "🚫 Tentative de refaire le quiz : Score non sauvegardé (déjà existant)")
                    return@withContext Result.success(Unit)
                }
            }

            // Sinon, on procède à l'enregistrement classique (Premier essai)
            val finalData = mapOf(
                "courseId" to courseId,
                "chapterId" to chapterId,
                "date" to historyEntry.date,
                "hour" to historyEntry.hour,
                "score" to historyEntry.score,
                "total" to historyEntry.total,
                "pointsGained" to historyEntry.pointsGained,
                "timestamp" to historyEntry.timestamp,
                "userAnswersJson" to gson.toJson(userAnswers ?: emptyMap<Int, Int>())
            )

            firestore.collection("quiz_results").document(userId).collection("history").add(finalData).await()

            // Mise à jour de la progression si ce n'est pas un mode spécial
            val specialModes = listOf("ALL_CHAPTERS", "DISCOVERY", "REVIEW")
            if (!specialModes.contains(chapterId)) {
                firestore.collection("user_progress")
                    .document(userId).collection("courses").document(courseId)
                    .collection("chapters").document(chapterId)
                    .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Erreur sauvegarde : ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getQuizHistory(courseId: String, chapterId: String): Result<List<QuizHistory>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
                val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                    .whereEqualTo("courseId", courseId).whereEqualTo("chapterId", chapterId)
                    .orderBy("timestamp", Query.Direction.DESCENDING).get().await()

                Result.success(snapshot.toObjects(QuizHistory::class.java))
            } catch (e: Exception) { Result.failure(e) }
        }

    suspend fun getLastDailyQuizScore(): Result<Pair<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.success(null)
            val today = dateFormat.format(Date()) // Format dd/MM/yy

            // On cherche dans l'historique de l'utilisateur
            val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .get().await()

            if (snapshot.isEmpty) {
                Log.d("QuizRepository", "🔍 Aucun score trouvé pour la date: $today")
                return@withContext Result.success(null)
            }

            // On prend le plus récent basé sur le timestamp
            val doc = snapshot.documents.maxByOrNull { it.getLong("timestamp") ?: 0L }

            val score = doc?.getLong("score")?.toInt() ?: 0
            val total = doc?.getLong("total")?.toInt() ?: 10

            Log.d("QuizRepository", "✅ Score chargé : $score/$total")
            Result.success(score to total)
        } catch (e: Exception) {
            Log.e("QuizRepository", "❌ Erreur getLastDailyQuizScore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getWeeklyProgress(goalPerWeek: Int = 4): Result<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val calendar = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            val mondayDate = dateFormat.format(calendar.time)

            // Récupère tout l'historique de la semaine
            val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                .whereGreaterThanOrEqualTo("date", mondayDate).get().await()

            val dailyQuizCount = snapshot.documents.count { doc ->
                val cid = doc.getString("chapterId")
                cid == "DISCOVERY" || cid == "REVIEW"
            }
            Result.success(dailyQuizCount to goalPerWeek)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun parseQuestions(json: String): List<Question> {
        return try {
            val listType = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) { emptyList() }
    }
}