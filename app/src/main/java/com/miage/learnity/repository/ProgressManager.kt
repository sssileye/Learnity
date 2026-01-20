package com.miage.learnity.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestionnaire centralisé de la progression utilisateur
 * Notifie tous les écrans quand la progression change
 */
object ProgressManager {

    // Event pour notifier les changements
    private val _progressUpdated = MutableStateFlow(0L)
    val progressUpdated: StateFlow<Long> = _progressUpdated.asStateFlow()

    // Détails du dernier changement
    data class ProgressChange(
        val courseId: String,
        val chapterId: String,
        val type: ProgressType
    )

    enum class ProgressType {
        CONTENT_READ,
        VIDEO_WATCHED,
        QUIZ_COMPLETED
    }

    private val _lastChange = MutableStateFlow<ProgressChange?>(null)
    val lastChange: StateFlow<ProgressChange?> = _lastChange.asStateFlow()

    /**
     * Notifie qu'un changement de progression a eu lieu
     */
    fun notifyProgressChanged(
        courseId: String,
        chapterId: String,
        type: ProgressType
    ) {
        _lastChange.value = ProgressChange(courseId, chapterId, type)
        _progressUpdated.value = System.currentTimeMillis()
        println("✅ ProgressManager - Notification: ${type.name} pour $courseId/$chapterId")
    }

    /**
     * Reset la notification
     */
    fun clearLastChange() {
        _lastChange.value = null
    }
}