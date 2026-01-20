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

    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    private val _maxIndexReached = MutableStateFlow(0)
    val maxIndexReached: StateFlow<Int> = _maxIndexReached.asStateFlow()

    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadQuiz(courseId: String, chapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getQuizForChapter(courseId, chapterId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
            }
        }
    }

    fun selectAnswer(selectedIndex: Int) {
        // On ne peut sélectionner que si la réponse n'est pas encore révélée
        // et qu'on n'est pas en train de revoir une question passée.
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
            // Si la question suivante a déjà été validée auparavant, on montre direct la réponse
            _isCurrentAnswerRevealed.value = _currentQuestionIndex.value < _maxIndexReached.value
        } else {
            finishQuiz()
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
            _isCurrentAnswerRevealed.value = true // Toujours révélé en mode navigation arrière
        }
    }

    /**
     * Appelé depuis le récapitulatif pour revoir une question précise
     */
    fun goToQuestionForReview(index: Int) {
        _currentQuestionIndex.value = index
        _isCurrentAnswerRevealed.value = true
        _isQuizFinished.value = false
    }

    /**
     * Appelé par le bouton "Retour au récapitulatif" dans l'interface de Review
     */
    fun returnToSummary() {
        _isQuizFinished.value = true
    }

    private fun finishQuiz() {
        val questions = _questions.value
        val answers = _userAnswers.value
        var finalScore = 0

        questions.forEachIndexed { index, q ->
            if (answers[index] == q.correctAnswerIndex) finalScore++
        }

        _score.value = finalScore
        _isQuizFinished.value = true

        viewModelScope.launch {
            _quiz.value?.let {
                repository.saveQuizResult(
                    courseId = it.courseId,
                    chapterId = it.chapterId,
                    score = finalScore,
                    total = questions.size
                )
            }
        }
    }

    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        _maxIndexReached.value = 0
        _userAnswers.value = emptyMap()
        _isCurrentAnswerRevealed.value = false
        _isQuizFinished.value = false
        _score.value = 0
    }
}