package com.miage.learnity.ui.screens.library


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Course
import com.miage.learnity.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Bibliothèque (liste des cours)
 *
 * Version actuelle : utilise MockData
 * TODO: Remplacer par CourseRepository quand backend prêt
 */
class LibraryViewModel : ViewModel() {

    // État : Liste des cours
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    // État : Chargement en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État : Erreur
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Charger les cours au démarrage
        loadCourses()
    }

    /**
     * Charge la liste des cours
     * Version actuelle : depuis MockData
     * TODO: Remplacer par appel Repository
     */
    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Simuler un délai réseau pour test
                delay(500)

                // ⭐ VERSION MOCK (pour développement front-end)
                _courses.value = MockData.sampleCourses

                /* ⭐ VERSION FIREBASE (décommenter quand backend prêt)
                courseRepository.getAllCourses()
                    .onSuccess { courses ->
                        _courses.value = courses
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Erreur de chargement"
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
     * Rafraîchir la liste des cours
     */
    fun refresh() {
        loadCourses()
    }
}

