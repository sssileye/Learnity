package com.miage.learnity.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.UserRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * État UI enrichi pour le profil et la progression
 */
data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val readChaptersCount: Int = 0,
    val totalChaptersCount: Int = 0
)

class UserViewModel(
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
        refreshProgressionStats()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeUserProfile()
                .catch { e ->
                    _uiState.update { it.copy(
                        error = "Erreur de connexion : ${e.message}",
                        isLoading = false
                    ) }
                }
                .collect { profile ->
                    _uiState.update { it.copy(
                        profile = profile,
                        isLoading = false,
                        error = null
                    ) }
                }
        }
    }

    /**
     * SYNCHRONISATION GLOBALE
     */
    fun refreshProgressionStats() {
        calculateReadChaptersCount()
        calculateTotalChaptersCount()
    }

    /**
     * Compte uniquement les chapitres présents dans LA progression de l'utilisateur connecté.
     * Utilisation de CollectionGroup pour contourner les erreurs de listing hiérarchique.
     */
    private fun calculateReadChaptersCount() {
        val userId = repository.getCurrentUserId() ?: return
        val projectId = firestore.app.options.projectId

        Log.d("LearnityDebug", "--- DÉBUT SCAN DYNAMIQUE ---")
        Log.d("LearnityDebug", "Projet: $projectId | User: $userId")

        viewModelScope.launch {
            try {
                // ⭐ STRATÉGIE COLLECTION GROUP :
                // On cherche tous les documents "chapters" dans toute la base
                val snapshot = firestore.collectionGroup("chapters").get().await()

                var count = 0
                for (doc in snapshot.documents) {
                    // On filtre par le chemin pour ne garder que celui de l'utilisateur actuel
                    // Chemin attendu : user_progress/UID/courses/ID_COURS/chapters/ID_CHAPITRE
                    if (doc.reference.path.contains("user_progress/$userId")) {
                        val isCoursRead = doc.getBoolean("isCoursRead") ?: false
                        val isFdrRead = doc.getBoolean("isFdrRead") ?: false

                        if (isCoursRead || isFdrRead) {
                            count++
                            Log.d("LearnityDebug", "✅ Chapitre lu détecté : ${doc.id}")
                        }
                    }
                }

                _uiState.update { it.copy(readChaptersCount = count) }
                Log.d("LearnityDebug", "TOTAL FINAL DÉTECTÉ : $count")

            } catch (e: Exception) {
                Log.e("LearnityDebug", "❌ Erreur Scan : ${e.message}")
                // Si l'erreur mentionne un index manquant, un lien URL apparaîtra dans le Logcat
            }
        }
    }

    /**
     * Compte tous les chapitres existants dans le catalogue global (le dénominateur)
     */
    private fun calculateTotalChaptersCount() {
        viewModelScope.launch {
            try {
                val coursesSnapshot = firestore.collection("courses").get().await()
                var globalCount = 0

                for (courseDoc in coursesSnapshot.documents) {
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    globalCount += chaptersSnapshot.size()
                }

                _uiState.update { it.copy(totalChaptersCount = globalCount) }
                Log.d("LearnityDebug", "CATALOGUE TOTAL : $globalCount")
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur calcul total : ${e.message}")
            }
        }
    }

    fun updateQuizMode(newMode: String) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile
            if (currentProfile != null) {
                _uiState.update { it.copy(profile = currentProfile.copy(quizMode = newMode)) }
            }
            repository.updateQuizMode(newMode).onFailure { e ->
                _uiState.update { it.copy(error = "Erreur mode quiz : ${e.message}") }
            }
        }
    }

    fun processQuizResult(
        quizType: PointsManager.QuizType,
        score: Int,
        totalQuestions: Int,
        courseId: String,
        chapterId: String
    ) {
        val profile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()
            val absoluteBestScore = progress?.bestScore ?: 0
            val wasAlreadyPerfect = absoluteBestScore >= totalQuestions

            val result = PointsManager.calculateResults(
                type = quizType,
                score = score,
                totalQuestions = totalQuestions,
                oldBestScore = absoluteBestScore,
                profile = profile,
                wasAlreadyPerfect = wasAlreadyPerfect
            )

            repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = score,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = result.progressionPoints,
                bonusCalculated = result.bonusGained,
                debtCalculated = result.debtAdded
            ).onSuccess {
                refreshProgressionStats()
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Erreur sauvegarde : ${e.message}") }
            }
        }
    }

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            val profile = repository.getUserProfile().getOrNull() ?: return@launch
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            if (profile.lastDailyQuizDate == null || profile.lastDailyQuizDate == todayStr) return@launch

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate)
                val diffMillis = Date().time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                if (daysDiff > 1) {
                    val missedDays = (daysDiff - 1).toDouble()
                    val totalPenalty = profile.redevanceSoutienUnitaire * missedDays
                    repository.applyAbsenteeismPenalty(totalPenalty)
                }
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur assiduité : ${e.message}")
            }
        }
    }

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch { repository.updateRedevanceUnitaire(newValue) }
    }

    fun makeDonation(amount: Double) {
        viewModelScope.launch {
            repository.deductFromDebt(amount).onFailure { e ->
                _uiState.update { it.copy(error = "Erreur lors du don : ${e.message}") }
            }
        }
    }
}