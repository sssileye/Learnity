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

/**
 * ViewModel pour gérer le profil utilisateur et ses statistiques
 */
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

    // ============================================
    // GESTION DU PROFIL (Temps réel via Firestore Snapshots)
    // ============================================

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
    // ⭐ LE CŒUR : TRAITEMENT DES RÉSULTATS
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
            // 1. Récupération du record actuel (Important pour calculer le gain réel)
            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()
            val oldBestScore = progress?.bestScore ?: 0
            val wasAlreadyPerfect = oldBestScore >= totalQuestions

            // 2. Calcul des gains différentiels
            val result = PointsManager.calculateResults(
                type = quizType,
                score = score,
                totalQuestions = totalQuestions,
                oldBestScore = oldBestScore,
                profile = profile,
                wasAlreadyPerfect = wasAlreadyPerfect
            )

            // 3. Sauvegarde via Transaction (Points + Record + Dette potentielle)
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
    // VÉRIFICATION D'ASSIDUITÉ (Pénalité de retard)
    // ============================================

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            // Utilisation de la version suspend pour un check initial propre
            val profile = repository.getUserProfile().getOrNull() ?: return@launch
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            if (profile.lastDailyQuizDate == null || profile.lastDailyQuizDate == todayStr) return@launch

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate)
                val todayDate = sdf.parse(todayStr)
                val diffMillis = todayDate!!.time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                // Si plus de 24h d'écart entre aujourd'hui et le dernier quiz (exclu)
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
    // ACTIONS PARAMÈTRES & DONS
    // ============================================

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            repository.updateRedevanceUnitaire(newValue)
        }
    }

    /**
     * Déduit un montant de la dette après confirmation du don réel
     */
    fun makeDonation(amount: Double) {
        viewModelScope.launch {
            repository.deductFromDebt(amount).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Erreur lors du don : ${e.message}")
            }
        }
    }
}