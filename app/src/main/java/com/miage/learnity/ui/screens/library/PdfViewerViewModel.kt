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

        viewModelScope.launch {
            _isLoading.value = true
            _contentType.value = type

            // Charger le chapitre
            courseRepository.getChapter(courseId, chapterId)
                .onSuccess { chapter ->
                    _chapter.value = chapter

                    // Déterminer l'URL selon le type
                    _contentUrl.value = when (type) {
                        UserProgressRepository.ContentType.COURS -> chapter.coursUrl
                        UserProgressRepository.ContentType.FDR -> chapter.fdrUrl
                        UserProgressRepository.ContentType.VIDEO -> chapter.videoUrl
                    }

                    // Charger la progression
                    progressRepository.getChapterProgress(courseId, chapterId)
                        .onSuccess { progress ->
                            _isMarkedAsRead.value = when (type) {
                                UserProgressRepository.ContentType.VIDEO -> progress.isVideoWatched
                                UserProgressRepository.ContentType.COURS -> progress.isCoursRead
                                UserProgressRepository.ContentType.FDR -> progress.isFdrRead
                            }
                        }
                }

            _isLoading.value = false
        }
    }

    fun markAsReadOrWatched() {
        viewModelScope.launch {
            when (_contentType.value) {
                UserProgressRepository.ContentType.VIDEO -> {
                    progressRepository.markVideoAsWatched(currentCourseId, currentChapterId)
                        .onSuccess {
                            _isMarkedAsRead.value = true
                            println("✅ Vidéo marquée comme vue")
                        }
                        .onFailure {
                            println("❌ Erreur marquage vidéo: ${it.message}")
                        }
                }
                UserProgressRepository.ContentType.COURS,
                UserProgressRepository.ContentType.FDR -> {
                    // ✅ Passer le type de contenu
                    progressRepository.markContentAsRead(
                        currentCourseId,
                        currentChapterId,
                        _contentType.value
                    ).onSuccess {
                        _isMarkedAsRead.value = true
                        println("✅ Contenu marqué comme lu (${_contentType.value})")
                    }.onFailure {
                        println("❌ Erreur marquage contenu: ${it.message}")
                    }
                }
            }
        }
    }
}