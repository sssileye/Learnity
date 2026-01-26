package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChapterContentViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

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

            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    // On ne met pas à jour tout de suite, on laisse le listener le faire
                    startProgressListener(courseId, chapterId, chapter)
                }
                .onFailure {
                    _error.value = it.message ?: "Erreur lors du chargement"
                }

            _isLoading.value = false
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
                    // ⭐ LOGIQUE DE DÉBLOCAGE : AU MOINS UN DES DEUX
                    val canUnlock = progress.isCoursRead || progress.isFdrRead

                    _chapter.value = baseChapter.copy(
                        isCoursRead = progress.isCoursRead,
                        isFdrRead = progress.isFdrRead,
                        isVideoWatched = progress.isVideoWatched,
                        isQuizCompleted = progress.isQuizCompleted,
                        // ✅ Le quiz se débloque si le cours OU la FDR est lu(e)
                        isQuizUnlocked = canUnlock
                    )

                    println("🔥 Progress Update - Cours: ${progress.isCoursRead}, FDR: ${progress.isFdrRead} -> Unlocked: $canUnlock")
                }
        }
    }

    fun refresh() {
        if (currentCourseId.isNotEmpty() && currentChapterId.isNotEmpty()) {
            loadChapter(currentCourseId, currentChapterId)
        }
    }
}