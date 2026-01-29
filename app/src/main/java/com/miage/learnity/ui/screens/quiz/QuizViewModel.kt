package com.miage.learnity.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.UserProfile
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.screens.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ... (tes imports restent identiques)

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    // --- États du Quiz ---
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()
    private var isResultSaved = false
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

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

    private val _sessionPointsGained = MutableStateFlow(0)
    val sessionPointsGained: StateFlow<Int> = _sessionPointsGained.asStateFlow()

    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

    // ============================================
    // CHARGEMENT DES MODES
    // ============================================

    fun loadQuiz(courseId: String, chapterId: String) {
        isResultSaved=false
        viewModelScope.launch {
            _isLoading.value = true
            fetchUserProgress(courseId, chapterId)
            repository.getQuizForChapter(courseId, chapterId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
            }
            _isLoading.value = false
        }
    }

    // Mega Quiz et Daily Quiz utilisent la même logique de reset interne
    fun loadMegaQuiz(courseId: String) {
        isResultSaved=false
        viewModelScope.launch {
            _isLoading.value = true
            progressRepository.getChapterProgress(courseId, "ALL_CHAPTERS").onSuccess { progress ->
                _oldBestScore.value = progress?.bestScore ?: 0
                _wasAlreadyCompleted.value = (progress?.bestScore ?: 0) >= 20
            }
            repository.getMegaQuizForCourse(courseId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
            }
            _isLoading.value = false
        }
    }

    fun loadDailyQuiz(isDiscoveryMode: Boolean) {
        isResultSaved=false
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDailyQuiz(isDiscoveryMode) { progress ->
                _loadingProgress.value = progress
            }.onSuccess { dailyQuiz ->
                _quiz.value = dailyQuiz
                _questions.value = dailyQuiz.questions
                repository.getLastDailyQuizScore().onSuccess { result ->
                    if (result != null) _oldBestScore.value = result.first
                }
            }
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
    // ⭐ LOGIQUE DE FINALISATION (SÉCURISÉE)
    // ============================================

    fun processFinalResults(
        quizType: PointsManager.QuizType,
        userViewModel: UserViewModel,
        courseId: String,
        chapterId: String
    ) {
        if (isResultSaved) return

        // On active le verrou pour bloquer les appels suivants (ex: retour au récap)
        isResultSaved = true
        val profile = userViewModel.uiState.value.profile ?: return // Sécurité
        calculateAndSetScore(_userAnswers.value)

        val finalScore = _score.value
        val totalQuestions = _questions.value.size
        val wasAlreadyPerfect = _oldBestScore.value >= totalQuestions

        val calculation = PointsManager.calculateResults(
            type = quizType,
            score = finalScore,
            totalQuestions = totalQuestions,
            oldBestScore = _oldBestScore.value,
            profile = profile,
            wasAlreadyPerfect = wasAlreadyPerfect
        )

        _sessionPointsGained.value = calculation.progressionPoints + calculation.bonusGained

        viewModelScope.launch {
            // Sauvegarde Historique
            val historyEntry = QuizHistory(
                date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date()),
                hour = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                score = finalScore,
                total = totalQuestions,
                pointsGained = _sessionPointsGained.value,
                timestamp = System.currentTimeMillis()
            )
            repository.saveQuizHistory(courseId, chapterId, historyEntry)

            // Sauvegarde Firebase Transactionnelle
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