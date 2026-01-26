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

/**
 * Repository pour gérer les profils utilisateurs
 */
class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ============================================
    // LECTURE (Une seule fois)
    // ============================================

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    suspend fun getUserProfile(): Result<UserProfile?> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val profile = doc.toObject(UserProfile::class.java)

            println("✅ UserRepository - Profil chargé : ${profile?.email}")
            Result.success(profile)

        } catch (e: Exception) {
            println("❌ UserRepository - Erreur : ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // ÉCRITURE
    // ============================================

    /**
     * Sauvegarde le profil complet
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users")
                .document(profile.uid)
                .set(profile)
                .await()

            println("✅ UserRepository - Profil sauvegardé : ${profile.email}")
            Result.success(Unit)

        } catch (e: Exception) {
            println("❌ UserRepository - Erreur sauvegarde : ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Met à jour des champs spécifiques (merge)
     */
    suspend fun updateUserFields(fields: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))

            firestore.collection("users")
                .document(userId)
                .set(fields, SetOptions.merge())
                .await()

            println("✅ UserRepository - Champs mis à jour : $fields")
            Result.success(Unit)

        } catch (e: Exception) {
            println("❌ UserRepository - Erreur mise à jour : ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Crée un profil initial pour un nouvel utilisateur
     */
    suspend fun createInitialProfile(uid: String, email: String, firstName: String, lastName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val initialProfile = UserProfile(
                uid = uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                photoUrl = "avatar_b1",
                createdAt = System.currentTimeMillis(),
                redevanceSoutienUnitaire = 1.0,
                detteCumulee = 0.0,
                unityPoints = 0,
                currentStreak = 0,
                bestStreak = 0,
                lastDailyQuizDate = null,
                selectedAssociationId = null
            )

            firestore.collection("users")
                .document(uid)
                .set(initialProfile)
                .await()

            println("✅ UserRepository - Profil initial créé avec avatar : $email")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ UserRepository - Erreur création profil : ${e.message}")
            Result.failure(e)
        }
    }

    // ============================================
    // LISTENERS TEMPS RÉEL
    // ============================================

    /**
     * 🔥 Observe le profil en temps réel
     */
    fun observeUserProfile(): Flow<UserProfile?> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            trySend(null)
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // ⭐ GESTION DU CRASH DÉCONNEXION
                    // Si l'erreur est liée aux permissions (signOut), on ferme le flux sans crash
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        println("ℹ️ UserRepository - Permission refusée (normal lors du signOut)")
                        return@addSnapshotListener
                    }
                    println("❌ UserRepository - Erreur Listener : ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            println("🔥 UserRepository - Listener supprimé")
            listener.remove()
        }
    }

    // ============================================
    // OPÉRATIONS SPÉCIFIQUES
    // ============================================

    /**
     * Incrémente les Unity Points
     */
    suspend fun addUnityPoints(points: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getUserProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("Profil non trouvé"))

            val newPoints = profile.unityPoints + points
            updateUserFields(mapOf("unityPoints" to newPoints))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ajoute de la dette
     */
    suspend fun addDebt(amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getUserProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("Profil non trouvé"))

            val newDebt = profile.detteCumulee + amount
            updateUserFields(mapOf("detteCumulee" to newDebt))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour le streak
     */
    suspend fun updateStreak(newStreak: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = getUserProfile().getOrNull()
                ?: return@withContext Result.failure(Exception("Profil non trouvé"))

            val bestStreak = maxOf(profile.bestStreak, newStreak)
            updateUserFields(mapOf(
                "currentStreak" to newStreak,
                "bestStreak" to bestStreak
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Met à jour la date du dernier quiz
     */
    suspend fun updateLastQuizDate(date: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            updateUserFields(mapOf("lastDailyQuizDate" to date))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}