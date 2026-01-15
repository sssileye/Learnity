package com.miage.learnity.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Type de contenu à afficher
 */
enum class ContentType {
    COURS,      // PDF cours complet
    FDR,        // Fiche de révision
    VIDEO       // Vidéo YouTube
}

/**
 * ViewModel pour le viewer PDF/Vidéo
 * Gère l'affichage du contenu d'un chapitre
 */
class PdfViewerViewModel : ViewModel() {

    // État : Chapitre actuel
    private val _chapter = MutableStateFlow<Chapter?>(null)
    val chapter: StateFlow<Chapter?> = _chapter.asStateFlow()

    // État : Type de contenu affiché
    private val _contentType = MutableStateFlow<ContentType>(ContentType.COURS)
    val contentType: StateFlow<ContentType> = _contentType.asStateFlow()

    // État : URL du contenu
    private val _contentUrl = MutableStateFlow<String?>(null)
    val contentUrl: StateFlow<String?> = _contentUrl.asStateFlow()

    // État : Chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État : Contenu marqué comme lu/vu
    private val _isMarkedAsRead = MutableStateFlow(false)
    val isMarkedAsRead: StateFlow<Boolean> = _isMarkedAsRead.asStateFlow()

    /**
     * Charge un chapitre et son contenu
     * @param courseId ID du cours
     * @param chapterId ID du chapitre
     * @param type Type de contenu à afficher (COURS, FDR, VIDEO)
     */
    fun loadContent(courseId: String, chapterId: String, type: ContentType) {
        viewModelScope.launch {
            _isLoading.value = true
            _contentType.value = type

            try {
                // Simuler délai de chargement
                delay(300)

                // ⭐ VERSION MOCK
                val chapter = MockData.getChapter(courseId, chapterId)

                if (chapter != null) {
                    _chapter.value = chapter

                    // Déterminer l'URL selon le type
                    _contentUrl.value = when (type) {
                        ContentType.COURS -> chapter.coursUrl
                        ContentType.FDR -> chapter.fdrUrl
                        ContentType.VIDEO -> chapter.videoUrl
                    }

                    // Vérifier si déjà marqué comme lu/vu
                    _isMarkedAsRead.value = when (type) {
                        ContentType.VIDEO -> chapter.isVideoWatched
                        else -> chapter.isContentRead
                    }
                }

                /* ⭐ VERSION FIREBASE (pour plus tard)
                courseRepository.getChapter(courseId, chapterId)
                    .onSuccess { chapter ->
                        _chapter.value = chapter
                        // Récupérer l'URL depuis Firebase Storage
                        val url = when (type) {
                            ContentType.COURS -> chapter.coursUrl
                            ContentType.FDR -> chapter.fdrUrl
                            ContentType.VIDEO -> chapter.videoUrl
                        }
                        _contentUrl.value = url
                    }
                */

            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Marque le contenu comme lu/vu
     */
    fun markAsReadOrWatched() {
        _isMarkedAsRead.value = true

        // TODO: Mettre à jour dans Firebase
        /*
        viewModelScope.launch {
            when (_contentType.value) {
                ContentType.VIDEO -> {
                    userProgressRepository.markVideoAsWatched(...)
                }
                else -> {
                    userProgressRepository.markContentAsRead(...)
                }
            }
        }
        */
    }
}