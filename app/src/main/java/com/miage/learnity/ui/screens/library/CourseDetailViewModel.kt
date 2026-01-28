package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository(),
    private val quizRepository: QuizRepository = QuizRepository() // ✅ Ajouté pour l'historique
) : ViewModel() {

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    // ⭐ État pour l'historique des Examens Blancs
    private val _examHistory = MutableStateFlow<List<QuizHistory>>(emptyList())
    val examHistory: StateFlow<List<QuizHistory>> = _examHistory.asStateFlow()

    private val _isExamUnlocked = MutableStateFlow(false)
    val isExamUnlocked: StateFlow<Boolean> = _isExamUnlocked.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Calcul de la progression en temps réel
    val courseProgress: StateFlow<CourseProgress> = _chapters.map { list ->
        val completed = list.count { it.isQuizCompleted }
        CourseProgress(completedChapters = completed, totalChapters = list.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourseProgress(0, 0))

    private var baseChapters: List<Chapter> = emptyList()

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // 1. Charger les détails du cours
            courseRepository.getCourse(courseId).onSuccess { _course.value = it }
                .onFailure { _error.value = it.message ?: "Erreur chargement cours" }

            // 2. Charger les chapitres
            courseRepository.getChapters(courseId).onSuccess { chapters ->
                baseChapters = chapters
                _chapters.value = chapters
                startProgressListener(courseId)
            }.onFailure { _error.value = it.message ?: "Erreur chargement chapitres" }

            // 3. Charger l'historique des examens blancs de cette UE
            loadExamHistory(courseId)

            _isLoading.value = false
        }
    }

    /**
     * ✅ RÉCUPÉRATION DE L'HISTORIQUE DES EXAMENS BLANCS
     * On filtre sur "ALL_CHAPTERS" pour isoler les tentatives d'UE
     */
    private fun loadExamHistory(courseId: String) {
        viewModelScope.launch {
            quizRepository.getQuizHistory(courseId, "ALL_CHAPTERS").onSuccess { history ->
                _examHistory.value = history
            }.onFailure {
                _examHistory.value = emptyList()
            }
        }
    }

    /**
     * ✅ ÉCOUTEUR TEMPS RÉEL : Met à jour l'UI dès que l'utilisateur
     * finit un support ou un Quiz.
     */
    private fun startProgressListener(courseId: String) {
        viewModelScope.launch {
            progressRepository.observeCourseProgress(courseId)
                .collect { progressMap ->
                    val updatedChapters = baseChapters.map { chapter ->
                        val progress = progressMap[chapter.chapterId]

                        val isCoursRead = progress?.isCoursRead ?: false
                        val isFdrRead = progress?.isFdrRead ?: false
                        val isQuizDone = progress?.isQuizCompleted ?: false

                        chapter.copy(
                            isCoursRead = isCoursRead,
                            isFdrRead = isFdrRead,
                            isVideoWatched = progress?.isVideoWatched ?: false,
                            isQuizCompleted = isQuizDone,
                            bestScore = progress?.bestScore ?: 0,
                            // Déblocage Quiz : Si au moins un support est lu
                            isQuizUnlocked = isCoursRead || isFdrRead
                        )
                    }

                    _chapters.value = updatedChapters

                    // ✅ Déblocage de l'examen si tous les quiz de chapitres sont faits
                    _isExamUnlocked.value = updatedChapters.isNotEmpty() &&
                            updatedChapters.all { it.isQuizCompleted }
                }
        }
    }

    fun refresh(courseId: String) { loadCourse(courseId) }
}