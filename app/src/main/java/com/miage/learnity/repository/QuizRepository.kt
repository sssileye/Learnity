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
import java.util.*

class QuizRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    // ✅ Format ISO unique pour assurer le tri alphabétique correct dans Firestore
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ============================================
    // RÉCUPÉRATION DES QUIZ CLASSIQUES
    // ============================================

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

                val selectedQuestions = parseQuestions(quizJson).shuffled().take(5)

                Result.success(Quiz(
                    quizId = "${chapterId}_quiz",
                    courseId = courseId,
                    chapterId = chapterId,
                    title = chapterDoc.getString("title") ?: "Quiz de chapitre",
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
                    doc.getString("quiz")?.let { allUEQuestions.addAll(parseQuestions(it)) }
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

    // ============================================
    // ⭐ QUIZ DU JOUR (LOGIQUE RÉVISIONS CORRIGÉE)
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

            // 1. Vérifier si un quiz a déjà été généré aujourd'hui
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

            // 2. Scan de la base de données
            val allQuestionsPool = mutableListOf<Question>()
            val globalCoursesSnapshot = firestore.collection("courses").get().await()
            val totalCourses = globalCoursesSnapshot.size()

            globalCoursesSnapshot.documents.forEachIndexed { index, courseDoc ->
                onProgress((index.toFloat() / totalCourses.toFloat()))
                val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()

                for (chapterDoc in chaptersSnapshot.documents) {
                    var shouldInclude = isDiscoveryMode // En mode découverte, on inclut tout

                    if (!isDiscoveryMode) {
                        // 🎯 CORRECTION : On vérifie tes vrais champs Firebase
                        val progressDoc = firestore.collection("user_progress")
                            .document(userId).collection("courses").document(courseDoc.id)
                            .collection("chapters").document(chapterDoc.id).get().await()

                        if (progressDoc.exists()) {
                            val isCoursRead = progressDoc.getBoolean("isCoursRead") ?: false
                            val isFdrRead = progressDoc.getBoolean("isFdrRead") ?: false

                            // Si l'un des deux est vrai, l'utilisateur a étudié ce chapitre
                            if (isCoursRead || isFdrRead) {
                                shouldInclude = true
                            }
                        }
                    }

                    if (shouldInclude) {
                        chapterDoc.getString("quiz")?.let { allQuestionsPool.addAll(parseQuestions(it)) }
                    }
                }
            }

            if (allQuestionsPool.isEmpty()) {
                val msg = if (isDiscoveryMode) "Contenu insuffisant"
                else "Il faut lire au moins un cours ou une fiche pour débloquer les Révisions ! Passe en mode Découverte. 😉"
                return@withContext Result.failure(Exception(msg))
            }

            // 3. Sélection et Sauvegarde
            val selectedQuestions = allQuestionsPool.shuffled().take(10)
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
    // ⭐ HISTORIQUE ET SAUVEGARDE
    // ============================================

    suspend fun saveQuizHistory(
        courseId: String,
        chapterId: String,
        historyEntry: QuizHistory,
        userAnswers: Map<Int, Int>? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())

            // Sécurité : On ne sauvegarde qu'une fois le Quiz du Jour par jour
            if (chapterId == "DISCOVERY" || chapterId == "REVIEW") {
                val existing = firestore.collection("quiz_results").document(userId).collection("history")
                    .whereEqualTo("date", today).whereEqualTo("chapterId", chapterId).get().await()
                if (!existing.isEmpty) return@withContext Result.success(Unit)
            }

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

            // Marquer le chapitre comme complété (pour les quiz de chapitre uniquement)
            val specialModes = listOf("ALL_CHAPTERS", "DISCOVERY", "REVIEW")
            if (!specialModes.contains(chapterId)) {
                firestore.collection("user_progress")
                    .document(userId).collection("courses").document(courseId)
                    .collection("chapters").document(chapterId)
                    .set(mapOf("isQuizCompleted" to true), SetOptions.merge())
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
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

    // ============================================
    // STATISTIQUES ET RÉPONSES
    // ============================================

    suspend fun getLastDailyQuizScore(): Result<Pair<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())

            val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .orderBy("timestamp", Query.Direction.ASCENDING).limit(1).get().await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            val doc = snapshot.documents[0]
            Result.success((doc.getLong("score")?.toInt() ?: 0) to (doc.getLong("total")?.toInt() ?: 10))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getDailyQuizAnswers(): Result<Map<Int, Int>?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val today = dateFormat.format(Date())

            val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                .whereEqualTo("date", today)
                .whereIn("chapterId", listOf("DISCOVERY", "REVIEW"))
                .orderBy("timestamp", Query.Direction.ASCENDING).limit(1).get().await()

            if (snapshot.isEmpty) return@withContext Result.success(null)

            val json = snapshot.documents[0].getString("userAnswersJson")
            val type = object : TypeToken<Map<Int, Int>>() {}.type
            Result.success(gson.fromJson(json, type))
        } catch (e: Exception) { Result.failure(e) }
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

            val snapshot = firestore.collection("quiz_results").document(userId).collection("history")
                .whereGreaterThanOrEqualTo("date", dateFormat.format(calendar.time)).get().await()

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