package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository(),
    private val quizRepository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _examHistory = MutableStateFlow<List<QuizHistory>>(emptyList())
    val examHistory: StateFlow<List<QuizHistory>> = _examHistory.asStateFlow()

    private val _isExamUnlocked = MutableStateFlow(false)
    val isExamUnlocked: StateFlow<Boolean> = _isExamUnlocked.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val courseProgress: StateFlow<CourseProgress> = _chapters.map { list ->
        val completed = list.count { it.isQuizCompleted }
        CourseProgress(completedChapters = completed, totalChapters = list.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourseProgress(0, 0))

    private var baseChapters: List<Chapter> = emptyList()

    fun loadCourse(courseId: String) {
        val userId = auth.currentUser?.uid
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            courseRepository.getCourse(courseId, userId).onSuccess {
                _course.value = it
            }.onFailure {
                _error.value = "Erreur cours: ${it.message}"
            }

            courseRepository.getChapters(courseId, userId).onSuccess { chapters ->
                baseChapters = chapters
                _chapters.value = chapters
                startProgressListener(courseId)
            }.onFailure {
                _error.value = "Erreur chapitres: ${it.message}"
            }

            loadExamHistory(courseId)
            _isLoading.value = false
        }
    }
    /**
     * Alterne le favori de l'UE actuelle
     */
    fun toggleCourseFavorite() {
        val currentCourse = _course.value ?: return
        val nextState = !currentCourse.isFavorite

        viewModelScope.launch {
            progressRepository.toggleCourseFavorite(currentCourse.id, nextState).onSuccess {
                _course.value = currentCourse.copy(isFavorite = nextState)
            }
        }
    }

    /**
     * Alterne le favori d'un chapitre spécifique
     */
    fun toggleChapterFavorite(chapterId: String, nextState: Boolean) {
        val courseId = _course.value?.id ?: return

        viewModelScope.launch {
            progressRepository.toggleChapterFavorite(courseId, chapterId, nextState).onSuccess {
                _chapters.value = _chapters.value.map {
                    if (it.chapterId == chapterId) it.copy(isFavorite = nextState) else it
                }
                baseChapters = baseChapters.map {
                    if (it.chapterId == chapterId) it.copy(isFavorite = nextState) else it
                }
            }
        }
    }
    private fun loadExamHistory(courseId: String) {
        viewModelScope.launch {
            quizRepository.getQuizHistory(courseId, "ALL_CHAPTERS").onSuccess { history ->
                _examHistory.value = history
            }.onFailure {
                _examHistory.value = emptyList()
            }
        }
    }

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
                            isFavorite = progress?.isFavorite ?: chapter.isFavorite,
                            isQuizUnlocked = isCoursRead || isFdrRead
                        )
                    }

                    _chapters.value = updatedChapters
                    _isExamUnlocked.value = updatedChapters.isNotEmpty() &&
                            updatedChapters.all { it.isQuizCompleted }
                }
        }
    }
    enum class ChapterSortOrder {
        ORIGINAL,
        FAVORITES,
        INCOMPLETE_FIRST
    }
    private val _sortOrder = MutableStateFlow(ChapterSortOrder.ORIGINAL)
    val sortOrder: StateFlow<ChapterSortOrder> = _sortOrder.asStateFlow()

    val sortedChapters: StateFlow<List<Chapter>> = combine(_chapters, _sortOrder) { list, order ->
        when (order) {
            ChapterSortOrder.ORIGINAL -> list.sortedBy { it.order }
            ChapterSortOrder.FAVORITES -> list.sortedWith(compareByDescending<Chapter> { it.isFavorite }.thenBy { it.order })
            ChapterSortOrder.INCOMPLETE_FIRST -> list.sortedWith(compareBy<Chapter> { it.isQuizCompleted }.thenBy { it.order })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSortOrder(order: ChapterSortOrder) {
        _sortOrder.value = order
    }

    fun refresh(courseId: String) { loadCourse(courseId) }
}