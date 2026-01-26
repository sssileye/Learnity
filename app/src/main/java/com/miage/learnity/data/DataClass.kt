package com.miage.learnity.data

import com.google.gson.annotations.SerializedName

// ============================================
// PROFIL UTILISATEUR COMPLET
// ============================================
data class UserProfile(
    // --- Informations d'Identification ---
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val photoUrl: String = "avatar_b1",
    val createdAt: Long = System.currentTimeMillis(),

    // --- Système de Redevance (La Dette) ---
    // Valeur "X" fixée par l'utilisateur pour son engagement
    val redevanceSoutienUnitaire: Double = 1.0,
    // Dette virtuelle cumulée (Incrémentée par X ou X/10)
    val detteCumulee: Double = 0.0,

    // --- Système de Récompense (Unity Points) ---
    val unityPoints: Int = 0,

    // --- Progression & Winstreak ---
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    // Format "yyyy-MM-dd" pour calculer l'absentéisme au démarrage
    val lastDailyQuizDate: String? = null,

    // --- Engagement Social ---
    // ID de l'association parrainée
    val selectedAssociationId: String? = null
)

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
    val questions: List<Question> = emptyList()
)
// ============================================
// Association
// ============================================
data class Association(
    val name: String = "",
    val websiteUrl: String = "", // Lien vers le site de don
    val logoName: String = "",
    val description: String = ""
)
data class Question(
    @SerializedName("text")
    val questionText: String = "",

    @SerializedName("options")
    val options: List<String> = emptyList(),

    @SerializedName("correct")
    val correctAnswerIndex: Int = 0,

    @SerializedName("explanation")
    val explanation: String? = null
)

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