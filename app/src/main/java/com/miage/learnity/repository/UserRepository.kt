package com.miage.learnity.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserProfile(): Result<UserProfile?> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("Utilisateur non connecté")
        val doc = firestore.collection("users").document(userId).get().await()
        Result.success(doc.toObject(UserProfile::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = try {
        firestore.collection("users").document(profile.uid).set(profile).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}