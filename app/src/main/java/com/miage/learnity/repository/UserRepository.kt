package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    // ============================================
    // LECTURE
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
                val isFirstAttemptOverall = !progressSnapshot.exists()

                // 1. Calcul du gain de points (Différence de record)
                // ✅ Sécurité : Ratio calculé sur le total pour éviter division par zéro si score nul
                val netPointsToAdd = if (newScore > oldBestScore && totalQuestions > 0) {
                    val ratio = pointsCalculated.toDouble() / newScore.toDouble()
                    val scoreGap = newScore - oldBestScore
                    (scoreGap * ratio).toInt()
                } else 0

                // 2. Calcul du bonus unique (1er Perfect)
                val isFirstPerfect = (newScore == totalQuestions && !hadPerfect)
                val netBonusToAdd = if (isFirstPerfect) bonusCalculated else 0

                // 3. Calcul de la dette (Seulement au premier essai pour le Quiz du Jour)
                val netDebtToAdd = if (isFirstAttemptOverall && quizType == PointsManager.QuizType.DAILY) {
                    debtCalculated
                } else 0.0

                // --- MISE À JOUR UTILISATEUR ---
                val currentTotalPoints = userSnapshot.getLong("unityPoints") ?: 0
                val currentTotalDebt = userSnapshot.getDouble("detteCumulee") ?: 0.0

                val userUpdates = mutableMapOf<String, Any>(
                    "unityPoints" to (currentTotalPoints + netPointsToAdd + netBonusToAdd),
                    "detteCumulee" to (currentTotalDebt + netDebtToAdd)
                )

                // Streak (Seulement au premier essai du QDJ aujourd'hui)
                if (quizType == PointsManager.QuizType.DAILY && isFirstAttemptOverall) {
                    val currentStreak = userSnapshot.getLong("currentStreak") ?: 0
                    val currentBestStreak = userSnapshot.getLong("bestStreak") ?: 0
                    userUpdates["currentStreak"] = currentStreak + 1
                    if (currentStreak + 1 > currentBestStreak) {
                        userUpdates["bestStreak"] = currentStreak + 1
                    }
                    userUpdates["lastDailyQuizDate"] = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }

                transaction.update(userRef, userUpdates)

                // --- MISE À JOUR PROGRESSION ---
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
    // ✅ GESTION DE LA PÉNALITÉ (Absentéisme)
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
                    "currentStreak" to 0 // On casse la flamme
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // ÉCRITURE ET SAUVEGARDE SIMPLE
    // ============================================

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(profile.uid).set(profile, SetOptions.merge()).await()
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

    // Fonction pour déduire de la dette virtuelle de l'utilisateur
    suspend fun deductFromDebt(amount: Double): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId) // Utilise 'firestore' au lieu de 'db' pour la cohérence

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                // 🎯 CHANGE "virtualDebt" par "detteCumulee" pour correspondre au reste du fichier
                val currentDebt = snapshot.getDouble("detteCumulee") ?: 0.0

                val newDebt = (currentDebt - amount).coerceAtLeast(0.0)
                transaction.update(userRef, "detteCumulee", newDebt)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}