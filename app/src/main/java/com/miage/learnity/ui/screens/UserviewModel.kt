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
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository() // ✅ Ajouté pour vérifier le record
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
            repository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
            }
        }
    }

    // ============================================
    // ⭐ TRAITEMENT DES RÉSULTATS (CORRIGÉ)
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
            // 1. On récupère le record actuel en base pour le calcul
            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()
            val oldBestScore = progress?.bestScore ?: 0
            val wasAlreadyPerfect = oldBestScore == totalQuestions

            // 2. Calcul des gains REELS via le PointsManager (Sécurité progression)
            val result = PointsManager.calculateResults(
                type = quizType,
                score = score,
                totalQuestions = totalQuestions,
                oldBestScore = oldBestScore, // ✅ Paramètre manquant corrigé
                profile = profile,
                wasAlreadyPerfect = wasAlreadyPerfect // ✅ Paramètre bonus ajouté
            )

            // 3. Sauvegarde via la transaction Firestore
            repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = score,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = result.progressionPoints, // ✅ Renommé
                bonusCalculated = result.bonusGained,
                debtCalculated = result.debtAdded
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ============================================
    // VÉRIFICATION D'ASSIDUITÉ
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
}