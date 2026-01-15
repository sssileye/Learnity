package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.data.CourseProgress
import com.miage.learnity.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de détail d'un cours (liste des chapitres)
 *
 * Version actuelle : utilise MockData
 * TODO: Remplacer par CourseRepository quand backend prêt
 */
class CourseDetailViewModel : ViewModel() {

    // État : Cours actuel
    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    // État : Liste des chapitres (simulant la sous-collection Firestore)
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    // État : Chargement en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État : Erreur
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Charge un cours et ses chapitres
     * @param courseId ID du cours à charger
     */
    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Simuler un délai réseau
                delay(500)

                // ⭐ VERSION MOCK (pour développement front-end)

                // Charger le cours
                val course = MockData.getCourse(courseId)
                if (course != null) {
                    _course.value = course

                    // Charger les chapitres (simule sous-collection Firestore)
                    val chapters = MockData.getChaptersForCourse(courseId)
                    _chapters.value = chapters
                } else {
                    _error.value = "Cours non trouvé"
                }

                /* ⭐ VERSION FIREBASE (décommenter quand backend prêt)
                // Charger le cours
                courseRepository.getCourse(courseId)
                    .onSuccess { course ->
                        _course.value = course
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur de chargement du cours"
                    }

                // Charger les chapitres (sous-collection)
                courseRepository.getChapters(courseId)
                    .onSuccess { chapters ->
                        _chapters.value = chapters
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur de chargement des chapitres"
                    }
                */

            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calcule la progression du cours
     * @return CourseProgress avec le nombre de chapitres complétés
     */
    fun getCourseProgress(): CourseProgress {
        val chapters = _chapters.value
        return CourseProgress(
            completedChapters = chapters.count { it.isCompleted },
            totalChapters = chapters.size
        )
    }

    /**
     * Rafraîchir les données du cours
     */
    fun refresh(courseId: String) {
        loadCourse(courseId)
    }
}