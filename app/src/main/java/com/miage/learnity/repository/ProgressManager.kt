package com.miage.learnity.repository

import com.miage.learnity.model.PointsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ProgressManager {

    private val _progressUpdated = MutableStateFlow(0L)
    val progressUpdated: StateFlow<Long> = _progressUpdated.asStateFlow()

    data class ProgressChange(
        val courseId: String,
        val chapterId: String?,
        val type: ProgressType,
        val quizType: PointsManager.QuizType? = null // Ajout du type
    )

    enum class ProgressType {
        CONTENT_READ,
        VIDEO_WATCHED,
        QUIZ_COMPLETED
    }

    private val _lastChange = MutableStateFlow<ProgressChange?>(null)
    val lastChange: StateFlow<ProgressChange?> = _lastChange.asStateFlow()

    fun notifyProgressChanged(
        courseId: String,
        chapterId: String?,
        type: ProgressType,
        quizType: PointsManager.QuizType? = null
    ) {
        _lastChange.value = ProgressChange(courseId, chapterId, type, quizType)
        _progressUpdated.value = System.currentTimeMillis()
    }
}