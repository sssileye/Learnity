package com.miage.learnity.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.screens.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    // --- États du Quiz ---
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // ✅ NOUVEAU : On stocke l'ancien record pour calculer le gain net sur le front
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

    // --- États d'Affichage & Chargement ---
    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

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

    // ============================================
    // CHARGEMENT DES DONNÉES
    // ============================================

    fun loadQuiz(courseId: String, chapterId: String) {
        resetQuizState()
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

    fun loadMegaQuiz(courseId: String) {
        resetQuizState()
        viewModelScope.launch {
            _isLoading.value = true
            fetchUserProgress(courseId, "ALL_CHAPTERS")
            repository.getMegaQuizForCourse(courseId).onSuccess { megaQuiz ->
                _quiz.value = megaQuiz
                _questions.value = megaQuiz.questions
            }
            _isLoading.value = false
        }
    }

    fun loadDailyQuiz(isDiscoveryMode: Boolean) {
        resetQuizState()
        viewModelScope.launch {
            _isLoading.value = true
            fetchUserProgress("DAILY_COURSE", "DAILY_CHAPTER")
            repository.getDailyQuiz(isDiscoveryMode) { progress ->
                _loadingProgress.value = progress
            }.onSuccess { dailyQuiz ->
                _quiz.value = dailyQuiz
                _questions.value = dailyQuiz.questions
            }
            _isLoading.value = false
        }
    }

    private suspend fun fetchUserProgress(courseId: String, chapterId: String) {
        progressRepository.getChapterProgress(courseId, chapterId).onSuccess { progress ->
            _wasAlreadyCompleted.value = progress?.isQuizCompleted == true
            // ✅ On mémorise le record AVANT le début du quiz pour le comparatif final
            _oldBestScore.value = progress?.bestScore ?: 0
        }.onFailure {
            _wasAlreadyCompleted.value = false
            _oldBestScore.value = 0
        }
    }

    // ============================================
    // LOGIQUE DU JEU
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
            finishQuiz()
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
            _isCurrentAnswerRevealed.value = _currentQuestionIndex.value < _maxIndexReached.value
        }
    }

    private fun finishQuiz() {
        calculateAndSetScore(_userAnswers.value)
        _isQuizFinished.value = true
    }

    // ============================================
    // ⭐ SYSTÈME DE POINTS ET FINALISATION
    // ============================================

    fun processFinalResults(
        quizType: PointsManager.QuizType,
        userViewModel: UserViewModel,
        courseId: String,
        chapterId: String
    ) {
        val currentQuiz = _quiz.value ?: return
        val profile = userViewModel.uiState.value.profile ?: UserProfile()

        calculateAndSetScore(_userAnswers.value)
        val finalScore = _score.value
        val totalQuestions = _questions.value.size

        // Calcul des gains basés sur le score actuel
        val calculation = PointsManager.calculateResults(
            type = quizType,
            score = finalScore,
            totalQuestions = totalQuestions,
            profile = profile
        )

        viewModelScope.launch {
            // 1. Sauvegarde historique locale
            repository.saveQuizResult(courseId, chapterId, finalScore, totalQuestions, _userAnswers.value)

            // 2. Transaction Firestore : Le back gérera la soustraction (NewScore - OldScore)
            // Mais on envoie les points théoriques calculés pour le score actuel.
            userViewModel.repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = finalScore,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = calculation.pointsGained,
                bonusCalculated = calculation.bonusGained,
                debtCalculated = calculation.debtAdded
            )
        }
    }

    private fun calculateAndSetScore(answers: Map<Int, Int>) {
        var finalScore = 0
        _questions.value.forEachIndexed { index, question ->
            if (answers[index] == question.correctAnswerIndex) finalScore++
        }
        _score.value = finalScore
    }

    // ============================================
    // UTILS
    // ============================================

    fun markSummaryAsSeen() { _hasSeenSummary.value = true }

    fun goToQuestionForReview(index: Int) {
        _currentQuestionIndex.value = index
        _isCurrentAnswerRevealed.value = true
        _isQuizFinished.value = false
    }

    fun returnToSummary() { _isQuizFinished.value = true }

    private fun resetQuizState() {
        _currentQuestionIndex.value = 0
        _maxIndexReached.value = 0
        _userAnswers.value = emptyMap()
        _isCurrentAnswerRevealed.value = false
        _isQuizFinished.value = false
        _hasSeenSummary.value = false
        _score.value = 0
        _quiz.value = null
        _questions.value = emptyList()
        _loadingProgress.value = 0f
        _wasAlreadyCompleted.value = false
        _oldBestScore.value = 0 // Réinitialisation du record
    }

    fun resetQuiz() { resetQuizState() }
}