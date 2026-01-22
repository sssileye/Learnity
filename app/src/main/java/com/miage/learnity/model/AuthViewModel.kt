package com.miage.learnity.model

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    val error: String? = null,
    val resetPasswordSuccess: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = user != null
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply {
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    private val firestore = FirebaseFirestore.getInstance()

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

    fun signUp(email: String, password: String){
        setLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ Créer le profil utilisateur
                    val user = auth.currentUser
                    if (user != null) {
                        createUserProfile(user.uid, email)
                    }
                    ok()
                } else {
                    fail(task.exception)
                }
            }
    }

    private fun createUserProfile(uid: String, email: String) {
        val userRepository = com.miage.learnity.repository.UserRepository()
        val newProfile = com.miage.learnity.data.UserProfile(
            uid = uid,
            email = email,
            firstName = "",
            lastName = "",
            createdAt = System.currentTimeMillis(),
            redevanceSoutienUnitaire = 1.0,
            detteCumulee = 0.0,
            unityPoints = 0,
            currentStreak = 0,
            bestStreak = 0
        )

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userRepository.saveUserProfile(newProfile)
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