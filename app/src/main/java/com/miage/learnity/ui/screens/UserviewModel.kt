package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager // ✅ Import de ton nouveau manager
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
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        // On observe en temps réel dès le début pour éviter les loadProfile() manuels
        observeProfile()
        checkAndApplyAttendancePenalty()
    }

    // ============================================
    // GESTION DU PROFIL (Real-time)
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
    // ⭐ LE CŒUR : TRAITEMENT DES RÉSULTATS
    // ============================================

    /**
     * Traite la fin d'un Quiz (Chapitre, Quotidien ou Examen)
     * Centralise Points, Dette et Streak en un seul appel Repository
     */
    fun processQuizResult(
        quizType: PointsManager.QuizType,
        score: Int,
        totalQuestions: Int
    ) {
        val profile = _uiState.value.profile ?: return

        // 1. Calcul via ton PointsManager (situé dans model)
        val result = PointsManager.calculateResults(
            type = quizType,
            score = score,
            totalQuestions = totalQuestions,
            profile = profile
        )

        // 2. Sauvegarde atomique dans Firebase
        viewModelScope.launch {
            repository.updateStatsAfterQuiz(
                pointsGained = result.pointsGained,
                debtAdded = result.debtAdded,
                isDaily = (quizType == PointsManager.QuizType.DAILY)
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ============================================
    // VÉRIFICATION D'ASSIDUITÉ (Pénalité Minuit)
    // ============================================

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            val profile = repository.getUserProfile().getOrNull() ?: return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            // On ne fait rien si c'est la première fois ou si déjà fait aujourd'hui
            if (profile.lastDailyQuizDate == null || profile.lastDailyQuizDate == todayStr) return@launch

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate)
                val todayDate = sdf.parse(todayStr)
                val diffMillis = todayDate!!.time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                if (daysDiff > 1) {
                    val missedDays = daysDiff - 1
                    // ✅ La pénalité d'absence est X (redevance unitaire) par jour raté
                    val totalPenalty = profile.redevanceSoutienUnitaire * missedDays
                    repository.applyAbsenteeismPenalty(totalPenalty)
                    println("⚠️ UserViewModel - Pénalité appliquée : $totalPenalty€")
                }
            } catch (e: Exception) {
                println("❌ Erreur assiduité : ${e.message}")
            }
        }
    }

    // ============================================
    // ACTIONS MANUELLES (Paramètres)
    // ============================================

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            repository.updateRedevanceUnitaire(newValue)
        }
    }
}