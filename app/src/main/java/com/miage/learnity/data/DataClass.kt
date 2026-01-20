package com.miage.learnity.data

// ============================================
// COURSE (Cours - Sans les chapitres)
// ============================================
data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconRes: Int? = null
)

// ============================================
// CHAPTER (Chapitre avec contenu flexible)
// ============================================
data class Chapter(
    val chapterId: String = "",
    val title: String = "",
    val order: Int = 0,

    // === CONTENU PÉDAGOGIQUE ===
    val coursUrl: String? = null,
    val fdrUrl: String? = null,
    val videoUrl: String? = null,

    // === MÉTADONNÉES ===
    val pageCount: Int = 0,
    val estimatedReadTime: Int = 0,
    val videoDuration: Int = 0,

    // === QUIZ ===
    val quizId: String? = null,

    // === ÉTATS DE PROGRESSION ===
    val isVideoWatched: Boolean = false,
    val isCoursRead: Boolean = false,
    val isFdrRead: Boolean = false,
    val isQuizCompleted: Boolean = false
) {
    /**
     * ✅ Chapitre 100% complété (tout fait y compris quiz)
     */
    val isCompleted: Boolean
        get() {
            val videoRequired = videoUrl != null
            val contentRequired = coursUrl != null || fdrUrl != null

            val videoOk = if (videoRequired) isVideoWatched else true
            val contentOk = if (contentRequired) {
                (coursUrl != null && isCoursRead) || (fdrUrl != null && isFdrRead)
            } else {
                true
            }

            return videoOk && contentOk && isQuizCompleted
        }

    /**
     * ✅ NOUVEAU - Progression en pourcentage (0.0 à 1.0)
     * Compte tout le contenu indépendamment du quiz
     */
    val progressPercentage: Float
        get() {
            var completed = 0f
            var total = 0f

            // Compter cours
            if (coursUrl != null) {
                total += 1f
                if (isCoursRead) completed += 1f
            }

            // Compter FDR
            if (fdrUrl != null) {
                total += 1f
                if (isFdrRead) completed += 1f
            }

            // Compter vidéo
            if (videoUrl != null) {
                total += 1f
                if (isVideoWatched) completed += 1f
            }

            // Compter quiz
            if (quizId != null) {
                total += 1f
                if (isQuizCompleted) completed += 1f
            }

            return if (total > 0) completed / total else 0f
        }

    /**
     * ✅ NOUVEAU - Contenu terminé (lecture + vidéo, sans quiz)
     */
    val isContentCompleted: Boolean
        get() {
            val videoRequired = videoUrl != null
            val contentRequired = coursUrl != null || fdrUrl != null

            val videoOk = if (videoRequired) isVideoWatched else true
            val contentOk = if (contentRequired) {
                (coursUrl != null && isCoursRead) || (fdrUrl != null && isFdrRead)
            } else {
                true
            }

            return videoOk && contentOk
        }

    val isQuizUnlocked: Boolean
        get() {
            val videoRequired = videoUrl != null
            val contentRequired = coursUrl != null || fdrUrl != null

            val videoOk = if (videoRequired) isVideoWatched else true
            val contentOk = if (contentRequired) {
                (coursUrl != null && isCoursRead) || (fdrUrl != null && isFdrRead)
            } else {
                true
            }

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