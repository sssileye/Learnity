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

    private var baseChapters: List<Chapter> = emptyList()
    private var currentCourseId: String = ""

    fun loadCourse(courseId: String) {
        currentCourseId = courseId

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
                    baseChapters = chapters
                    _chapters.value = chapters

                    // 🔥 DÉMARRER LE LISTENER TEMPS RÉEL
                    startProgressListener(courseId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erreur de chargement des chapitres"
                }

            _isLoading.value = false
        }
    }

    /**
     * 🔥 Observe la progression du cours en temps réel
     */
    private fun startProgressListener(courseId: String) {
        viewModelScope.launch {
            progressRepository.observeCourseProgress(courseId)
                .collect { progressMap ->
                    // Fusionner les chapitres avec la progression
                    _chapters.value = baseChapters.map { chapter ->
                        val progress = progressMap[chapter.chapterId]
                        chapter.copy(
                            isCoursRead = progress?.isCoursRead ?: false,
                            isFdrRead = progress?.isFdrRead ?: false,
                            isVideoWatched = progress?.isVideoWatched ?: false,
                            isQuizCompleted = progress?.isQuizCompleted ?: false
                        )
                    }
                    println("🔥 CourseDetailVM - Progress updated from Firebase (${progressMap.size} chapters)")
                }
        }
    }


    fun getCourseProgress(): CourseProgress {
        val chapters = _chapters.value

        // Option 1 : Progression stricte (quiz obligatoire)
        val strictCompleted = chapters.count { it.isCompleted }

        // Option 2 : Progression du contenu (sans quiz)
        val contentCompleted = chapters.count { it.isContentCompleted }

        // Option 3 : Progression moyenne en pourcentage
        val avgProgress = if (chapters.isNotEmpty()) {
            chapters.sumOf { it.progressPercentage.toDouble() }.toFloat() / chapters.size
        } else {
            0f
        }

        // ✅ Utilise la progression du contenu (plus encourageante)
        return CourseProgress(
            completedChapters = contentCompleted,  // ✅ Sans quiz obligatoire
            totalChapters = chapters.size
        )
    }

    fun refresh(courseId: String) {
        loadCourse(courseId)
    }
}