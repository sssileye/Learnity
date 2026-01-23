package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false,
    val updateSuccess: Boolean = false // ⭐ Ajouté pour déclencher l'animation/Snackbar
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // ⭐ On remplace loadUserProfile() par une observation en temps réel
        observeUserProfile()
    }

    /**
     * Écoute les changements du profil dans Firestore en temps réel.
     * Plus besoin de refresh manuel, l'UI se mettra à jour toute seule.
     */
    private fun observeUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false,
                    error = if (profile == null) "Profil introuvable" else null
                )
            }
        }
    }

    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        photoUrl: String? = null,
        redevance: Double? = null,
        selectedAssociationId: String? = null
    ) {
        val currentProfile = _uiState.value.profile ?: return

        viewModelScope.launch {
            // On affiche un état de chargement pendant la sauvegarde
            _uiState.value = _uiState.value.copy(isLoading = true, updateSuccess = false)

            val updatedProfile = currentProfile.copy(
                firstName = firstName ?: currentProfile.firstName,
                lastName = lastName ?: currentProfile.lastName,
                photoUrl = photoUrl ?: currentProfile.photoUrl,
                redevanceSoutienUnitaire = redevance ?: currentProfile.redevanceSoutienUnitaire,
                selectedAssociationId = selectedAssociationId ?: currentProfile.selectedAssociationId
            )

            userRepository.saveUserProfile(updatedProfile)
                .onSuccess {
                    // On ne met pas à jour le profil ici manuellement car
                    // observeUserProfile() s'en chargera dès que Firebase aura validé.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        updateSuccess = true // ⭐ Déclenche l'effet visuel dans l'UI
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Erreur de sauvegarde",
                        isLoading = false
                    )
                }
        }
    }

    /**
     * Appelé par l'UI après avoir affiché la Snackbar pour réinitialiser l'état
     */
    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing
        )
    }

    fun refresh() {
        // Optionnel maintenant grâce à observeUserProfile,
        // mais utile en cas d'erreur réseau pour relancer le flow.
        observeUserProfile()
    }
}