package com.miage.learnity.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuizRepository = QuizRepository()) : ViewModel() {

    // --- États du Quiz ---
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // --- Navigation & Progression ---
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _maxIndexReached = MutableStateFlow(0)
    val maxIndexReached: StateFlow<Int> = _maxIndexReached.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    // --- États d'Affichage ---
    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
            repository.getQuizForChapter(courseId, chapterId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
            }
            _isLoading.value = false
        }
    }

    /**
     * ⭐ Charge les anciennes réponses pour le mode "Revoir"
     */
    fun loadOldAnswers() {
        viewModelScope.launch {
            repository.getDailyQuizAnswers().onSuccess { oldAnswers ->
                if (oldAnswers != null) {
                    _userAnswers.value = oldAnswers
                    calculateAndSetScore(oldAnswers)
                }
            }
        }
    }

    fun loadMegaQuiz(courseId: String) {
        resetQuizState()
        viewModelScope.launch {
            _isLoading.value = true
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
            repository.getDailyQuiz(isDiscoveryMode).onSuccess { dailyQuiz ->
                _quiz.value = dailyQuiz
                _questions.value = dailyQuiz.questions
            }
            _isLoading.value = false
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
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex >= _maxIndexReached.value) {
            _maxIndexReached.value = currentIndex + 1
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
            _isCurrentAnswerRevealed.value = true
        }
    }

    // ============================================
    // RÉCAPITULATIF ET FIN
    // ============================================

    fun finishQuiz() {
        val currentQuiz = _quiz.value ?: return

        // On calcule le score final avant de sauvegarder
        calculateAndSetScore(_userAnswers.value)

        viewModelScope.launch {
            repository.saveQuizResult(
                courseId = currentQuiz.courseId,
                chapterId = currentQuiz.chapterId,
                score = _score.value,
                total = _questions.value.size,
                userAnswers = _userAnswers.value
            )
            _isQuizFinished.value = true
        }
    }

    /**
     * Helper pour calculer le score à partir d'une map de réponses
     */
    private fun calculateAndSetScore(answers: Map<Int, Int>) {
        var finalScore = 0
        _questions.value.forEachIndexed { index, question ->
            if (answers[index] == question.correctAnswerIndex) {
                finalScore++
            }
        }
        _score.value = finalScore
    }

    fun markSummaryAsSeen() {
        _hasSeenSummary.value = true
    }

    fun goToQuestionForReview(index: Int) {
        _currentQuestionIndex.value = index
        _isCurrentAnswerRevealed.value = true
        _isQuizFinished.value = false
    }

    fun returnToSummary() {
        _isQuizFinished.value = true
    }

    // ============================================
    // UTILS
    // ============================================

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
    }

    fun resetQuiz() {
        resetQuizState()
    }
}