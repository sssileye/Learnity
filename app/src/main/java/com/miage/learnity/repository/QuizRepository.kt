package com.miage.learnity.repository

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    // ============================================
    // QUIZ PAR CHAPITRE (LOGIQUE JSON)
    // ============================================

    /**
     * Récupère le quiz stocké en JSON dans le document du chapitre
     */
    suspend fun getQuizForChapter(courseId: String, chapterId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                println("🔍 QuizRepository - Lecture du JSON pour: $courseId/$chapterId")

                // 1. Accéder au document du chapitre dans la sous-collection
                val chapterDoc = firestore.collection("courses")
                    .document(courseId)
                    .collection("chapters")
                    .document(chapterId)
                    .get()
                    .await()

                if (!chapterDoc.exists()) {
                    return@withContext Result.failure(Exception("Chapitre non trouvé"))
                }

                // 2. Récupérer la String JSON du champ "quiz"
                val quizJson = chapterDoc.getString("quiz")
                if (quizJson.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Aucun quiz JSON configuré pour ce chapitre"))
                }

                // 3. Parser le JSON vers une liste de Questions
                val listType = object : TypeToken<List<Question>>() {}.type
                val allQuestions: List<Question> = gson.fromJson(quizJson, listType)

                if (allQuestions.isEmpty()) {
                    return@withContext Result.failure(Exception("Le quiz est vide"))
                }

                // 4. Mélanger et prendre 5 questions
                val selectedQuestions = allQuestions.shuffled().take(5)

                val quiz = Quiz(
                    quizId = "${chapterId}_quiz",
                    courseId = courseId,
                    chapterId = chapterId,
                    title = chapterDoc.getString("title") ?: "Quiz de chapitre",
                    questions = selectedQuestions
                )

                Result.success(quiz)
            } catch (e: Exception) {
                println("❌ QuizRepository Erreur JSON: ${e.message}")
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
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            val result = mapOf(
                "courseId" to courseId,
                "chapterId" to chapterId,
                "score" to score,
                "total" to total,
                "percentage" to (score.toFloat() / total * 100).toInt(),
                "completedAt" to System.currentTimeMillis(),
                "date" to today
            )

            // Sauvegarder dans l'historique
            firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .add(result)
                .await()

            // Marquer comme complété dans la progression utilisateur
            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Les autres méthodes (getQuizStats, etc.) peuvent être conservées si besoin
}