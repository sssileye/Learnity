package com.miage.learnity.ui.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.R
import com.miage.learnity.data.UserProfile
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditing: Boolean = false,
    val updateSuccess: Boolean = false,
    val readChaptersCount: Int = 0,
    val totalChaptersCount: Int = 0
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    val availableAvatars = listOf(
        R.drawable.avatar_b1, R.drawable.avatar_b2, R.drawable.avatar_b3,
        R.drawable.avatar_o1, R.drawable.avatar_o2, R.drawable.avatar_o3,
        R.drawable.avatar_v1, R.drawable.avatar_v2, R.drawable.avatar_v3,
        R.drawable.avatar_r1, R.drawable.avatar_r2, R.drawable.avatar_r3,
        R.drawable.avatar_vivi1
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
        refreshProgressionStats()
    }


    fun refreshProgressionStats() {
        val userId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                // 1. Scan de la progression utilisateur (Lu)
                val userCoursesRef = firestore.collection("user_progress")
                    .document(userId).collection("courses")

                val userCoursesSnapshot = userCoursesRef.get().await()
                var readCount = 0

                for (courseDoc in userCoursesSnapshot.documents) {
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    for (chapDoc in chaptersSnapshot.documents) {
                        // Utilisation de getBoolean pour éviter les erreurs de cast si le champ est null
                        val isCoursRead = chapDoc.getBoolean("isCoursRead") ?: false
                        val isFdrRead = chapDoc.getBoolean("isFdrRead") ?: false

                        if (isCoursRead || isFdrRead) {
                            readCount++
                        }
                    }
                }


                val globalCoursesSnapshot = firestore.collection("courses").get().await()
                var totalCount = 0
                for (courseDoc in globalCoursesSnapshot.documents) {
                    val globalChaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    totalCount += globalChaptersSnapshot.size()
                }

                Log.d("LearnityDebug", "Progression calculée : $readCount / $totalCount")

                _uiState.value = _uiState.value.copy(
                    readChaptersCount = readCount,
                    totalChaptersCount = totalCount
                )
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur refreshProgressionStats: ${e.message}")
            }
        }
    }

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

    fun updateQuizMode(newMode: String) {
        val currentProfile = _uiState.value.profile ?: return
        if (currentProfile.quizMode == newMode) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                profile = currentProfile.copy(quizMode = newMode)
            )

            userRepository.updateQuizMode(newMode)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur mode quiz : ${exception.message}"
                    )
                }
        }
    }

    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        photoResName: String? = null,
        redevance: Double? = null,
        selectedAssociationId: String? = null
    ) {
        val currentProfile = _uiState.value.profile ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, updateSuccess = false)

            val updatedProfile = currentProfile.copy(
                firstName = firstName ?: currentProfile.firstName,
                lastName = lastName ?: currentProfile.lastName,
                photoUrl = photoResName ?: currentProfile.photoUrl,
                redevanceSoutienUnitaire = redevance ?: currentProfile.redevanceSoutienUnitaire,
                selectedAssociationId = selectedAssociationId ?: currentProfile.selectedAssociationId
            )

            userRepository.saveUserProfile(updatedProfile)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        updateSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Erreur sauvegarde",
                        isLoading = false
                    )
                }
        }
    }

    fun getResourceName(resourceId: Int, context: Context): String {
        return context.resources.getResourceEntryName(resourceId)
    }

    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun refresh() {
        observeUserProfile()
        refreshProgressionStats()
    }
}