package com.miage.learnity.repository

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
    // ⭐ LE MOTEUR : TRANSACTION HIGH SCORE
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

                // 1. GESTION DES POINTS (Seulement si record battu)
                val netPointsToAdd = if (newScore > oldBestScore) pointsCalculated else 0

                // 2. GESTION DU BONUS (Premier 100% uniquement)
                val isFirstPerfect = (newScore == totalQuestions && !hadPerfect)
                val netBonusToAdd = if (isFirstPerfect) bonusCalculated else 0

                // 3. GESTION DE LA DETTE (À chaque essai du Daily Quiz)
                val netDebtToAdd = if (quizType == PointsManager.QuizType.DAILY) debtCalculated else 0.0

                // Récupération des totaux actuels
                val currentTotalPoints = userSnapshot.getLong("unityPoints") ?: 0
                val currentTotalDebt = userSnapshot.getDouble("detteCumulee") ?: 0.0

                val userUpdates = mutableMapOf<String, Any>(
                    "unityPoints" to (currentTotalPoints + netPointsToAdd + netBonusToAdd),
                    "detteCumulee" to (currentTotalDebt + netDebtToAdd)
                )

                // Streak & Date (Uniquement Daily Quiz)
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

                // Mise à jour du record de progression
                val progressUpdates = mutableMapOf<String, Any>(
                    "bestScore" to maxOf(oldBestScore, newScore),
                    "isQuizCompleted" to true
                )
                if (isFirstPerfect) progressUpdates["isPerfectCompleted"] = true

                transaction.set(progressRef, progressUpdates, SetOptions.merge())
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // ✅ ACTIONS UTILISATEUR (Pénalité, Dons, Paramètres)
    // ============================================

    suspend fun applyAbsenteeismPenalty(amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)
        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentDebt = snapshot.getDouble("detteCumulee") ?: 0.0
                transaction.update(userRef, mapOf(
                    "detteCumulee" to (currentDebt + amount),
                    "currentStreak" to 0
                ))
            }.await()
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

    // ============================================
    // ÉCRITURE GÉNÉRALE (Sauvegarde Profil)
    // ============================================

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(profile.uid)
                .set(profile, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}