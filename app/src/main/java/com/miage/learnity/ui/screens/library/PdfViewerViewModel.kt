package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.ChapterProgressData // ✅ Import de la Data Class
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfViewerViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    private val _contentType = MutableStateFlow<UserProgressRepository.ContentType>(
        UserProgressRepository.ContentType.COURS
    )
    val contentType: StateFlow<UserProgressRepository.ContentType> = _contentType.asStateFlow()

    private val _contentUrl = MutableStateFlow<String?>(null)
    val contentUrl: StateFlow<String?> = _contentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isMarkedAsRead = MutableStateFlow(false)
    val isMarkedAsRead: StateFlow<Boolean> = _isMarkedAsRead.asStateFlow()

    private var currentCourseId: String = ""
    private var currentChapterId: String = ""

    fun loadContent(courseId: String, chapterId: String, type: UserProgressRepository.ContentType) {
        currentCourseId = courseId
        currentChapterId = chapterId
        _contentType.value = type

        viewModelScope.launch {
            _isLoading.value = true

            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    _chapter.value = chapter
                    _contentUrl.value = if (type == UserProgressRepository.ContentType.FDR) {
                        chapter.fdr
                    } else {
                        chapter.cours
                    }

                    val progressResult = progressRepository.getChapterProgress(courseId, chapterId)

                    val progress = progressResult.getOrNull() ?: ChapterProgressData()

                    _isMarkedAsRead.value = when (type) {
                        UserProgressRepository.ContentType.FDR -> progress.isFdrRead
                        else -> progress.isCoursRead
                    }
                }
                .onFailure {
                    println("❌ PdfViewerVM - Erreur chargement chapitre : ${it.message}")
                }

            _isLoading.value = false
        }
    }

    fun markAsReadOrWatched() {
        if (currentCourseId.isEmpty() || currentChapterId.isEmpty()) return

        viewModelScope.launch {
            progressRepository.markContentAsCompleted(
                courseId = currentCourseId,
                chapterId = currentChapterId,
                contentType = _contentType.value
            ).onSuccess {
                _isMarkedAsRead.value = true
                println("✅ PdfViewerVM - Progression sauvegardée pour ${_contentType.value}")
            }.onFailure {
                println("❌ PdfViewerVM - Erreur sauvegarde : ${it.message}")
            }
        }
    }
}