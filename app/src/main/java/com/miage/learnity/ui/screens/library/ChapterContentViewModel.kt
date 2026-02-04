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
        val userId = auth.currentUser?.uid // ⭐ Récupération du userId pour les favoris

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // 1. Charger les données du chapitre (avec userId pour le favori)
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { baseChapter ->
                    // 2. Lancer l'écouteur de progression temps réel
                    startProgressListener(courseId, chapterId, baseChapter)
                    // 3. Charger l'historique des scores
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

    /**
     * Alterne le favori du chapitre actuel
     */
    fun toggleFavorite() {
        val currentChapter = _chapter.value ?: return
        val nextState = !currentChapter.isFavorite

        viewModelScope.launch {
            progressRepository.toggleChapterFavorite(currentCourseId, currentChapterId, nextState)
                .onSuccess {
                    _chapter.value = currentChapter.copy(isFavorite = nextState)
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
                    // Un quiz est débloqué si le cours OU la FDR est lu
                    val canUnlock = progress.isCoursRead || progress.isFdrRead

                    _chapter.value = baseChapter.copy(
                        isCoursRead = progress.isCoursRead,
                        isFdrRead = progress.isFdrRead,
                        isVideoWatched = progress.isVideoWatched,
                        isQuizCompleted = progress.isQuizCompleted,
                        bestScore = progress.bestScore,
                        isFavorite = progress.isFavorite, // ⭐ On récupère le favori du listener
                        isQuizUnlocked = canUnlock
                    )

                    // Recharger l'historique automatiquement si le score change
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