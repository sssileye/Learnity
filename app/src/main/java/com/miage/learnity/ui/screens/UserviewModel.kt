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


/**
 * État UI enrichi pour le profil et la progression
 */
data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val readChaptersCount: Int = 0,
    val totalChaptersCount: Int = 0,
    val dailyScore: Pair<Int, Int>? = null,    // ⭐ Ajouté
    val weeklyProgress: Pair<Int, Int>? = null // ⭐ Ajouté
)

class UserViewModel(
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // ⭐ Liste Dynamique des Associations récupérées de Firebase
    private val _associations = MutableStateFlow<List<Association>>(emptyList())
    val associations: StateFlow<List<Association>> = _associations.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
        refreshProgressionStats()
        firestore.clearPersistence()
        // ⭐ Chargement immédiat des associations
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

    /**
     * SYNCHRONISATION GLOBALE
     */
    fun refreshProgressionStats() {
        calculateReadChaptersCount()
        calculateTotalChaptersCount()
    }

    /**
     * 🔍 RÉCUPÉRATION DES ASSOCIATIONS DEPUIS FIREBASE
     */
    private fun fetchAssociations() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("Associations").get().await()

                // ⭐ LE LOG RÉVÉLATEUR
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

    /**
     * Calcule le nombre de chapitres lus par l'utilisateur (via CollectionGroup)
     */
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

    /**
     * Calcule le nombre total de chapitres dans le catalogue
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

//    fun processQuizResult( //A SUPPRIMER
//        quizType: PointsManager.QuizType,
//        score: Int,
//        totalQuestions: Int,
//        courseId: String,
//        chapterId: String
//    ) {
//        val profile = _uiState.value.profile ?: return
//        viewModelScope.launch {
//            // --- ÉTAPE 1 : ENVOI ANALYTICS (AVANT LA SAUVEGARDE) ---
//            // On log pour vérifier que le chapterId est bien détecté
//            Log.d("LearnityAnalytics", "Tentative d'envoi pour chapterId: $chapterId")
//
//            if (chapterId == "REVIEW" || chapterId == "DISCOVERY") {
//                Firebase.analytics.logEvent("qdj_completed_today") {
//                    param("score", score.toLong())
//                    param("mode", quizType.name)
//                }
//                Log.d("LearnityAnalytics", "✅ Signal QDJ envoyé à Firebase Analytics")
//            }
//
//            // --- ÉTAPE 2 : LOGIQUE DE POINTS ET SAUVEGARDE FIRESTORE ---
//            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()
//            val absoluteBestScore = progress?.bestScore ?: 0
//            val wasAlreadyPerfect = absoluteBestScore >= totalQuestions
//
//            val result = PointsManager.calculateResults(
//                type = quizType,
//                score = score,
//                totalQuestions = totalQuestions,
//                oldBestScore = absoluteBestScore,
//                profile = profile,
//                wasAlreadyPerfect = wasAlreadyPerfect
//            )
//
//            repository.updateStatsWithHighscore(
//                courseId = courseId,
//                chapterId = chapterId,
//                newScore = score,
//                totalQuestions = totalQuestions,
//                quizType = quizType,
//                pointsCalculated = result.progressionPoints,
//                bonusCalculated = result.bonusGained,
//                debtCalculated = result.debtAdded
//            ).onSuccess {
//                refreshProgressionStats()
//                Log.d("LearnityAnalytics", "📊 Sauvegarde Firestore réussie")
//            }.onFailure { e ->
//                _uiState.update { it.copy(error = "Erreur sauvegarde : ${e.message}") }
//                Log.e("LearnityAnalytics", "❌ Erreur sauvegarde Firestore : ${e.message}")
//            }
//        }
//    }
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
    /**
     * 🔥 GESTION DU TOKEN DE NOTIFICATION (FCM)
     * Récupère l'identifiant unique du téléphone et le stocke dans Firestore
     */
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
                    // On met à jour le champ fcmToken dans le document de l'utilisateur
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