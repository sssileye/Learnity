
package com.miage.learnity.ui.screens.quiz

import androidx.lifecycle.ViewModel
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizViewModel : ViewModel() {

    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadQuiz(courseId: String, chapterId: String) {
        _isLoading.value = true
        _error.value = null

        try {
            val quiz = MockData.getQuizForChapter(courseId, chapterId)

            if (quiz != null) {
                _quiz.value = quiz
            } else {
                _error.value = "Quiz non trouvé"
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Erreur inconnue"
        } finally {
            _isLoading.value = false
        }
    }
}