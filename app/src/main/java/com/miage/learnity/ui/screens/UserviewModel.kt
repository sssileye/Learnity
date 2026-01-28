package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * État UI pour le profil utilisateur
 */
data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel pour gérer le profil utilisateur et ses statistiques
 */
class UserViewModel(
    // ✅ 'val' au lieu de 'private val' pour permettre l'accès depuis QuizViewModel
    val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
    }

    // ============================================
    // GESTION DU PROFIL (Temps réel)
    // ============================================

    private fun observeProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
            }
        }
    }

    // ============================================
    // ⭐ LE CŒUR : TRAITEMENT DES RÉSULTATS (High Score)
    // ============================================

    /**
     * Appelle la transaction sécurisée pour mettre à jour les points
     * en fonction du record personnel de l'utilisateur.
     */
    fun processQuizResult(
        quizType: PointsManager.QuizType,
        score: Int,
        totalQuestions: Int,
        courseId: String,
        chapterId: String
    ) {
        val profile = _uiState.value.profile ?: return

        // 1. Calcul des gains potentiels via le PointsManager
        val result = PointsManager.calculateResults(
            type = quizType,
            score = score,
            totalQuestions = totalQuestions,
            profile = profile
        )

        // 2. Sauvegarde via la transaction High Score du Repository
        viewModelScope.launch {
            repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = score,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = result.pointsGained,
                bonusCalculated = result.bonusGained,
                debtCalculated = result.debtAdded
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ============================================
    // VÉRIFICATION D'ASSIDUITÉ (Pénalité)
    // ============================================

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            val profile = repository.getUserProfile().getOrNull() ?: return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            if (profile.lastDailyQuizDate == null || profile.lastDailyQuizDate == todayStr) return@launch

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate)
                val todayDate = sdf.parse(todayStr)
                val diffMillis = todayDate!!.time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                if (daysDiff > 1) {
                    val missedDays = (daysDiff - 1).toDouble()
                    val totalPenalty = profile.redevanceSoutienUnitaire * missedDays
                    repository.applyAbsenteeismPenalty(totalPenalty)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Erreur assiduité : ${e.message}")
            }
        }
    }

    // ============================================
    // ACTIONS PARAMÈTRES
    // ============================================
    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            repository.updateRedevanceUnitaire(newValue)
        }
    }

    /**
     * Lance la mise à jour de la dette suite à un don
     */
    fun makeDonation(amount: Double) {
        viewModelScope.launch {
            repository.deductFromDebt(amount).onFailure { e ->
                // Mise à jour de l'état d'erreur si besoin
                _uiState.value = _uiState.value.copy(error = "Erreur don : ${e.message}")
            }
            // Note : observeProfile() recevra automatiquement la nouvelle valeur
            // de Firebase et mettra à jour l'UI partout.
        }
    }
}