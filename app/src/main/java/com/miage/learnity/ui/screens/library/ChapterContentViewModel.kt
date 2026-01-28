package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.QuizHistory // ✅ Import de ta DataClass
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.QuizRepository // ✅ Nouveau Repository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterContentViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository(),
    private val quizRepository: QuizRepository = QuizRepository() // ✅ Ajouté
) : ViewModel() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    // ⭐ NOUVEAU : État pour l'historique du tableau
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

            // 1. Charger les données du chapitre
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    startProgressListener(courseId, chapterId, chapter)
                    // 2. Charger l'historique pour le tableau
                    loadQuizHistory(courseId, chapterId)
                }
                .onFailure {
                    _error.value = it.message ?: "Erreur lors du chargement"
                }

            _isLoading.value = false
        }
    }

    // ⭐ FONCTION POUR CHARGER L'HISTORIQUE
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

                    // ✅ Optionnel : Recharger l'historique si un nouveau quiz est complété
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