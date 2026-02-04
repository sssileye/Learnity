package com.miage.learnity.ui.screens.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.screens.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    // --- États du Quiz ---
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _courseTitle = MutableStateFlow("")
    val courseTitle: StateFlow<String> = _courseTitle.asStateFlow()

    private val _chapterTitle = MutableStateFlow("")
    val chapterTitle: StateFlow<String> = _chapterTitle.asStateFlow()

    private var isResultSaved = false

    private val _oldBestScore = MutableStateFlow(0)
    val oldBestScore: StateFlow<Int> = _oldBestScore.asStateFlow()

    private val _wasAlreadyCompleted = MutableStateFlow(false)
    val wasAlreadyCompleted: StateFlow<Boolean> = _wasAlreadyCompleted.asStateFlow()

    // --- Navigation & Progression ---
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _maxIndexReached = MutableStateFlow(0)
    val maxIndexReached: StateFlow<Int> = _maxIndexReached.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    // --- États d'Affichage & Résultats ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _hasSeenSummary = MutableStateFlow(false)
    val hasSeenSummary: StateFlow<Boolean> = _hasSeenSummary.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    // ⭐ NOUVEAUX ÉTATS POUR L'AFFICHAGE DU BILAN
    private val _sessionPointsGained = MutableStateFlow(0)
    val sessionPointsGained: StateFlow<Int> = _sessionPointsGained.asStateFlow()

    private val _sessionDebtAdded = MutableStateFlow(0.0)
    val sessionDebtAdded: StateFlow<Double> = _sessionDebtAdded.asStateFlow()

    private val _multiplierUsed = MutableStateFlow(1.0)
    val multiplierUsed: StateFlow<Double> = _multiplierUsed.asStateFlow()

    private val _isFirstAttempt = MutableStateFlow(true)
    val isFirstAttempt: StateFlow<Boolean> = _isFirstAttempt.asStateFlow()

    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

    // ============================================
    // ⭐ GESTION DU CONTEXTE (TITRES)
    // ============================================

    private suspend fun fetchContextTitles(courseId: String, chapterId: String?) {
        try {
            if (courseId == "GLOBAL" || chapterId == "DISCOVERY" || chapterId == "REVIEW") {
                _courseTitle.value = "Quiz du Jour"
                _chapterTitle.value = if (chapterId == "DISCOVERY") "Mode Découverte" else "Mode Révisions"
                return
            }
            repository.getCourseDetails(courseId).onSuccess { _courseTitle.value = it.title }
            when (chapterId) {
                "ALL_CHAPTERS" -> _chapterTitle.value = "Synthèse de l'UE"
                null -> _chapterTitle.value = ""
                else -> repository.getChapterDetails(courseId, chapterId).onSuccess { _chapterTitle.value = it.title }
            }
        } catch (e: Exception) { Log.e("QuizVM", "Erreur fetchContextTitles: ${e.message}") }
    }

    // ============================================
    // CHARGEMENT DES MODES
    // ============================================

    fun loadQuiz(courseId: String, chapterId: String) {
        isResultSaved = false
        viewModelScope.launch {
            _isLoading.value = true
            fetchContextTitles(courseId, chapterId)
            fetchUserProgress(courseId, chapterId)
            repository.getQuizForChapter(courseId, chapterId).onSuccess {
                _quiz.value = it
                _questions.value = it.questions
            }
            _isLoading.value = false
        }
    }

    fun loadMegaQuiz(courseId: String) {
        isResultSaved = false
        viewModelScope.launch {
            _isLoading.value = true
            fetchContextTitles(courseId, "ALL_CHAPTERS")
            progressRepository.getChapterProgress(courseId, "ALL_CHAPTERS").onSuccess { progress ->
                _oldBestScore.value = progress?.bestScore ?: 0
                _wasAlreadyCompleted.value = (progress?.bestScore ?: 0) >= 20
            }
            repository.getMegaQuizForCourse(courseId).onSuccess {
                _quiz.value = it
                _questions.value = it.questions
            }
            _isLoading.value = false
        }
    }

    fun loadDailyQuiz(isDiscoveryMode: Boolean) {
        isResultSaved = false
        viewModelScope.launch {
            _isLoading.value = true
            repository.getLastDailyQuizScore().onSuccess { result ->
                _oldBestScore.value = result?.first ?: 0
            }
            val mode = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
            fetchContextTitles("GLOBAL", mode)
            repository.getDailyQuiz(isDiscoveryMode) { _loadingProgress.value = it }.onSuccess {
                _quiz.value = it
                _questions.value = it.questions
            }.onFailure { Log.e("QuizVM", "Erreur QDJ: ${it.message}") }
            _isLoading.value = false
        }
    }

    private suspend fun fetchUserProgress(courseId: String, chapterId: String) {
        progressRepository.getChapterProgress(courseId, chapterId).onSuccess { progress ->
            _wasAlreadyCompleted.value = progress?.isQuizCompleted == true
            _oldBestScore.value = progress?.bestScore ?: 0
        }
    }

    // ============================================
    // ⭐ LOGIQUE DE FINALISATION (CORRIGÉE)
    // ============================================

    fun processFinalResults(
        quizType: PointsManager.QuizType,
        userViewModel: UserViewModel,
        courseId: String,
        chapterId: String
    ) {
        if (isResultSaved) return
        isResultSaved = true

        val profile = userViewModel.uiState.value.profile ?: return
        calculateAndSetScore(_userAnswers.value)

        val finalScore = _score.value
        val totalQuestions = _questions.value.size

        // 1. Détection du premier essai (via la date du profil)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isDailyQuiz = (chapterId == "REVIEW" || chapterId == "DISCOVERY")

        // On considère que c'est le premier essai si la date Firestore est différente d'aujourd'hui
        val firstAttemptToday = if (isDailyQuiz) profile.lastDailyQuizDate != todayStr else true
        _isFirstAttempt.value = firstAttemptToday

        // 2. Calcul via PointsManager
        val calculation = PointsManager.calculateResults(
            type = quizType,
            score = finalScore,
            totalQuestions = totalQuestions,
            oldBestScore = _oldBestScore.value,
            profile = profile,
            wasAlreadyPerfect = _wasAlreadyCompleted.value,
            isFirstAttemptToday = firstAttemptToday // ⭐ Nouveau paramètre
        )

        // 3. Mise à jour des états pour l'UI de résultat
        _sessionPointsGained.value = calculation.progressionPoints + calculation.bonusGained
        _sessionDebtAdded.value = calculation.debtAdded
        _multiplierUsed.value = calculation.multiplierUsed

        // 4. Analytics
        if (isDailyQuiz && firstAttemptToday) {
            Firebase.analytics.logEvent("qdj_completed_today") {
                param("score", finalScore.toLong())
                param("mode", chapterId)
            }
        }

        viewModelScope.launch {
            try {
                val historyEntry = QuizHistory(
                    date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date()),
                    hour = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    score = finalScore,
                    total = totalQuestions,
                    pointsGained = _sessionPointsGained.value,
                    timestamp = System.currentTimeMillis()
                )

                // Sauvegarde historique
                repository.saveQuizHistory(courseId, chapterId, historyEntry, _userAnswers.value)

                // 5. Sauvegarde Firestore uniquement si c'est un gain réel (1er essai ou Record)
                if (firstAttemptToday || (!isDailyQuiz && finalScore > _oldBestScore.value)) {
                    userViewModel.repository.updateStatsWithHighscore(
                        courseId = courseId,
                        chapterId = chapterId,
                        newScore = finalScore,
                        totalQuestions = totalQuestions,
                        quizType = quizType,
                        pointsCalculated = calculation.progressionPoints,
                        bonusCalculated = calculation.bonusGained,
                        debtCalculated = calculation.debtAdded
                    )
                }

                userViewModel.refreshProgressionStats()
                userViewModel.refreshDailyStats()

            } catch (e: Exception) { Log.e("QuizVM", "Erreur sauvegarde: ${e.message}") }
        }
    }

    // ============================================
    // LOGIQUE DU JEU & NAVIGATION
    // ============================================

    fun selectAnswer(selectedIndex: Int) {
        if (!_isCurrentAnswerRevealed.value && _currentQuestionIndex.value >= _maxIndexReached.value) {
            _userAnswers.value = _userAnswers.value + (_currentQuestionIndex.value to selectedIndex)
        }
    }

    fun validateAnswer() {
        _isCurrentAnswerRevealed.value = true
        if (_currentQuestionIndex.value >= _maxIndexReached.value) {
            _maxIndexReached.value = _currentQuestionIndex.value + 1
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value++
            _isCurrentAnswerRevealed.value = _currentQuestionIndex.value < _maxIndexReached.value
        } else {
            calculateAndSetScore(_userAnswers.value)
            _isQuizFinished.value = true
        }
    }

    private fun calculateAndSetScore(answers: Map<Int, Int>) {
        var finalScore = 0
        _questions.value.forEachIndexed { index, question ->
            if (answers[index] == question.correctAnswerIndex) finalScore++
        }
        _score.value = finalScore
    }

    fun markSummaryAsSeen() { _hasSeenSummary.value = true }

    fun goToQuestionForReview(index: Int) {
        _currentQuestionIndex.value = index
        _isCurrentAnswerRevealed.value = true
        _isQuizFinished.value = false
    }

    fun returnToSummary() { _isQuizFinished.value = true }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
            _isCurrentAnswerRevealed.value = true
        }
    }
}