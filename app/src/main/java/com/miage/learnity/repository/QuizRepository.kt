package com.miage.learnity.repository

import android.icu.text.SimpleDateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.Query

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

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
                if (quizJson.isNullOrEmpty()) return@withContext Result.failure(Exception("Pas de quiz"))

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
    // ⭐ QUIZ DU JOUR (DAILY QUIZ)
    // ============================================

    suspend fun getDailyQuiz(isDiscoveryMode: Boolean): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val mode = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"

                // Identifiant unique pour le quiz généré (User + Date + Mode)
                val dailyDocId = "${userId}_${today}_${mode}"

                // 1. Vérifier si un quiz a déjà été généré aujourd'hui
                val existingDaily = firestore.collection("daily_quizzes_generated")
                    .document(dailyDocId)
                    .get()
                    .await()

                if (existingDaily.exists()) {
                    val questionsJson = existingDaily.getString("questionsJson")
                    if (!questionsJson.isNullOrEmpty()) {
                        return@withContext Result.success(
                            Quiz(
                                "DAILY_QUIZ",
                                "GLOBAL",
                                mode,
                                "Quiz du Jour",
                                parseQuestions(questionsJson)
                            )
                        )
                    }
                }

                // 2. Si aucun quiz généré, on crée le pool de questions
                val allQuestionsPool = mutableListOf<Question>()
                val globalCoursesSnapshot = firestore.collection("courses").get().await()

                for (courseDoc in globalCoursesSnapshot.documents) {
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()

                    for (chapterDoc in chaptersSnapshot.documents) {
                        var shouldInclude = isDiscoveryMode

                        if (!isDiscoveryMode) {
                            val progressDoc = firestore.collection("user_progress")
                                .document(userId)
                                .collection("courses")
                                .document(courseDoc.id)
                                .collection("chapters")
                                .document(chapterDoc.id)
                                .get()
                                .await()

                            if (progressDoc.exists() && progressDoc.getBoolean("isContentRead") == true) {
                                shouldInclude = true
                            }
                        }

                        if (shouldInclude) {
                            chapterDoc.getString("quiz")?.let { allQuestionsPool.addAll(parseQuestions(it)) }
                        }
                    }
                }

                if (allQuestionsPool.isEmpty()) {
                    val errorMsg = if (!isDiscoveryMode)
                        "Aucun chapitre lu trouvé."
                    else "Aucune question trouvée."
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val selectedQuestions = allQuestionsPool.shuffled().take(10)

                // 3. Sauvegarder les questions générées pour pouvoir les refaire
                val questionsJson = gson.toJson(selectedQuestions)
                firestore.collection("daily_quizzes_generated")
                    .document(dailyDocId)
                    .set(mapOf(
                        "questionsJson" to questionsJson,
                        "date" to today,
                        "userId" to userId
                    ))
                    .await()

                Result.success(
                    Quiz("DAILY_QUIZ", "GLOBAL", mode, "Quiz du Jour", selectedQuestions)
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getLastDailyQuizScore(): Result<Pair<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val snapshot = firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            // On prend le tout premier score enregistré pour cette date (le score figé)
            val firstDoc = snapshot.documents.minByOrNull { it.getLong("completedAt") ?: Long.MAX_VALUE }
            val score = firstDoc?.getLong("score")?.toInt() ?: 0
            val total = firstDoc?.getLong("total")?.toInt() ?: 10

            Result.success(score to total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // SAUVEGARDE DES RÉSULTATS
    // ============================================

    suspend fun saveQuizResult(
        courseId: String,
        chapterId: String,
        score: Int,
        total: Int,
        userAnswers: Map<Int, Int> // ⭐ AJOUTE CE PARAMÈTRE ICI
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (chapterId == "DISCOVERY" || chapterId == "REVIEW") {
                val existing = firestore.collection("quiz_results")
                    .document(userId)
                    .collection("history")
                    .whereEqualTo("date", today)
                    .whereEqualTo("chapterId", chapterId)
                    .get()
                    .await()

                if (!existing.isEmpty) return@withContext Result.success(Unit)
            }

            // ⭐ On utilise maintenant userAnswers pour créer le JSON
            val resultData = mapOf(
                "courseId" to courseId,
                "chapterId" to chapterId,
                "score" to score,
                "total" to total,
                "percentage" to (score.toFloat() / total * 100).toInt(),
                "completedAt" to System.currentTimeMillis(),
                "date" to today,
                "answersJson" to gson.toJson(userAnswers) // Sauvegarde des réponses
            )

            firestore.collection("quiz_results").document(userId).collection("history").add(resultData).await()

            val specialModes = listOf("ALL_CHAPTERS", "DISCOVERY", "REVIEW")
            if (!specialModes.contains(chapterId)) {
                firestore.collection("user_progress")
                    .document(userId)
                    .collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                    .await()

                ProgressManager.notifyProgressChanged(courseId, chapterId, ProgressManager.ProgressType.QUIZ_COMPLETED)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ⭐ N'OUBLIE PAS la fonction pour récupérer les réponses
    suspend fun getDailyQuizAnswers(): Result<Map<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val snapshot = firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .get()
                .await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            // Récupérer le premier document créé (le score figé)
            val doc = snapshot.documents.minByOrNull { it.getLong("completedAt") ?: Long.MAX_VALUE }
            val answersJson = doc?.getString("answersJson")

            val type = object : TypeToken<Map<Int, Int>>() {}.type
            val answers: Map<Int, Int>? = gson.fromJson(answersJson, type)

            Result.success(answers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseQuestions(json: String): List<Question> {
        return try {
            val listType = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}