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
    val isEditing: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Erreur de chargement",
                        isLoading = false
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
            val updatedProfile = currentProfile.copy(
                firstName = firstName ?: currentProfile.firstName,
                lastName = lastName ?: currentProfile.lastName,
                photoUrl = photoUrl ?: currentProfile.photoUrl,
                redevanceSoutienUnitaire = redevance ?: currentProfile.redevanceSoutienUnitaire,
                selectedAssociationId = selectedAssociationId ?: currentProfile.selectedAssociationId
            )

            userRepository.saveUserProfile(updatedProfile)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        profile = updatedProfile,
                        isEditing = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Erreur de sauvegarde"
                    )
                }
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditing = !_uiState.value.isEditing
        )
    }

    fun refresh() {
        loadUserProfile()
    }
}