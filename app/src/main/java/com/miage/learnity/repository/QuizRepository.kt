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

    /**
     * Module 1 & 2 : Récupère 10 questions transversales
     * @param isDiscoveryMode Si true : toutes les UE. Si false : uniquement chapitres LUS (isContentRead).
     */
    suspend fun getDailyQuiz(isDiscoveryMode: Boolean): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
                val allQuestionsPool = mutableListOf<Question>()

                // 1. On récupère la liste de TOUS les cours existants (Source : collection "courses")
                val globalCoursesSnapshot = firestore.collection("courses").get().await()

                for (courseDoc in globalCoursesSnapshot.documents) {
                    val courseId = courseDoc.id

                    // 2. On récupère tous les chapitres de cette UE
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()

                    for (chapterDoc in chaptersSnapshot.documents) {
                        val chapterId = chapterDoc.id
                        var shouldInclude = isDiscoveryMode // Si découverte, on prend tout

                        if (!isDiscoveryMode) {
                            // 3. MODE RÉVISION : On va chercher DIRECTEMENT le document de progression
                            // On ne liste pas, on tape directement au bon chemin
                            val progressDoc = firestore.collection("user_progress")
                                .document(userId)
                                .collection("courses")
                                .document(courseId)
                                .collection("chapters")
                                .document(chapterId)
                                .get()
                                .await()

                            // Si le document existe et que isContentRead est vrai
                            if (progressDoc.exists() && progressDoc.getBoolean("isContentRead") == true) {
                                shouldInclude = true
                                println("LOG_QUIZ: Chapitre validé pour révision -> $chapterId")
                            }
                        }

                        // 4. Extraction des questions
                        if (shouldInclude) {
                            val quizJson = chapterDoc.getString("quiz")
                            if (!quizJson.isNullOrEmpty()) {
                                allQuestionsPool.addAll(parseQuestions(quizJson))
                            }
                        }
                    }
                }

                if (allQuestionsPool.isEmpty()) {
                    val errorMsg = if (!isDiscoveryMode)
                        "Aucun chapitre lu trouvé. Ouvre un cours pour débloquer le mode Révision !"
                    else "Aucune question trouvée dans la base de données."
                    return@withContext Result.failure(Exception(errorMsg))
                }

                Result.success(
                    Quiz(
                        "DAILY_QUIZ",
                        "GLOBAL",
                        if (isDiscoveryMode) "DISCOVERY" else "REVIEW",
                        "Quiz du Jour",
                        allQuestionsPool.shuffled().take(10)
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("QUIZ_ERROR", "Erreur fatale: ${e.message}")
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

            val lastDoc = snapshot.documents.maxByOrNull { it.getLong("completedAt") ?: 0L }
            val score = lastDoc?.getLong("score")?.toInt() ?: 0
            val total = lastDoc?.getLong("total")?.toInt() ?: 10

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
        total: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val resultData = mapOf(
                "courseId" to courseId,
                "chapterId" to chapterId,
                "score" to score,
                "total" to total,
                "percentage" to (score.toFloat() / total * 100).toInt(),
                "completedAt" to System.currentTimeMillis(),
                "date" to today
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

    private fun parseQuestions(json: String): List<Question> {
        return try {
            val listType = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}