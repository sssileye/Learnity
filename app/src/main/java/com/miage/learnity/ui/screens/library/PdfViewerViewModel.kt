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

enum class ContentType {
    COURS, FDR, VIDEO
}

class PdfViewerViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    private val _contentType = MutableStateFlow<ContentType>(ContentType.COURS)
    val contentType: StateFlow<ContentType> = _contentType.asStateFlow()

    private val _contentUrl = MutableStateFlow<String?>(null)
    val contentUrl: StateFlow<String?> = _contentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isMarkedAsRead = MutableStateFlow(false)
    val isMarkedAsRead: StateFlow<Boolean> = _isMarkedAsRead.asStateFlow()

    private var currentCourseId: String = ""
    private var currentChapterId: String = ""

    fun loadContent(courseId: String, chapterId: String, type: ContentType) {
        currentCourseId = courseId
        currentChapterId = chapterId

        viewModelScope.launch {
            _isLoading.value = true
            _contentType.value = type

            // Charger le chapitre
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    _chapter.value = chapter

                    // Déterminer l'URL
                    _contentUrl.value = when (type) {
                        ContentType.COURS -> chapter.coursUrl
                        ContentType.FDR -> chapter.fdrUrl
                        ContentType.VIDEO -> chapter.videoUrl
                    }

                    // Charger la progression
                    progressRepository.getChapterProgress(courseId, chapterId)
                        .onSuccess { progress ->
                            _isMarkedAsRead.value = when (type) {
                                ContentType.VIDEO -> progress.isVideoWatched
                                else -> progress.isContentRead
                            }
                        }
                }

            _isLoading.value = false
        }
    }

    fun markAsReadOrWatched() {
        viewModelScope.launch {
            when (_contentType.value) {
                ContentType.VIDEO -> {
                    progressRepository.markVideoAsWatched(currentCourseId, currentChapterId)
                        .onSuccess {
                            _isMarkedAsRead.value = true
                        }
                }
                else -> {
                    progressRepository.markContentAsRead(currentCourseId, currentChapterId)
                        .onSuccess {
                            _isMarkedAsRead.value = true
                        }
                }
            }
        }
    }
}

