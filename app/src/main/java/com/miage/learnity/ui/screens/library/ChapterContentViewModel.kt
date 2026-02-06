package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterContentViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository(),
    private val quizRepository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    private val _history = MutableStateFlow<List<QuizHistory>>(emptyList())
    val history: StateFlow<List<QuizHistory>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentCourseId: String = ""
    private var currentChapterId: String = ""

    fun loadChapter(courseId: String, chapterId: String) {
        currentCourseId = courseId
        currentChapterId = chapterId

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null


            courseRepository.getChapter(courseId, chapterId, auth.currentUser?.uid)
                .onSuccess { chapter ->
                    startProgressListener(courseId, chapterId, chapter)

                    loadQuizHistory(courseId, chapterId)
                }
                .onFailure {
                    _error.value = it.message ?: "Erreur lors du chargement"
                }

            _isLoading.value = false
        }
    }

    private fun loadQuizHistory(courseId: String, chapterId: String) {
        viewModelScope.launch {
            quizRepository.getQuizHistory(courseId, chapterId)
                .onSuccess { historyList ->
                    _history.value = historyList
                }
                .onFailure {
                    println("❌ Erreur chargement historique: ${it.message}")
                }
        }
    }

    fun toggleFavorite() {
        val currentChapter = _chapter.value ?: return
        val nextState = !currentChapter.isFavorite

        viewModelScope.launch {

            progressRepository.toggleChapterFavorite(
                courseId = currentCourseId,
                chapter = currentChapter,
                isFavorite = nextState
            ).onSuccess {

                _chapter.value = currentChapter.copy(isFavorite = nextState)
                println("✅ Favori mis à jour : ${currentChapter.title} -> $nextState")
            }.onFailure { e ->
                println("❌ Erreur toggle favori : ${e.message}")
            }
        }
    }

    private fun startProgressListener(
        courseId: String,
        chapterId: String,
        baseChapter: Chapter
    ) {
        viewModelScope.launch {
            progressRepository.observeChapterProgress(courseId, chapterId)
                .collect { progress ->
                    val canUnlock = progress.isCoursRead || progress.isFdrRead

                    _chapter.value = baseChapter.copy(
                        isCoursRead = progress.isCoursRead,
                        isFdrRead = progress.isFdrRead,
                        isVideoWatched = progress.isVideoWatched,
                        isQuizCompleted = progress.isQuizCompleted,
                        isQuizUnlocked = canUnlock
                    )


                    if (progress.isQuizCompleted) {
                        loadQuizHistory(courseId, chapterId)
                    }
                }
        }
    }

    fun refresh() {
        if (currentCourseId.isNotEmpty() && currentChapterId.isNotEmpty()) {
            loadChapter(currentCourseId, currentChapterId)
        }
    }
}