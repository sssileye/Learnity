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

    // ⭐ NOUVEAU : État pour le déblocage de l'examen blanc
    private val _isExamUnlocked = MutableStateFlow(false)
    val isExamUnlocked: StateFlow<Boolean> = _isExamUnlocked.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var baseChapters: List<Chapter> = emptyList()

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            courseRepository.getCourse(courseId).onSuccess { _course.value = it }
                .onFailure { _error.value = it.message ?: "Erreur chargement cours" }

            courseRepository.getChapters(courseId).onSuccess { chapters ->
                baseChapters = chapters
                _chapters.value = chapters
                startProgressListener(courseId)
            }.onFailure { _error.value = it.message ?: "Erreur chargement chapitres" }

            _isLoading.value = false
        }
    }

    private fun startProgressListener(courseId: String) {
        viewModelScope.launch {
            progressRepository.observeCourseProgress(courseId)
                .collect { progressMap ->
                    val updatedChapters = baseChapters.map { chapter ->
                        val progress = progressMap[chapter.chapterId]

                        // ✅ Extraction des flags de progression
                        val isCoursRead = progress?.isCoursRead ?: false
                        val isFdrRead = progress?.isFdrRead ?: false

                        chapter.copy(
                            isCoursRead = isCoursRead,
                            isFdrRead = isFdrRead,
                            isVideoWatched = progress?.isVideoWatched ?: false,
                            isQuizCompleted = progress?.isQuizCompleted ?: false,
                            // ✅ Un quiz de chapitre est débloqué si le cours OU la FDR est lu(e)
                            isQuizUnlocked = isCoursRead || isFdrRead
                        )
                    }

                    _chapters.value = updatedChapters

                    // ⭐ LOGIQUE DÉBLOCAGE EXAMEN BLANC :
                    // On vérifie que TOUS les chapitres ont leur quiz complété
                    _isExamUnlocked.value = updatedChapters.isNotEmpty() &&
                            updatedChapters.all { it.isQuizCompleted }

                    println("🔥 CourseDetailVM - Exam Unlocked: ${_isExamUnlocked.value}")
                }
        }
    }

    fun getCourseProgress(): CourseProgress {
        val chapters = _chapters.value
        // On utilise la logique de "Contenu complété" pour la barre de progression
        val contentCompleted = chapters.count { it.isContentCompleted }

        return CourseProgress(
            completedChapters = contentCompleted,
            totalChapters = chapters.size
        )
    }

    fun refresh(courseId: String) { loadCourse(courseId) }
}