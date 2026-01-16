package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Charger le cours
            courseRepository.getCourse(courseId)
                .onSuccess { course ->
                    _course.value = course
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erreur de chargement du cours"
                }

            // Charger les chapitres
            courseRepository.getChapters(courseId)
                .onSuccess { chapters ->
                    // Charger la progression pour chaque chapitre
                    val chapterIds = chapters.map { it.chapterId }
                    progressRepository.getCourseProgress(courseId, chapterIds)
                        .onSuccess { progressMap ->
                            // Fusionner chapitres + progression
                            _chapters.value = chapters.map { chapter ->
                                val progress = progressMap[chapter.chapterId]
                                chapter.copy(
                                    isContentRead = progress?.isContentRead ?: false,
                                    isVideoWatched = progress?.isVideoWatched ?: false,
                                    isQuizCompleted = progress?.isQuizCompleted ?: false
                                )
                            }
                        }
                        .onFailure {
                            // Afficher chapitres sans progression
                            _chapters.value = chapters
                        }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erreur de chargement des chapitres"
                }

            _isLoading.value = false
        }
    }

    fun getCourseProgress(): CourseProgress {
        val chapters = _chapters.value
        return CourseProgress(
            completedChapters = chapters.count { it.isCompleted },
            totalChapters = chapters.size
        )
    }

    fun refresh(courseId: String) {
        loadCourse(courseId)
    }
}

