package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.miage.learnity.data.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository pour gérer les profils utilisateurs et leurs statistiques
 */
class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return@addSnapshotListener
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    // ============================================
    // ⭐ SYSTÈME DE POINTS ET DETTE (Transactionnel)
    // ============================================

    /**
     * Met à jour les stats après un quiz de manière atomique
     */
    suspend fun updateStatsAfterQuiz(
        pointsGained: Int,
        debtAdded: Double,
        isDaily: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                val currentPoints = snapshot.getLong("unityPoints") ?: 0
                val currentDebt = snapshot.getDouble("detteCumulee") ?: 0.0
                val currentStreak = snapshot.getLong("currentStreak") ?: 0
                val currentBest = snapshot.getLong("bestStreak") ?: 0

                val updates = mutableMapOf<String, Any>(
                    "unityPoints" to (currentPoints + pointsGained),
                    "detteCumulee" to (currentDebt + debtAdded)
                )

                if (isDaily) {
                    val newStreak = currentStreak + 1
                    updates["currentStreak"] = newStreak
                    if (newStreak > currentBest) {
                        updates["bestStreak"] = newStreak
                    }
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    updates["lastDailyQuizDate"] = sdf.format(Date())
                }

                transaction.update(userRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applique la pénalité d'absentéisme (Dette + Reset Streak)
     */
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================
    // ÉCRITURE ET SAUVEGARDE
    // ============================================

    /**
     * ✅ AJOUTÉ : Sauvegarde le profil complet (utilisé par AuthViewModel)
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users")
                .document(profile.uid)
                .set(profile, SetOptions.merge()) // On utilise merge pour ne pas écraser les champs non présents dans l'objet
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserFields(fields: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
            firestore.collection("users").document(userId).update(fields).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInitialProfile(uid: String, email: String, firstName: String, lastName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val initialProfile = UserProfile(
                uid = uid, email = email, firstName = firstName, lastName = lastName,
                createdAt = System.currentTimeMillis(),
                redevanceSoutienUnitaire = 1.0
            )
            // Utilisation de la nouvelle fonction saveUserProfile pour la création
            saveUserProfile(initialProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRedevanceUnitaire(value: Double): Result<Unit> {
        return updateUserFields(mapOf("redevanceSoutienUnitaire" to value))
    }
}