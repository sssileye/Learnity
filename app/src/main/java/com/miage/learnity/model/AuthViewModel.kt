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

    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        setLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {

                        createUserProfile(user.uid, email, firstName, lastName)
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
        lastName: String
    ) {

        val newProfile = UserProfile(
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
            bestStreak = 0
        )


        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserProfile(newProfile)
                .onSuccess {
                    println("✅ AuthViewModel - Profil Firestore créé avec succès")
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
                    println("✅ Email de réinitialisation envoyé à : $email")
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
    //  NOUVEAU : SUPPRESSION DE COMPTE
    // ============================================

    fun deleteAccount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.value = _state.value.copy(
                error = "Aucun utilisateur connecté"
            )
            return
        }

        setLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = currentUser.uid
                println("🗑️ Début de la suppression du compte : $uid")

                // 1. Supprimer le document utilisateur dans users/{uid}
                firestore.collection("users")
                    .document(uid)
                    .delete()
                    .await()
                println("✅ Document utilisateur supprimé")

                // 2. Supprimer toute la progression dans user_progress/{uid}
                // On doit d'abord récupérer toutes les sous-collections
                val userProgressRef = firestore.collection("user_progress").document(uid)

                // Supprimer la collection courses et ses sous-collections
                val coursesSnapshot = userProgressRef.collection("courses").get().await()
                for (courseDoc in coursesSnapshot.documents) {
                    // Supprimer les chapitres de chaque cours
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    for (chapterDoc in chaptersSnapshot.documents) {
                        chapterDoc.reference.delete().await()
                    }
                    // Supprimer le document cours
                    courseDoc.reference.delete().await()
                }

                // Supprimer le document user_progress principal
                userProgressRef.delete().await()
                println("✅ Progression utilisateur supprimée")

                // 3. Supprimer le compte Firebase Auth
                currentUser.delete().await()
                println("✅ Compte Firebase Auth supprimé")

                // 4. Mettre à jour l'état
                _state.value = _state.value.copy(
                    isLoading = false,
                    user = null,
                    accountDeleteSuccess = true,
                    error = null
                )
                println("✅ Suppression du compte terminée avec succès")

            } catch (e: Exception) {
                println("❌ Erreur lors de la suppression : ${e.message}")
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
    // GESTION ERREURS
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
        val e = ex ?: return "Échec de l'authentification. Veuillez réessayer."

        return when (e) {
            is FirebaseAuthUserCollisionException ->
                "Cet email est déjà enregistré. Essayez de vous connecter ou de réinitialiser votre mot de passe."

            is FirebaseAuthInvalidCredentialsException ->
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "L'adresse email est mal formatée."
                    "ERROR_WRONG_PASSWORD" -> "Mot de passe incorrect. Veuillez réessayer."
                    else -> "Identifiants invalides. Vérifiez votre email et mot de passe."
                }

            is FirebaseAuthInvalidUserException ->
                when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "Aucun compte trouvé avec cet email."
                    "ERROR_USER_DISABLED" -> "Votre compte a été désactivé."
                    else -> "Ce compte n'est pas valide."
                }

            is FirebaseNetworkException ->
                "Pas de connexion internet. Vérifiez votre réseau et réessayez."

            else -> {
                val code = (e as? FirebaseAuthException)?.errorCode
                when (code) {
                    "ERROR_EMAIL_ALREADY_IN_USE" ->
                        "Cet email est déjà enregistré. Essayez de vous connecter ou de réinitialiser votre mot de passe."

                    "ERROR_WEAK_PASSWORD" ->
                        "Mot de passe trop faible. Utilisez au moins 6 caractères."

                    "ERROR_OPERATION_NOT_ALLOWED" ->
                        "La connexion email/mot de passe est désactivée pour ce projet."

                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Trop de tentatives. Veuillez réessayer plus tard."

                    else ->
                        e.localizedMessage?.substringBefore('\n')
                            ?: "Échec de l'authentification. Veuillez réessayer."
                }
            }
        }
    }
}