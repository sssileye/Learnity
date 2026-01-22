// ProfileViewModel.kt
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

/**
 * ViewModel spécifique pour l'écran de profil
 * (Réutilise UserRepository)
 */
class ProfileViewModel(
    private val repository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // ✅ Option 1 : Chargement unique
        loadProfile()

        // ✅ Option 2 : Observation temps réel (décommenter si souhaité)
        // observeProfile()
    }

    /**
     * Chargement unique
     */
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    /**
     * Observation temps réel (optionnel)
     */
    private fun observeProfile() {
        viewModelScope.launch {
            repository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
            }
        }
    }

    fun enableEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(isEditMode = false)
        loadProfile()
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
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = ProfileUiState()
    }
}