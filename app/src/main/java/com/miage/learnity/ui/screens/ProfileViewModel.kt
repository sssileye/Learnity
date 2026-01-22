package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miage.learnity.data.UserProfile
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // ============================================
    // CHARGEMENT DU PROFIL
    // ============================================

    /**
     * ✅ Méthode PUBLIQUE pour charger/rafraîchir le profil
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                    println("✅ ProfileViewModel - Profil chargé : ${profile?.email}")
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur de chargement"
                    )
                    println("❌ ProfileViewModel - Erreur : ${exception.message}")
                }
        }
    }

    /**
     * ✅ Alias pour rafraîchir (optionnel mais plus explicite)
     */
    fun refresh() {
        loadProfile()
    }

    // ============================================
    // OBSERVATION TEMPS RÉEL (Optionnel)
    // ============================================

    /**
     * 🔥 Active l'observation temps réel du profil
     * À appeler si vous voulez que les changements Firebase
     * soient automatiquement reflétés dans l'UI
     */
    fun observeProfile() {
        viewModelScope.launch {
            repository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
                println("🔥 ProfileViewModel - Profil mis à jour en temps réel")
            }
        }
    }

    // ============================================
    // ÉDITION DU PROFIL
    // ============================================

    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(isEditMode = false)
        loadProfile() // Recharger pour annuler les modifications
    }

    fun saveProfile(firstName: String, lastName: String) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile ?: return@launch

            _uiState.value = _uiState.value.copy(isLoading = true)

            val updatedProfile = currentProfile.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim()
            )

            repository.saveUserProfile(updatedProfile)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        profile = updatedProfile,
                        isLoading = false,
                        isEditMode = false
                    )
                    println("✅ ProfileViewModel - Profil sauvegardé")
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    println("❌ ProfileViewModel - Erreur sauvegarde : ${exception.message}")
                }
        }
    }

    // ============================================
    // MISE À JOUR REDEVANCE
    // ============================================

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile ?: return@launch

            val updatedProfile = currentProfile.copy(
                redevanceSoutienUnitaire = newValue
            )

            repository.saveUserProfile(updatedProfile)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(profile = updatedProfile)
                    println("✅ ProfileViewModel - Redevance mise à jour : $newValue€")
                }
                .onFailure { exception ->
                    println("❌ ProfileViewModel - Erreur : ${exception.message}")
                }
        }
    }

    // ============================================
    // DÉCONNEXION
    // ============================================

    fun signOut() {
        auth.signOut()
        _uiState.value = ProfileUiState()
    }
}