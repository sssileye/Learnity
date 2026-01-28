package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.UserRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

class UserViewModel(
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.observeUserProfile()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur de connexion : ${e.message}",
                        isLoading = false
                    )
                }
                .collect { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    // ============================================
    // ⭐ LOGIQUE ANTI-BUG : COMPARAISON AU RECORD ABSOLU
    // ============================================

    fun processQuizResult(
        quizType: PointsManager.QuizType,
        score: Int,
        totalQuestions: Int,
        courseId: String,
        chapterId: String
    ) {
        val profile = _uiState.value.profile ?: return

        viewModelScope.launch {
            // 1. FORCER la récupération du record actuel en base de données
            // On ne fait PAS confiance au score qui vient de l'écran de quiz
            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()

            // C'est ici que ton bug se joue : si progress est null, on met 0.
            val absoluteBestScore = progress?.bestScore ?: 0
            val wasAlreadyPerfect = absoluteBestScore >= totalQuestions

            // 2. Calcul des gains par rapport au VRAI record absolu
            val result = PointsManager.calculateResults(
                type = quizType,
                score = score,
                totalQuestions = totalQuestions,
                oldBestScore = absoluteBestScore, // On compare 1 à 4 -> Gain 0
                profile = profile,
                wasAlreadyPerfect = wasAlreadyPerfect
            )

            // 3. On n'appelle la mise à jour que si on a vraiment gagné quelque chose
            // ou si on a battu le record visuel.
            repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = score,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = result.progressionPoints,
                bonusCalculated = result.bonusGained,
                debtCalculated = result.debtAdded
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Erreur sauvegarde : ${e.message}")
            }
        }
    }

    // ============================================
    // RESTE DES FONCTIONS (ASSIDUITÉ & DONS)
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

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            repository.updateRedevanceUnitaire(newValue)
        }
    }

    fun makeDonation(amount: Double) {
        viewModelScope.launch {
            repository.deductFromDebt(amount).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Erreur lors du don : ${e.message}")
            }
        }
    }
}