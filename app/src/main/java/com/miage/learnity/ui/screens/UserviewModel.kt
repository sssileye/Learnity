package com.miage.learnity.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.Association
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
import com.google.firebase.messaging.FirebaseMessaging



data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val readChaptersCount: Int = 0,
    val totalChaptersCount: Int = 0,
    val dailyScore: Pair<Int, Int>? = null,
    val weeklyProgress: Pair<Int, Int>? = null,
    val penaltyMessage: String? = null
)

class UserViewModel(
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()


    private val _associations = MutableStateFlow<List<Association>>(emptyList())
    val associations: StateFlow<List<Association>> = _associations.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
        refreshProgressionStats()
        refreshDailyStats()
        firestore.clearPersistence()

        fetchAssociations()

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


    fun refreshProgressionStats() {
        calculateReadChaptersCount()
        calculateTotalChaptersCount()
    }


    private fun fetchAssociations() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("Associations").get().await()


                if (snapshot.documents.isNotEmpty()) {
                    val firstDocPath = snapshot.documents[0].reference.path
                    Log.e("LearnityAssos", "📍 CHEMIN RÉEL DANS FIREBASE : $firstDocPath")
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Association::class.java)
                }
                _associations.value = list
                Log.e("LearnityAssos", "✅ TOTAL CHARGÉ : ${list.size}")

            } catch (e: Exception) {
                Log.e("LearnityAssos", "❌ ERREUR : ${e.message}")
            }
        }
    }


    private fun calculateReadChaptersCount() {
        val userId = repository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collectionGroup("chapters").get().await()
                var count = 0
                for (doc in snapshot.documents) {
                    if (doc.reference.path.contains("user_progress/$userId")) {
                        val isCoursRead = doc.getBoolean("isCoursRead") ?: false
                        val isFdrRead = doc.getBoolean("isFdrRead") ?: false
                        if (isCoursRead || isFdrRead) count++
                    }
                }
                _uiState.update { it.copy(readChaptersCount = count) }
            } catch (e: Exception) {
                Log.e("LearnityDebug", "❌ Erreur Scan Progression : ${e.message}")
            }
        }
    }


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
            } catch (e: Exception) {
                Log.e("LearnityDebug", "❌ Erreur calcul catalogue total : ${e.message}")
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

fun refreshDailyStats() {
    val quizRepo = com.miage.learnity.repository.QuizRepository()
    viewModelScope.launch {
        quizRepo.getLastDailyQuizScore().onSuccess { score ->
            _uiState.update { it.copy(dailyScore = score) }
        }
        quizRepo.getWeeklyProgress(goalPerWeek = 4).onSuccess { progress ->
            _uiState.update { it.copy(weeklyProgress = progress) }
        }
    }
}

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {

            val profile = repository.getUserProfile().getOrNull() ?: return@launch
            val lastDateStr = profile.lastDailyQuizDate ?: return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            try {
                // 2. Normalisation de la date du jour (Minuit) pour API 24
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val lastDate = sdf.parse(lastDateStr) ?: return@launch


                val diffMillis = today.time - lastDate.time
                val daysDiff = (diffMillis / (1000 * 60 * 60 * 24)).toInt()


                if (daysDiff > 1) {
                    val missedDays = daysDiff - 1
                    val redevance = profile.redevanceSoutienUnitaire ?: 1.0

                    android.util.Log.d("Learnity_Debug", "🕵️ Absence détectée : $missedDays jours.")


                    repository.applyAbsenteeismPenalty(missedDays, redevance)
                        .onSuccess { messageBilan ->
                            android.util.Log.d("Learnity_Debug", "✅ Pénalité traitée : $messageBilan")


                            _uiState.update { it.copy(
                                penaltyMessage = messageBilan
                            ) }


                            refreshProfile()
                        }
                        .onFailure { e ->
                            android.util.Log.e("Learnity_Debug", "❌ Échec pénalité : ${e.message}")
                        }
                } else {
                    android.util.Log.d("Learnity_Debug", "✅ Assiduité OK (Écart : $daysDiff j).")
                }
            } catch (e: Exception) {
                android.util.Log.e("Learnity_Debug", "❌ Erreur calcul assiduité : ${e.message}")
            }
        }
    }
    fun dismissPenaltyPopup() {
        _uiState.update { it.copy(penaltyMessage = null) }
    }

    fun refreshProfile() {
        viewModelScope.launch {

            repository.getUserProfile().onSuccess { profile ->

                _uiState.update { it.copy(profile = profile) }
                Log.d("UserViewModel", "Profil synchronisé après pénalité")
            }.onFailure { e ->
                Log.e("UserViewModel", "Erreur lors du refresh : ${e.message}")
            }
        }
    }

    fun updateFcmToken() {
        val userId = repository.getCurrentUserId() ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("LearnityFCM", "❌ Échec récupération Token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            viewModelScope.launch {
                try {

                    firestore.collection("users").document(userId)
                        .update("fcmToken", token)
                        .await()
                    Log.d("LearnityFCM", "✅ Token mis à jour pour $userId : $token")
                } catch (e: Exception) {
                    Log.e("LearnityFCM", "❌ Erreur sauvegarde Firestore : ${e.message}")
                }
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