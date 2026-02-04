package com.miage.learnity.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ============================================
    // LECTURE & OBSERVATION
    // ============================================

    suspend fun getUserProfile(): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            val doc = firestore.collection("users").document(userId).get().await()
            Result.success(doc.toObject(UserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(null)
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { listener.remove() }
    }

    // ============================================
    // ⭐ TRANSACTION ABSENTÉISME (SÉCURISÉE)
    // ============================================

    /**
     * Applique la pénalité avec bouclier de points et sécurité de date.
     * 10 pts = 1 jour protégé.
     */
    /**
     * Applique la pénalité avec bouclier de points et renvoie le bilan textuel.
     * Change le type de retour de Result<Unit> à Result<String>.
     */
    suspend fun applyAbsenteeismPenalty(missedDays: Int, redevanceUnitaire: Double): Result<String> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)
        val pointPerAbsentDay = 10

        try {
            val messageBilan = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                // 1. État initial
                val oldPoints = snapshot.getLong("unityPoints")?.toInt() ?: 0
                val oldDebt = snapshot.getDouble("detteCumulee") ?: 0.0

                // 2. Calcul du bouclier
                val potentialDaysCovered = oldPoints / pointPerAbsentDay
                val actualDaysCovered = potentialDaysCovered.coerceAtMost(missedDays)
                val pointsToDeduct = actualDaysCovered * pointPerAbsentDay
                val remainingDays = missedDays - actualDaysCovered
                val debtToAdd = remainingDays * redevanceUnitaire

                // 3. État final
                val newPoints = oldPoints - pointsToDeduct
                val newDebt = oldDebt + debtToAdd

                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val updates = mutableMapOf<String, Any>(
                    "unityPoints" to newPoints,
                    "detteCumulee" to newDebt,
                    "currentStreak" to 0,
                    "lastDailyQuizDate" to todayStr
                )
                transaction.update(userRef, updates)

                // 4. Construction du message (Sobre et aligné)

                val rappel = "Rappel : 1 jour d'absence = 10 Unity Points"

                val situation = if (remainingDays == 0) {
                    "-> Absence de $missedDays jours \n-> Intégralement couverte par vos points."
                } else {
                    "Absence de $missedDays jours \n-> Dont $remainingDays jours ajoutés à la dette."
                }

                val ligneSeparatrice = "--------------------------------------"

                val detailTarif = "Rappel du montant de redevance unitaire : ${String.format("%.2f", redevanceUnitaire)}€/jour"

                val detailPoints = "Mes Unity Points :\n\nAncien Score : $oldPoints\nScore Actualisé : $newPoints (-$pointsToDeduct)"

                val detailDette = "Ma Dette Virtuelle\n\nAncienne Dette : ${String.format("%.2f", oldDebt)} €\nDette Actualisée : ${String.format("%.2f", newDebt)} € (+${String.format("%.2f", debtToAdd)} €)"

                val alerte = if (newPoints < 10) "\n-> Note : Solde de points insuffisant pour une future protection." else ""

                // Assemblage final
                rappel + "\n" + situation + "\n" + ligneSeparatrice + "\n" + detailTarif + "\n" + ligneSeparatrice + "\n\n" + detailPoints + "\n" + ligneSeparatrice + "\n\n" + detailDette + "\n"+ alerte
            }.await()

            Result.success(messageBilan ?: "")
        } catch (e: Exception) {
            android.util.Log.e("LearnityDebug", "Erreur transaction pénalité : ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // ⭐ TRANSACTION HIGH SCORE
    // ============================================

    suspend fun updateStatsWithHighscore(
        courseId: String,
        chapterId: String,
        newScore: Int,
        totalQuestions: Int,
        quizType: PointsManager.QuizType,
        pointsCalculated: Int,
        bonusCalculated: Int,
        debtCalculated: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)

        val progressRef = firestore.collection("user_progress")
            .document(userId).collection("courses")
            .document(courseId).collection("chapters")
            .document(chapterId)

        try {
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val progressSnapshot = transaction.get(progressRef)

                val oldBestScore = progressSnapshot.getLong("bestScore")?.toInt() ?: 0
                val hadPerfect = progressSnapshot.getBoolean("isPerfectCompleted") ?: false

                val netPointsToAdd = if (quizType == PointsManager.QuizType.DAILY) {
                    pointsCalculated
                } else {
                    if (newScore > oldBestScore) pointsCalculated else 0
                }

                val isFirstPerfect = (newScore == totalQuestions && !hadPerfect)
                val netBonusToAdd = if (isFirstPerfect) bonusCalculated else 0
                val netDebtToAdd = if (quizType == PointsManager.QuizType.DAILY) debtCalculated else 0.0

                val currentTotalPoints = userSnapshot.getLong("unityPoints") ?: 0
                val currentTotalDebt = userSnapshot.getDouble("detteCumulee") ?: 0.0

                val userUpdates = mutableMapOf<String, Any>(
                    "unityPoints" to (currentTotalPoints + netPointsToAdd + netBonusToAdd),
                    "detteCumulee" to (currentTotalDebt + netDebtToAdd)
                )

                if (quizType == PointsManager.QuizType.DAILY) {
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val lastDate = userSnapshot.getString("lastDailyQuizDate")

                    if (lastDate != todayStr) {
                        val currentStreak = userSnapshot.getLong("currentStreak") ?: 0
                        val currentBestStreak = userSnapshot.getLong("bestStreak") ?: 0

                        userUpdates["currentStreak"] = currentStreak + 1
                        if (currentStreak + 1 > currentBestStreak) {
                            userUpdates["bestStreak"] = currentStreak + 1
                        }
                        userUpdates["lastDailyQuizDate"] = todayStr
                    }
                }

                transaction.update(userRef, userUpdates)

                val progressUpdates = mutableMapOf<String, Any>(
                    "bestScore" to maxOf(oldBestScore, newScore),
                    "isQuizCompleted" to true
                )
                if (isFirstPerfect) progressUpdates["isPerfectCompleted"] = true

                transaction.set(progressRef, progressUpdates, SetOptions.merge())
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LearnityDebug", "❌ Erreur Transaction: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateQuizMode(mode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            firestore.collection("users").document(userId).update("quizMode", mode).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deductFromDebt(amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)
        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentDebt = snapshot.getDouble("detteCumulee") ?: 0.0
                val newDebt = (currentDebt - amount).coerceAtLeast(0.0)
                transaction.update(userRef, "detteCumulee", newDebt)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateRedevanceUnitaire(value: Double): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            firestore.collection("users").document(userId).update("redevanceSoutienUnitaire", value).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(profile.uid).set(profile, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}