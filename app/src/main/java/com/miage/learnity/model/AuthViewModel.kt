package com.miage.learnity.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.UserProfile
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    val error: String? = null,
    val resetPasswordSuccess: Boolean = false,
    val accountDeleteSuccess: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = user != null
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply {
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository()

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    // ============================================
    // CONNEXION
    // ============================================

    fun signIn(email: String, password: String) {
        setLoading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ok()
                } else {
                    fail(task.exception)
                }
            }
    }

    // ============================================
    // INSCRIPTION + CRÉATION PROFIL
    // ============================================

    /**
     * Inscription simplifiée : La redevance est fixée par défaut (ex: 1.0).
     * Le flag isFirstLogin est mis à TRUE pour déclencher l'onboarding en Home.
     */
    fun signUp(email: String, password: String, firstName: String, lastName: String, redevance: Double = 1.0) {
        setLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // ✅ Création du profil avec le flag isFirstLogin à true
                        createUserProfile(user.uid, email, firstName, lastName, redevance, isFirstLogin = true)
                    }
                    ok()
                } else {
                    fail(task.exception)
                }
            }
    }

    private fun createUserProfile(
        uid: String,
        email: String,
        firstName: String,
        lastName: String,
        redevance: Double,
        isFirstLogin: Boolean
    ) {
        val newProfile = UserProfile(
            uid = uid,
            email = email,
            firstName = firstName,
            lastName = lastName,
            photoUrl = "avatar_b1",
            createdAt = System.currentTimeMillis(),
            redevanceSoutienUnitaire = redevance,
            isFirstLogin = isFirstLogin, // ✅ Nouveau flag pour l'onboarding
            detteCumulee = 0.0,
            unityPoints = 0,
            currentStreak = 0,
            bestStreak = 0
        )

        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserProfile(newProfile)
                .onSuccess {
                    println("✅ AuthViewModel - Profil créé (First Login: $isFirstLogin)")
                }
                .onFailure { e ->
                    println("❌ AuthViewModel - Échec création profil : ${e.message}")
                }
        }
    }

    // ============================================
    // RÉINITIALISATION MOT DE PASSE
    // ============================================

    fun resetPassword(email: String) {
        setLoading()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        resetPasswordSuccess = true
                    )
                } else {
                    fail(task.exception)
                }
            }
    }

    fun clearResetPasswordSuccess() {
        _state.value = _state.value.copy(resetPasswordSuccess = false)
    }

    // ============================================
    // DÉCONNEXION
    // ============================================

    fun signOut() {
        auth.signOut()
        _state.value = _state.value.copy(user = null)
    }

    // ============================================
    // SUPPRESSION DE COMPTE
    // ============================================

    fun deleteAccount() {
        val currentUser = auth.currentUser ?: run {
            _state.value = _state.value.copy(error = "Aucun utilisateur connecté")
            return
        }

        setLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = currentUser.uid

                // 1. Supprimer le document utilisateur
                firestore.collection("users").document(uid).delete().await()

                // 2. Supprimer la progression (courses + chapters)
                val userProgressRef = firestore.collection("user_progress").document(uid)
                val coursesSnapshot = userProgressRef.collection("courses").get().await()

                for (courseDoc in coursesSnapshot.documents) {
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    for (chapterDoc in chaptersSnapshot.documents) {
                        chapterDoc.reference.delete().await()
                    }
                    courseDoc.reference.delete().await()
                }
                userProgressRef.delete().await()

                // 3. Supprimer Firebase Auth
                currentUser.delete().await()

                _state.value = _state.value.copy(
                    isLoading = false,
                    user = null,
                    accountDeleteSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Échec de la suppression : ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearAccountDeleteSuccess() {
        _state.value = _state.value.copy(accountDeleteSuccess = false)
    }

    // ============================================
    // GESTION ÉTATS & ERREURS
    // ============================================

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun setLoading() {
        _state.value = _state.value.copy(isLoading = true, error = null)
    }

    private fun ok() {
        _state.value = _state.value.copy(
            isLoading = false,
            user = auth.currentUser,
            error = null
        )
    }

    private fun fail(ex: Exception?) {
        _state.value = _state.value.copy(
            isLoading = false,
            error = mapError(ex)
        )
    }

    private fun mapError(ex: Exception?): String {
        val e = ex ?: return "Échec de l'authentification."
        return when (e) {
            is FirebaseAuthUserCollisionException -> "Email déjà enregistré."
            is FirebaseAuthInvalidCredentialsException -> "Identifiants invalides."
            is FirebaseAuthInvalidUserException -> "Aucun compte trouvé."
            is FirebaseNetworkException -> "Pas de connexion internet."
            else -> e.localizedMessage ?: "Une erreur est survenue."
        }
    }
}