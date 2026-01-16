package com.miage.learnity.data

data class Course(
    val id: String,
    val title: String,
    val description: String,
)
data class Chapter(
    val title: String = "",
    val order: Int = 0,
    val cours: String? = null,  // URL vers le PDF sur GitHub
    val fdr: String? = null,    // URL vers la Fiche de Révision
    val video: String? = null,  // URL YouTube ou autre
    val quiz: String? = null,    // URL du JSON ou ID du quiz
    val isVideoWatched: Boolean = false,
    val isContentRead: Boolean = false,
    val isQuizCompleted: Boolean = false
){
    val isCompleted: Boolean
        get() = isVideoWatched && isContentRead && isQuizCompleted
    val isQuizUnlocked: Boolean
        get() = isVideoWatched && isContentRead
}
data class Question(
    val questionText: String = "",
    val options: List<String> = emptyList(), // Liste des réponses possibles
    val correctAnswerIndex: Int = 0,        // Index de la bonne réponse (0, 1, 2...)
    val explanation: String? = null         // Explication affichée après la réponse
)

data class Quiz(
    val quizId: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList() // Ta banque de 100 questions
)

data class CourseProgress(
    val completedChapters: Int,
    val totalChapters: Int
) {
    val percentage: Float
        get() = if (totalChapters > 0) completedChapters.toFloat() / totalChapters else 0f

    val isAllCompleted: Boolean
        get() = completedChapters == totalChapters
}
data class Association(
    val name: String = "",
    val websiteUrl: String = "", // Lien vers le site de don
    val logoId: Int // Lien vers l'image
)
