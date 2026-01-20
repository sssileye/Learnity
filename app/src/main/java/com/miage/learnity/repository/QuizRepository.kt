package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.Question
import com.miage.learnity.data.QuestionDifficulty
import com.miage.learnity.data.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository pour gérer les Quiz avec Firebase
 * Gère : Quiz par chapitre, Quiz du jour, Planning de révision
 */
class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ============================================
    // QUIZ PAR CHAPITRE
    // ============================================

    /**
     * Récupère le quiz d'un chapitre spécifique
     * @param courseId ID du cours
     * @param chapterId ID du chapitre
     * @return Quiz avec questions mélangées aléatoirement
     */
    suspend fun getQuizForChapter(courseId: String, chapterId: String): Result<Quiz> =
        withContext(Dispatchers.IO) {
            try {
                println("🔍 QuizRepository - Recherche quiz pour: $courseId/$chapterId")

                // Chercher le quiz par courseId ET chapterId
                val snapshot = firestore.collection("quizzes")
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("chapterId", chapterId)
                    .get()
                    .await()

                if (snapshot.documents.isEmpty()) {
                    println("❌ QuizRepository - Aucun quiz trouvé")
                    return@withContext Result.failure(Exception("Quiz non trouvé pour ce chapitre"))
                }

                val quizDoc = snapshot.documents.first()
                println("✅ QuizRepository - Quiz trouvé: ${quizDoc.id}")

                // Charger les questions (sous-collection)
                val questionsSnapshot = firestore.collection("quizzes")
                    .document(quizDoc.id)
                    .collection("questions")
                    .get()
                    .await()

                val questions = questionsSnapshot.documents.mapNotNull { doc ->
                    try {
                        val options = (doc.get("options") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                        Question(
                            questionId = doc.id,
                            questionText = doc.getString("questionText") ?: "",
                            options = options,
                            correctAnswerIndex = doc.getLong("correctAnswerIndex")?.toInt() ?: 0,
                            explanation = doc.getString("explanation"),
                            difficulty = try {
                                QuestionDifficulty.valueOf(doc.getString("difficulty") ?: "MEDIUM")
                            } catch (e: Exception) {
                                QuestionDifficulty.MEDIUM
                            }
                        )
                    } catch (e: Exception) {
                        println("⚠️ Erreur parsing question ${doc.id}: ${e.message}")
                        null
                    }
                }

                println("✅ QuizRepository - ${questions.size} questions chargées")

                // Mélanger les questions ET les options de chaque question
                val shuffledQuestions = questions.shuffled().map { question ->
                    shuffleQuestionOptions(question)
                }

                val quiz = Quiz(
                    quizId = quizDoc.id,
                    courseId = courseId,
                    chapterId = chapterId,
                    title = quizDoc.getString("title") ?: "Quiz",
                    questions = shuffledQuestions,
                    timeLimit = quizDoc.getLong("timeLimit")?.toInt() ?: 180
                )

                Result.success(quiz)
            } catch (e: Exception) {
                println("❌ QuizRepository - Erreur: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Mélange les options d'une question et ajuste correctAnswerIndex
     */
    private fun shuffleQuestionOptions(question: Question): Question {
        val correctAnswer = question.options.getOrNull(question.correctAnswerIndex) ?: return question
        val shuffledOptions = question.options.shuffled()
        val newCorrectIndex = shuffledOptions.indexOf(correctAnswer)

        return question.copy(
            options = shuffledOptions,
            correctAnswerIndex = newCorrectIndex
        )
    }

    // ============================================
    // QUIZ DU JOUR
    // ============================================

    /**
     * Récupère ou génère le quiz du jour pour l'utilisateur
     * Basé sur son planning de révision hebdomadaire
     */
    suspend fun getDailyQuiz(): Result<Quiz> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            // 1. Vérifier si un quiz du jour existe déjà
            val existingQuiz = firestore.collection("daily_quiz_history")
                .document("${userId}_$today")
                .get()
                .await()

            if (existingQuiz.exists() && existingQuiz.getBoolean("completed") == true) {
                // Quiz déjà fait aujourd'hui
                return@withContext Result.failure(Exception("Quiz du jour déjà complété !"))
            }

            // 2. Récupérer le planning de la semaine
            val planningDoc = firestore.collection("user_quiz_planning")
                .document(userId)
                .get()
                .await()

            val selectedChapters = (planningDoc.get("selectedChapters") as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?: emptyList()

            if (selectedChapters.isEmpty()) {
                return@withContext Result.failure(Exception("Aucun chapitre sélectionné pour cette semaine"))
            }

            // 3. Choisir un chapitre aléatoire parmi le planning
            val randomChapterId = selectedChapters.random()

            // 4. Trouver le quiz correspondant
            val quizSnapshot = firestore.collection("quizzes")
                .whereEqualTo("chapterId", randomChapterId)
                .get()
                .await()

            if (quizSnapshot.documents.isEmpty()) {
                return@withContext Result.failure(Exception("Aucun quiz disponible pour ce chapitre"))
            }

            val quizDoc = quizSnapshot.documents.first()
            val courseId = quizDoc.getString("courseId") ?: ""

            // 5. Charger le quiz complet
            getQuizForChapter(courseId, randomChapterId)

        } catch (e: Exception) {
            println("❌ QuizRepository - Erreur daily quiz: ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // PLANNING DE RÉVISION
    // ============================================

    /**
     * Sauvegarde le planning de révision de la semaine
     */
    suspend fun saveWeeklyPlanning(
        selectedCourses: List<String>,
        selectedChapters: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val weekStart = getWeekStartDate()

            val planning = mapOf(
                "weekStart" to weekStart,
                "selectedCourses" to selectedCourses,
                "selectedChapters" to selectedChapters,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("user_quiz_planning")
                .document(userId)
                .set(planning, SetOptions.merge())
                .await()

            println("✅ Planning sauvegardé: ${selectedChapters.size} chapitres")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère le planning de révision actuel
     */
    suspend fun getWeeklyPlanning(): Result<WeeklyPlanning> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val doc = firestore.collection("user_quiz_planning")
                .document(userId)
                .get()
                .await()

            if (!doc.exists()) {
                return@withContext Result.success(WeeklyPlanning())
            }

            val planning = WeeklyPlanning(
                weekStart = doc.getString("weekStart") ?: "",
                selectedCourses = (doc.get("selectedCourses") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                selectedChapters = (doc.get("selectedChapters") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            )

            Result.success(planning)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // HISTORIQUE & SCORES
    // ============================================

    /**
     * Sauvegarde le résultat d'un quiz
     */
    suspend fun saveQuizResult(
        quizId: String,
        courseId: String,
        chapterId: String,
        score: Int,
        total: Int,
        isDailyQuiz: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val timestamp = System.currentTimeMillis()

            // 1. Sauvegarder dans l'historique général
            val result = mapOf(
                "quizId" to quizId,
                "courseId" to courseId,
                "chapterId" to chapterId,
                "score" to score,
                "total" to total,
                "percentage" to (score.toFloat() / total * 100).toInt(),
                "completedAt" to timestamp,
                "date" to today,
                "isDailyQuiz" to isDailyQuiz
            )

            firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .add(result)
                .await()

            // 2. Si c'est le quiz du jour, marquer comme complété
            if (isDailyQuiz) {
                firestore.collection("daily_quiz_history")
                    .document("${userId}_$today")
                    .set(
                        mapOf(
                            "date" to today,
                            "quizId" to quizId,
                            "score" to score,
                            "total" to total,
                            "completed" to true,
                            "completedAt" to timestamp
                        )
                    )
                    .await()
            }

            // 3. Marquer le chapitre comme "quiz complété" dans la progression
            firestore.collection("user_progress")
                .document(userId)
                .collection("courses")
                .document(courseId)
                .collection("chapters")
                .document(chapterId)
                .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                .await()

            println("✅ Résultat quiz sauvegardé: $score/$total")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Vérifie si le quiz du jour a été fait
     */
    suspend fun isDailyQuizCompleted(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            val doc = firestore.collection("daily_quiz_history")
                .document("${userId}_$today")
                .get()
                .await()

            Result.success(doc.exists() && doc.getBoolean("completed") == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère les statistiques de quiz de l'utilisateur
     */
    suspend fun getQuizStats(): Result<QuizStats> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val snapshot = firestore.collection("quiz_results")
                .document(userId)
                .collection("history")
                .get()
                .await()

            var totalQuizzes = 0
            var totalScore = 0
            var totalQuestions = 0
            var dailyQuizzesCompleted = 0

            snapshot.documents.forEach { doc ->
                totalQuizzes++
                totalScore += doc.getLong("score")?.toInt() ?: 0
                totalQuestions += doc.getLong("total")?.toInt() ?: 0
                if (doc.getBoolean("isDailyQuiz") == true) {
                    dailyQuizzesCompleted++
                }
            }

            val averagePercentage = if (totalQuestions > 0) {
                (totalScore.toFloat() / totalQuestions * 100).toInt()
            } else 0

            Result.success(
                QuizStats(
                    totalQuizzes = totalQuizzes,
                    totalCorrectAnswers = totalScore,
                    totalQuestions = totalQuestions,
                    averagePercentage = averagePercentage,
                    dailyQuizzesCompleted = dailyQuizzesCompleted
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // UTILITAIRES
    // ============================================

    private fun getWeekStartDate(): String {
        val today = LocalDate.now()
        val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        return monday.format(DateTimeFormatter.ISO_DATE)
    }
}

// ============================================
// DATA CLASSES
// ============================================

data class WeeklyPlanning(
    val weekStart: String = "",
    val selectedCourses: List<String> = emptyList(),
    val selectedChapters: List<String> = emptyList()
)

data class QuizStats(
    val totalQuizzes: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val averagePercentage: Int = 0,
    val dailyQuizzesCompleted: Int = 0
)
