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
    // ✅ SUPPRESSION DE COMPTE AVEC RÉ-AUTHENTIFICATION (CORRIGÉ)
    // ============================================

    /**
     * ✅ NOUVELLE VERSION : Supprime le compte avec ré-authentification obligatoire
     * pour éviter le problème de page blanche.
     *
     * Cette fonction demande le mot de passe AVANT de commencer toute suppression,
     * garantissant une session Firebase Auth fraîche et évitant la demande de
     * ré-authentification en plein milieu du processus.
     *
     * @param password Mot de passe actuel de l'utilisateur
     */
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
                Log.i("AccountDeletion", "🔐 Étape 1 : Ré-authentification")

                // ============================================
                // ÉTAPE 1 : RÉ-AUTHENTIFIER L'UTILISATEUR
                // ============================================
                // Cela garantit une session fraîche et évite FirebaseAuthRecentLoginRequiredException
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.reauthenticate(credential).await()
                Log.d("AccountDeletion", "✅ Ré-authentification réussie")

                // ============================================
                // ÉTAPE 2 : SUPPRIMER LES DONNÉES FIRESTORE
                // ============================================
                val uid = currentUser.uid
                deleteFirestoreData(uid)

                // ============================================
                // ÉTAPE 3 : SUPPRIMER LE COMPTE FIREBASE AUTH (EN DERNIER)
                // ============================================
                // Maintenant qu'on vient de se ré-authentifier, cette étape fonctionne toujours
                Log.i("AccountDeletion", "🗑️ Étape 3 : Suppression Firebase Auth")
                currentUser.delete().await()
                Log.i("AccountDeletion", "✅ Compte supprimé avec succès")

                _state.value = _state.value.copy(
                    isLoading = false,
                    user = null,
                    accountDeleteSuccess = true,
                    error = null
                )

            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // Mot de passe incorrect
                Log.e("AccountDeletion", "❌ Mot de passe incorrect")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Mot de passe incorrect"
                )
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                // Normalement, ça ne devrait jamais arriver car on vient de se ré-authentifier
                Log.e("AccountDeletion", "❌ Erreur inattendue : ré-authentification requise")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erreur inattendue. Veuillez réessayer."
                )
            } catch (e: FirebaseNetworkException) {
                Log.e("AccountDeletion", "❌ Erreur réseau")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Erreur réseau : Vérifiez votre connexion"
                )
            } catch (e: Exception) {
                Log.e("AccountDeletion", "❌ Erreur lors de la suppression", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Échec de la suppression : ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Supprime toutes les données Firestore de l'utilisateur.
     *
     * Collections supprimées :
     * 1. users/{uid} - Document principal de l'utilisateur
     * 2. user_progress/{uid}/courses/{courseId}/chapters/{chapterId} - Progression complète
     * 3. quiz_results/{uid}/history - Historique des quiz
     *
     * @param uid ID de l'utilisateur
     */
    private suspend fun deleteFirestoreData(uid: String) {
        Log.i("AccountDeletion", "🗑️ Étape 2 : Suppression des données Firestore")

        // ============================================
        // 1. SUPPRIMER LE DOCUMENT UTILISATEUR PRINCIPAL
        // ============================================
        firestore.collection("users").document(uid).delete().await()
        Log.d("AccountDeletion", "✅ Document utilisateur supprimé")

        // ============================================
        // 2. SUPPRIMER LA PROGRESSION (COURSES + CHAPTERS)
        // ============================================
        val userProgressRef = firestore.collection("user_progress").document(uid)
        val coursesSnapshot = userProgressRef.collection("courses").get().await()

        var chaptersDeleted = 0
        var coursesDeleted = 0

        for (courseDoc in coursesSnapshot.documents) {
            // Supprimer tous les chapitres du cours
            val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
            for (chapterDoc in chaptersSnapshot.documents) {
                chapterDoc.reference.delete().await()
                chaptersDeleted++
            }

            // Supprimer le document du cours
            courseDoc.reference.delete().await()
            coursesDeleted++
        }

        // Supprimer le document user_progress principal
        userProgressRef.delete().await()
        Log.d("AccountDeletion", "✅ Progression supprimée : $coursesDeleted cours, $chaptersDeleted chapitres")

        // ============================================
        // 3. ✅ SUPPRIMER L'HISTORIQUE DES QUIZ (CORRECTION IMPORTANTE)
        // ============================================
        val quizResultsRef = firestore.collection("quiz_results").document(uid)
        val historySnapshot = quizResultsRef.collection("history").get().await()

        var quizResultsDeleted = 0
        for (historyDoc in historySnapshot.documents) {
            historyDoc.reference.delete().await()
            quizResultsDeleted++
        }

        // Supprimer le document quiz_results principal
        quizResultsRef.delete().await()
        Log.d("AccountDeletion", "✅ Historique quiz supprimé : $quizResultsDeleted résultats")

        // ============================================
        // LOG RÉCAPITULATIF
        // ============================================
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