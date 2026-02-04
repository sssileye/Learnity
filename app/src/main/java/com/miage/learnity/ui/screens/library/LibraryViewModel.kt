package com.miage.learnity.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miage.learnity.data.Course
import com.miage.learnity.repository.CourseRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Définit les options de tri disponibles dans la bibliothèque
 */
enum class CourseSortOrder {
    ALPHABETICAL,
    FAVORITES,
    PROGRESSION // Note : Nécessitera le calcul de complétion globale par UE
}

class LibraryViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Liste brute venant du Repository
    private val _rawCourses = MutableStateFlow<List<Course>>(emptyList())

    // État du tri actuel
    private val _sortOrder = MutableStateFlow(CourseSortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<CourseSortOrder> = _sortOrder.asStateFlow()

    // ⭐ Liste finale TRIÉE (observée par l'UI)
    // On combine la liste brute et l'ordre de tri pour réagir dynamiquement
    val courses: StateFlow<List<Course>> = combine(_rawCourses, _sortOrder) { rawList, order ->
        applySort(rawList, order)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Reste actif 5s après que l'UI est fermée
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        val userId = auth.currentUser?.uid
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // On passe désormais le userId pour récupérer le statut "isFavorite"
            courseRepository.getAllCourses(userId)
                .onSuccess { courses ->
                    _rawCourses.value = courses
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erreur de chargement"
                }

            _isLoading.value = false
        }
    }

    /**
     * Change l'ordre de tri
     */
    fun updateSortOrder(newOrder: CourseSortOrder) {
        _sortOrder.value = newOrder
    }

    /**
     * Gère l'action de mettre en favori depuis la liste
     */
    fun toggleFavorite(courseId: String, currentIsFavorite: Boolean) {
        // ⭐ On récupère d'abord l'objet Course complet dans la liste actuelle
        val courseToUpdate = _rawCourses.value.find { it.id == courseId } ?: return
        val nextState = !currentIsFavorite

        viewModelScope.launch {
            // ✅ On envoie l'objet entier au repository pour copier le titre
            progressRepository.toggleCourseFavorite(
                course = courseToUpdate,
                isFavorite = nextState
            ).onSuccess {
                // Mise à jour locale immédiate pour une UI réactive
                _rawCourses.value = _rawCourses.value.map {
                    if (it.id == courseId) it.copy(isFavorite = nextState) else it
                }
                Log.d("DEBUG_FAV", "Matière '${courseToUpdate.title}' basculée vers: $nextState")
            }.onFailure { e ->
                Log.e("DEBUG_FAV", "Erreur toggle favori : ${e.message}")
            }
        }
    }

    /**
     * Logique de tri pure
     */
    private fun applySort(list: List<Course>, order: CourseSortOrder): List<Course> {
        return when (order) {
            CourseSortOrder.ALPHABETICAL -> list.sortedBy { it.title }
            CourseSortOrder.FAVORITES -> list.sortedWith(
                compareByDescending<Course> { it.isFavorite }.thenBy { it.title }
            )
            CourseSortOrder.PROGRESSION -> {
                // Pour l'instant on trie par ID ou titre en attendant
                // l'intégration des pourcentages de complétion réels
                list.sortedBy { it.id }
            }
        }
    }

    fun refresh() {
        loadCourses()
    }
}