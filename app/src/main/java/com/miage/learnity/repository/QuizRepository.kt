package com.miage.learnity.repository

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
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

    /**
     * Récupère le quiz d'un chapitre (5 questions aléatoires parmi le JSON du chapitre)
     */
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
                if (quizJson.isNullOrEmpty()) return@withContext Result.failure(Exception("Pas de quiz pour ce chapitre"))

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

    /**
     * NOUVEAU : Récupère 20 questions aléatoires parmi TOUS les chapitres d'une UE
     */
    suspend fun getMegaQuizForCourse(courseId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                // 1. Récupérer tous les documents de la sous-collection "chapters"
                val chaptersSnapshot = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .get()
                    .await()

                val allUEQuestions = mutableListOf<Question>()

                // 2. Extraire et fusionner les questions de chaque chapitre
                for (doc in chaptersSnapshot.documents) {
                    val quizJson = doc.getString("quiz")
                    if (!quizJson.isNullOrEmpty()) {
                        allUEQuestions.addAll(parseQuestions(quizJson))
                    }
                }

                if (allUEQuestions.isEmpty()) return@withContext Result.failure(Exception("Aucune question trouvée dans l'UE"))

                // 3. Shuffle et sélection de 20
                val selectedQuestions = allUEQuestions.shuffled().take(20)

                Result.success(
                    Quiz(
                        quizId = "MEGA_$courseId",
                        courseId = courseId,
                        chapterId = "ALL_CHAPTERS", // ID spécial pour le mode synthèse
                        title = "Grand Quiz de Synthèse",
                        questions = selectedQuestions
                    )
                )
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

            // 1. Enregistrement dans l'historique global
            firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .add(resultData)
                .await()

            // 2. Mise à jour de la progression (UNIQUEMENT si ce n'est pas un Mega Quiz)
            if (chapterId != "ALL_CHAPTERS") {
                firestore.collection("user_progress")
                    .document(userId)
                    .collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                    .await()

                // Notification pour la barre de progression UI
                ProgressManager.notifyProgressChanged(
                    courseId,
                    chapterId,
                    ProgressManager.ProgressType.QUIZ_COMPLETED
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // HELPER
    // ============================================

    private fun parseQuestions(json: String): List<Question> {
        return try {
            val listType = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}