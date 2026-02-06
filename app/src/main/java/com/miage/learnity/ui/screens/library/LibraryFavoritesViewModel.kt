package com.miage.learnity.ui.screens.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course
import com.miage.learnity.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryFavoritesViewModel(
    private val repository: CourseRepository = CourseRepository()
) : ViewModel() {


    private val _favoriteChapters = MutableStateFlow<List<Chapter>>(emptyList())
    val favoriteChapters = _favoriteChapters.asStateFlow()


    private val _favoriteCourses = MutableStateFlow<List<Course>>(emptyList())
    val favoriteCourses = _favoriteCourses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("DEBUG_FAV", "🚀 Chargement de la bibliothèque pour: $userId")


            repository.getFavoriteChapters(userId)
                .onSuccess { list ->
                    Log.d("DEBUG_FAV", "✅ Chapitres trouvés: ${list.size}")
                    _favoriteChapters.value = list
                }
                .onFailure { error ->
                    Log.e("DEBUG_FAV", "❌ Erreur Chapitres: ${error.message}")
                }

            repository.getFavoriteCourses(userId)
                .onSuccess { list ->
                    Log.d("DEBUG_FAV", "✅ Matières trouvées: ${list.size}")
                    _favoriteCourses.value = list
                }
                .onFailure { error ->
                    Log.e("DEBUG_FAV", "❌ Erreur Matières: ${error.message}")
                }

            _isLoading.value = false
        }
    }
}