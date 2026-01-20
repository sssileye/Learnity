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

    // On garde l'objet Quiz complet pour le titre et les IDs
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    // Liste des questions extraites (pour faciliter l'accès dans le Screen)
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadQuiz(courseId: String, chapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getQuizForChapter(courseId, chapterId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
                _isLoading.value = false
            }.onFailure { e ->
                _error.value = e.message ?: "Erreur lors du chargement"
                _isLoading.value = false
            }
        }
    }

    /**
     * Logique de validation de réponse et progression
     */
    fun onAnswerSelected(selectedIndex: Int) {
        val currentQuestions = _questions.value
        if (currentQuestions.isEmpty() || _currentQuestionIndex.value >= currentQuestions.size) return

        // 1. Vérifier si la réponse est correcte
        val correctIndex = currentQuestions[_currentQuestionIndex.value].correctAnswerIndex
        if (selectedIndex == correctIndex) {
            _score.value += 1
        }

        // 2. Passer à la question suivante ou finir
        if (_currentQuestionIndex.value < currentQuestions.size - 1) {
            _currentQuestionIndex.value += 1
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        _isQuizFinished.value = true
        // Optionnel : Sauvegarder le résultat dans Firebase ici
        viewModelScope.launch {
            val q = _quiz.value
            if (q != null) {
                repository.saveQuizResult(
                    courseId = q.courseId,
                    chapterId = q.chapterId,
                    score = _score.value,
                    total = _questions.value.size
                )
            }
        }
    }

    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        _score.value = 0
        _isQuizFinished.value = false
    }
}