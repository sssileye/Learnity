package com.miage.learnity.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
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

    fun signUp(email: String, password: String, firstName: String, lastName: String, redevance: Double = 1.0) {
        setLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
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
            isFirstLogin = isFirstLogin,
            detteCumulee = 0.0,
            unityPoints = 0,
            currentStreak = 0,
            bestStreak = 0
        )

        viewModelScope.launch(Dispatchers.IO) {
            userRepository.saveUserProfile(newProfile)
                .onSuccess {
                    println("AuthViewModel - Profil créé (First Login: $isFirstLogin)")
                }
                .onFailure { e ->
                    println("AuthViewModel - Échec création profil : ${e.message}")
                }
        }
    }

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
    fun signOut() {
        auth.signOut()
        _state.value = _state.value.copy(user = null)
    }

    fun deleteAccountWithPassword(password: String) {
        val currentUser = auth.currentUser ?: run {
            _state.value = _state.value.copy(error = "Aucun utilisateur connecté")
            return
        }

        val email = currentUser.email ?: run {
            _state.value = _state.value.copy(error = "Email non disponible")
            return
        }

        setLoading()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("AccountDeletion", "Étape 1 : Ré-authentification")

                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.reauthenticate(credential).await()
                Log.d("AccountDeletion", "Ré-authentification réussie")

                val uid = currentUser.uid
                deleteFirestoreData(uid)

                Log.i("AccountDeletion", "Étape 3 : Suppression Firebase Auth")
                currentUser.delete().await()
                Log.i("AccountDeletion", "Compte supprimé avec succès")

                _state.value = _state.value.copy(
                    isLoading = false,
                    user = null,
                    accountDeleteSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e("AccountDeletion", "Mot de passe incorrect")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Mot de passe incorrect"
                )
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                Log.e("AccountDeletion", "Erreur inattendue : ré-authentification requise")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erreur inattendue. Veuillez réessayer."
                )
            } catch (e: FirebaseNetworkException) {
                Log.e("AccountDeletion", "Erreur réseau")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erreur réseau : Vérifiez votre connexion"
                )
            } catch (e: Exception) {
                Log.e("AccountDeletion", "Erreur lors de la suppression", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Échec de la suppression : ${e.localizedMessage}"
                )
            }
        }
    }

    private suspend fun deleteFirestoreData(uid: String) {
        Log.i("AccountDeletion", " Étape 2 : Suppression des données Firestore")

        firestore.collection("users").document(uid).delete().await()
        Log.d("AccountDeletion", " Document utilisateur supprimé")

        val userProgressRef = firestore.collection("user_progress").document(uid)
        val coursesSnapshot = userProgressRef.collection("courses").get().await()

        var chaptersDeleted = 0
        var coursesDeleted = 0

        for (courseDoc in coursesSnapshot.documents) {
            val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
            for (chapterDoc in chaptersSnapshot.documents) {
                chapterDoc.reference.delete().await()
                chaptersDeleted++
            }

            courseDoc.reference.delete().await()
            coursesDeleted++
        }

        userProgressRef.delete().await()
        Log.d("AccountDeletion", "Progression supprimée : $coursesDeleted cours, $chaptersDeleted chapitres")

        val quizResultsRef = firestore.collection("quiz_results").document(uid)
        val historySnapshot = quizResultsRef.collection("history").get().await()

        var quizResultsDeleted = 0
        for (historyDoc in historySnapshot.documents) {
            historyDoc.reference.delete().await()
            quizResultsDeleted++
        }

        quizResultsRef.delete().await()
        Log.d("AccountDeletion", "Historique quiz supprimé : $quizResultsDeleted résultats")

        Log.i("AccountDeletion", """
            📊 Récapitulatif Firestore :
            - User document: ✅
            - Cours : $coursesDeleted
            - Chapitres : $chaptersDeleted
            - Résultats quiz : $quizResultsDeleted
        """.trimIndent())
    }

    fun clearAccountDeleteSuccess() {
        _state.value = _state.value.copy(accountDeleteSuccess = false)
    }

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