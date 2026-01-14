package com.miage.learnity.data

import com.google.android.gms.dynamite.DynamiteModule

data class Course(
    val id: String,
    val title: String,
    val module: String,
    val chapters: List<Chapter>,
    val iconRes: Int
)

data class Chapter(
    val id: String,
    val title: String,
    val iconRes: Int,
    val videoUrl: String? = null, // URL YouTube
    val contentUrl: String? = null, // URL du contenu à lire
    val isVideoWatched: Boolean = false,
    val isContentRead: Boolean = false,
    val isQuizCompleted: Boolean = false
){
    val isCompleted: Boolean
        get() = isVideoWatched && isContentRead && isQuizCompleted

    val isQuizUnlocked: Boolean
        get() = isVideoWatched && isContentRead
}
data class CourseProgress(
    val completedChapters: Int,
    val totalChapters: Int
) {
    val percentage: Float
        get() = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f

    val isAllCompleted: Boolean
        get() = completedChapters == totalChapters
}

