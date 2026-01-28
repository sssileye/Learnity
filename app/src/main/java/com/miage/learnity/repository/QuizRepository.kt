package com.miage.learnity.repository

import android.icu.text.SimpleDateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.QuizHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    // Format de date unique pour toute la classe
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ============================================
    // RÉCUPÉRATION DES QUIZ
    // ============================================

    suspend fun getQuizForChapter(courseId: String, chapterId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val chapterDoc = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get()
                    .await()

                if (!chapterDoc.exists()) return@withContext Result.failure(Exception("Chapitre non trouvé"))

                val quizJson = chapterDoc.getString("quiz")
                if (quizJson.isNullOrEmpty()) return@withContext Result.failure(Exception("Pas de quiz disponible"))

                val allQuestions = parseQuestions(quizJson)
                val selectedQuestions = allQuestions.shuffled().take(5)

                Result.success(
                    Quiz(
                        quizId = "${chapterId}_quiz",
                        courseId = courseId,
                        chapterId = chapterId,
                        title = chapterDoc.getString("title") ?: "Quiz de chapitre",
                        questions = selectedQuestions
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMegaQuizForCourse(courseId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val chaptersSnapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .get()
                    .await()

                val allUEQuestions = mutableListOf<Question>()
                for (doc in chaptersSnapshot.documents) {
                    doc.getString("quiz")?.let { allUEQuestions.addAll(parseQuestions(it)) }
                }

                if (allUEQuestions.isEmpty()) return@withContext Result.failure(Exception("Aucune question trouvée"))

                Result.success(
                    Quiz(
                        quizId = "MEGA_$courseId",
                        courseId = courseId,
                        chapterId = "ALL_CHAPTERS",
                        title = "Grand Quiz de Synthèse",
                        questions = allUEQuestions.shuffled().take(20)
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ============================================
    // QUIZ DU JOUR (DÉCOUVERTE & RÉVISION)
    // ============================================

    suspend fun getDailyQuiz(
        isDiscoveryMode: Boolean,
        onProgress: (Float) -> Unit
    ): Result<Quiz> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())
            val mode = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
            val dailyDocId = "${userId}_${today}_${mode}"

            val existingDaily = firestore.collection("daily_quizzes_generated").document(dailyDocId).get().await()

            if (existingDaily.exists()) {
                onProgress(1.0f)
                val questionsJson = existingDaily.getString("questionsJson")
                if (!questionsJson.isNullOrEmpty()) {
                    return@withContext Result.success(
                        Quiz("DAILY_QUIZ", "GLOBAL", mode, "Quiz du Jour", parseQuestions(questionsJson))
                    )
                }
            }

            // Génération d'un nouveau pool de questions
            val allQuestionsPool = mutableListOf<Question>()
            val globalCoursesSnapshot = firestore.collection("courses").get().await()
            val totalCourses = globalCoursesSnapshot.size()

            globalCoursesSnapshot.documents.forEachIndexed { index, courseDoc ->
                onProgress((index.toFloat() / totalCourses.toFloat()))
                val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()

                for (chapterDoc in chaptersSnapshot.documents) {
                    var shouldInclude = isDiscoveryMode
                    if (!isDiscoveryMode) {
                        val progressDoc = firestore.collection("user_progress")
                            .document(userId).collection("courses").document(courseDoc.id)
                            .collection("chapters").document(chapterDoc.id).get().await()

                        if (progressDoc.exists() && progressDoc.getBoolean("isContentRead") == true) {
                            shouldInclude = true
                        }
                    }
                    if (shouldInclude) {
                        chapterDoc.getString("quiz")?.let { allQuestionsPool.addAll(parseQuestions(it)) }
                    }
                }
            }

            if (allQuestionsPool.isEmpty()) return@withContext Result.failure(Exception("Pas assez de contenu pour générer le quiz"))

            val selectedQuestions = allQuestionsPool.shuffled().take(10)

            // Sauvegarde pour ne pas regénérer le quiz si on change d'onglet
            firestore.collection("daily_quizzes_generated").document(dailyDocId).set(mapOf(
                "questionsJson" to gson.toJson(selectedQuestions),
                "date" to today,
                "userId" to userId
            )).await()

            onProgress(1.0f)
            Result.success(Quiz("DAILY_QUIZ", "GLOBAL", mode, "Quiz du Jour", selectedQuestions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // ⭐ GESTION DE L'HISTORIQUE
    // ============================================

    suspend fun saveQuizHistory(
        courseId: String,
        chapterId: String,
        historyEntry: QuizHistory,
        userAnswers: Map<Int, Int>? = null // ✅ Ajouté pour pouvoir relire ses erreurs
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))

            val finalData = mapOf(
                "courseId" to courseId,
                "chapterId" to chapterId,
                "date" to historyEntry.date,
                "hour" to historyEntry.hour,
                "score" to historyEntry.score,
                "total" to historyEntry.total,
                "pointsGained" to historyEntry.pointsGained,
                "timestamp" to historyEntry.timestamp,
                "userAnswersJson" to gson.toJson(userAnswers ?: emptyMap<Int, Int>()) // ✅ Sauvegarde cruciale
            )

            firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .add(finalData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizHistory(courseId: String, chapterId: String): Result<List<QuizHistory>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))

                val snapshot = firestore.collection("quiz_results")
                    .document(userId)
                    .collection("history")
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("chapterId", chapterId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val historyList = snapshot.toObjects(QuizHistory::class.java)
                Result.success(historyList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ============================================
    // RÉCUPÉRATION POUR HOMESCREEN
    // ============================================

    suspend fun getLastDailyQuizScore(): Result<Pair<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())

            // On trie par timestamp ASC pour avoir le PREMIER essai (celui qui compte pour les points)
            val snapshot = firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            val firstDoc = snapshot.documents[0]
            val score = firstDoc.getLong("score")?.toInt() ?: 0
            val total = firstDoc.getLong("total")?.toInt() ?: 10

            Result.success(score to total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyQuizAnswers(): Result<Map<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())

            val snapshot = firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            val doc = snapshot.documents[0]
            val answersJson = doc.getString("userAnswersJson") // Nom harmonisé avec saveQuizHistory
            val type = object : TypeToken<Map<Int, Int>>() {}.type
            Result.success(gson.fromJson(answersJson, type))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // UTILS
    // ============================================

    private fun parseQuestions(json: String): List<Question> {
        return try {
            val listType = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) { emptyList() }
    }
}