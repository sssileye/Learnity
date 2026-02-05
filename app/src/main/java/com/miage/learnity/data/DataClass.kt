package com.miage.learnity.data

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

// ============================================
// PROFIL UTILISATEUR COMPLET
// ============================================


// ============================================
// PROFIL UTILISATEUR COMPLET
// ============================================

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val photoUrl: String = "avatar_b1",
    val createdAt: Long = System.currentTimeMillis(),
    val redevanceSoutienUnitaire: Double = 1.0,
    val detteCumulee: Double = 0.0,
    val unityPoints: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val fcmToken: String? = null,
    val lastDailyQuizDate: String? = null,
    val selectedAssociationId: String? = null,
    val quizMode: String = "DISCOVERY"
)

// ============================================
// COURSE
// ============================================
data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconRes: Int? = null,
    val isFavorite: Boolean = false
)

// ============================================
// CHAPTER (Chapitre corrigé)
// ============================================
data class Chapter(
    val chapterId: String = "",
    val courseId: String = "",
    val title: String = "",
    val order: Int = 0,
    val isFavorite: Boolean = false,

    // On force Firestore à mapper "cours" sur ta variable "cours"
    @get:PropertyName("cours") @set:PropertyName("cours")
    var cours: String? = null,

    // On force Firestore à mapper "fdr" sur ta variable "fdr"
    @get:PropertyName("fdr") @set:PropertyName("fdr")
    var fdr: String? = null,

    @get:PropertyName("video") @set:PropertyName("video")
    var video: String? = null,

    // === MÉTADONNÉES ===
    val pageCount: Int = 0,
    val estimatedReadTime: Int = 0,
    val videoDuration: Int = 0,

    // === QUIZ ===
    val quizId: String? = null,
    val bestScore: Int = 0,

    // === ÉTATS DE PROGRESSION (Modifiables via .copy()) ===
    val isVideoWatched: Boolean = false,
    val isCoursRead: Boolean = false,
    val isFdrRead: Boolean = false,
    val isQuizCompleted: Boolean = false,

    // ⭐ CORRECTION : Passé en paramètre du constructeur pour être reconnu par le ViewModel
    val isQuizUnlocked: Boolean = false
) {
    /**
     * ✅ Chapitre 100% complété
     * (Cours lu ET Quiz fait. La vidéo est bonus selon ta règle)
     */
    val isCompleted: Boolean
        get() = isCoursRead && isQuizCompleted

    /**
     * ✅ Progression en pourcentage (0.0 à 1.0)
     */
    val progressPercentage: Float
        get() {
            var completed = 0f
            var total = 0f

            if (hasCours) { total += 1f; if (isCoursRead) completed += 1f }
            if (hasFdr) { total += 1f; if (isFdrRead) completed += 1f }
            if (hasVideo) { total += 1f; if (isVideoWatched) completed += 1f }
            if (quizId != null) { total += 1f; if (isQuizCompleted) completed += 1f }

            return if (total > 0) completed / total else 0f
        }

    /**
     * ✅ Helper pour savoir si le contenu minimal (Cours) est fini
     */
    val isContentCompleted: Boolean get() = isCoursRead

    // Helpers rapides
    val hasVideo: Boolean get() = video != null
    val hasCours: Boolean get() = cours != null
    val hasFdr: Boolean get() = fdr != null
}

// ============================================
// QUIZ, QUESTIONS, ASSOC & PROGRESSION
// ============================================

data class Quiz(
    val quizId: String = "",
    val courseId: String = "",
    val chapterId: String = "",
    val title: String = "",
    val questions: List<Question> = emptyList()
)
data class QuizHistory(
    val id: String = "",          // ID unique du document Firestore
    val date: String = "",        // Ex: "28/01/26"
    val hour: String = "",        // Ex: "10:34"
    val score: Int = 0,
    val total: Int = 0,
    val pointsGained: Int = 0,    // Le gain net (nouveaux points)
    val timestamp: Long = 0L      // Pour le tri (du plus récent au plus ancien)
)
data class Question(
    @SerializedName("text") val questionText: String = "",
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("correct") val correctAnswerIndex: Int = 0,
    @SerializedName("explanation") val explanation: String? = null,
    val chapterTitle: String? = null,
    val courseTitle: String? = null
)

data class Association(
    val name: String = "",
    val websiteUrl: String = "",
    val logoname: String = "",
    val description: String = ""

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

enum class FontSize {
    SMALL,
    MEDIUM,
    LARGE
}
data class SettingsData(
    val isDarkMode: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM
)