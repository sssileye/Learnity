package com.miage.learnity.data

// ============================================
// COURSE (Cours - Sans les chapitres)
// ============================================
data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconRes: Int? = null                   // Icon pour l'UI
)
// ⚠️ PLUS de chapters: List<Chapter> !
// Les chapitres sont dans une sous-collection Firestore

// ============================================
// CHAPTER (Chapitre avec contenu flexible)
// ============================================
data class Chapter(
    val chapterId: String = "",
    val title: String = "",
    val order: Int = 0,

    // === CONTENU PÉDAGOGIQUE ===
    val coursUrl: String? = null,          // URL PDF cours complet
    val fdrUrl: String? = null,            // URL Fiche de Révision
    val videoUrl: String? = null,          // URL YouTube

    // === MÉTADONNÉES ===
    val pageCount: Int = 0,
    val estimatedReadTime: Int = 0,        // minutes
    val videoDuration: Int = 0,            // minutes

    // === QUIZ ===
    val quizId: String? = null,

    // === ÉTATS DE PROGRESSION ===
    val isVideoWatched: Boolean = false,
    val isContentRead: Boolean = false,
    val isQuizCompleted: Boolean = false
) {
    val isCompleted: Boolean
        get() {
            val videoRequired = videoUrl != null
            val contentRequired = coursUrl != null || fdrUrl != null

            val videoOk = if (videoRequired) isVideoWatched else true
            val contentOk = if (contentRequired) isContentRead else true

            return videoOk && contentOk && isQuizCompleted
        }

    val isQuizUnlocked: Boolean
        get() {
            val videoRequired = videoUrl != null
            val contentRequired = coursUrl != null || fdrUrl != null

            val videoOk = if (videoRequired) isVideoWatched else true
            val contentOk = if (contentRequired) isContentRead else true

            return videoOk && contentOk
        }

    val hasVideo: Boolean get() = videoUrl != null
    val hasCours: Boolean get() = coursUrl != null
    val hasFdr: Boolean get() = fdrUrl != null
}

// ============================================
// QUIZ & QUESTIONS
// ============================================
data class Quiz(
    val quizId: String = "",
    val courseId: String = "",
    val chapterId: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList(),
    val timeLimit: Int? = null
)

data class Question(
    val questionId: String = "",
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String? = null,
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM
)

enum class QuestionDifficulty {
    EASY, MEDIUM, HARD
}

// ============================================
// PROGRESSION
// ============================================
data class CourseProgress(
    val completedChapters: Int,
    val totalChapters: Int
) {
    val percentage: Float
        get() = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f

    val isAllCompleted: Boolean
        get() = completedChapters == totalChapters
}