package com.miage.learnity.data.mock

import com.miage.learnity.R
import com.miage.learnity.data.Chapter
import com.miage.learnity.data.Course

/**
 * Données mockées pour le développement front-end
 * À remplacer par les données Firebase une fois le backend prêt
 */
object MockData {

    // ============================================
    // COURSES (Liste des cours disponibles)
    // ============================================
    val sampleCourses = listOf(
        Course(
            id = "extraction_connaissances",
            title = "Extraction des Connaissances",
            description = "Techniques d'extraction de connaissances à partir de données massives",
            iconRes = R.drawable.ic_homepage_1  // Utilise une icône existante temporairement
        ),
        Course(
            id = "gestion_projet_agile",
            title = "Gestion de Projet Agile",
            description = "Méthodes agiles : Scrum, Kanban, XP et pratiques DevOps",
            iconRes = R.drawable.ic_cours_1
        ),
        Course(
            id = "architecture_logicielle",
            title = "Architecture Logicielle",
            description = "Patterns d'architecture, microservices et bonnes pratiques",
            iconRes = R.drawable.ic_settings_1
        ),
        Course(
            id = "base_donnees",
            title = "Base de Données Avancées",
            description = "SQL avancé, NoSQL, optimisation et indexation",
            iconRes = R.drawable.ic_ranking
        )
    )

    // ============================================
    // CHAPTERS (Chapitres par cours)
    // ============================================
    private val extractionChapters = listOf(
        Chapter(
            chapterId = "ec_chap1",
            title = "Introduction à l'Extraction",
            order = 1,
            coursUrl = "https://github.com/your-org/EC_Chap1_Cours.pdf",
            fdrUrl = "https://github.com/your-org/EC_Chap1_FDR.pdf",
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            pageCount = 15,
            estimatedReadTime = 20,
            videoDuration = 25,
            quizId = "quiz_ec_chap1",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "ec_chap2",
            title = "Méthodes Supervisées",
            order = 2,
            coursUrl = "https://github.com/your-org/EC_Chap2_Cours.pdf",
            fdrUrl = null,
            videoUrl = null,
            pageCount = 25,
            estimatedReadTime = 35,
            videoDuration = 0,
            quizId = "quiz_ec_chap2",
            isVideoWatched = false,
            isContentRead = true,  // Déjà lu pour test
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "ec_chap3",
            title = "Clustering K-Means",
            order = 3,
            coursUrl = null,
            fdrUrl = null,
            videoUrl = "https://www.youtube.com/watch?v=kmeans123",
            pageCount = 0,
            estimatedReadTime = 0,
            videoDuration = 45,
            quizId = "quiz_ec_chap3",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "ec_chap4",
            title = "Arbres de Décision",
            order = 4,
            coursUrl = "https://github.com/your-org/EC_Chap4_Cours.pdf",
            fdrUrl = "https://github.com/your-org/EC_Chap4_FDR.pdf",
            videoUrl = null,
            pageCount = 20,
            estimatedReadTime = 28,
            videoDuration = 0,
            quizId = "quiz_ec_chap4",
            isVideoWatched = false,
            isContentRead = true,
            isQuizCompleted = true  // Complété pour test
        ),
        Chapter(
            chapterId = "ec_chap5",
            title = "Réseaux de Neurones",
            order = 5,
            coursUrl = "https://github.com/your-org/EC_Chap5_Cours.pdf",
            fdrUrl = null,
            videoUrl = "https://www.youtube.com/watch?v=neural123",
            pageCount = 35,
            estimatedReadTime = 45,
            videoDuration = 60,
            quizId = "quiz_ec_chap5",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        )
    )

    private val agileChapters = listOf(
        Chapter(
            chapterId = "gpa_chap1",
            title = "Introduction à Scrum",
            order = 1,
            coursUrl = "https://github.com/your-org/GPA_Chap1.pdf",
            fdrUrl = "https://github.com/your-org/GPA_Chap1_FDR.pdf",
            videoUrl = "https://www.youtube.com/watch?v=scrum123",
            pageCount = 18,
            estimatedReadTime = 25,
            videoDuration = 30,
            quizId = "quiz_gpa_chap1",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "gpa_chap2",
            title = "Sprints et Rétrospectives",
            order = 2,
            coursUrl = "https://github.com/your-org/GPA_Chap2.pdf",
            fdrUrl = null,
            videoUrl = null,
            pageCount = 22,
            estimatedReadTime = 30,
            videoDuration = 0,
            quizId = "quiz_gpa_chap2",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "gpa_chap3",
            title = "Kanban et Flow",
            order = 3,
            coursUrl = "https://github.com/your-org/GPA_Chap3.pdf",
            fdrUrl = "https://github.com/your-org/GPA_Chap3_FDR.pdf",
            videoUrl = "https://www.youtube.com/watch?v=kanban456",
            pageCount = 16,
            estimatedReadTime = 22,
            videoDuration = 28,
            quizId = "quiz_gpa_chap3",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "gpa_chap4",
            title = "Extreme Programming (XP)",
            order = 4,
            coursUrl = "https://github.com/your-org/GPA_Chap4.pdf",
            fdrUrl = null,
            videoUrl = null,
            pageCount = 20,
            estimatedReadTime = 28,
            videoDuration = 0,
            quizId = "quiz_gpa_chap4",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        )
    )

    private val architectureChapters = listOf(
        Chapter(
            chapterId = "al_chap1",
            title = "Patterns de Conception",
            order = 1,
            coursUrl = "https://github.com/your-org/AL_Chap1.pdf",
            fdrUrl = null,
            videoUrl = null,
            pageCount = 30,
            estimatedReadTime = 40,
            videoDuration = 0,
            quizId = "quiz_al_chap1",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "al_chap2",
            title = "Architecture Microservices",
            order = 2,
            coursUrl = "https://github.com/your-org/AL_Chap2.pdf",
            fdrUrl = "https://github.com/your-org/AL_Chap2_FDR.pdf",
            videoUrl = "https://www.youtube.com/watch?v=microservices",
            pageCount = 28,
            estimatedReadTime = 38,
            videoDuration = 50,
            quizId = "quiz_al_chap2",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "al_chap3",
            title = "Clean Architecture",
            order = 3,
            coursUrl = "https://github.com/your-org/AL_Chap3.pdf",
            fdrUrl = null,
            videoUrl = null,
            pageCount = 25,
            estimatedReadTime = 35,
            videoDuration = 0,
            quizId = "quiz_al_chap3",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        )
    )

    private val databaseChapters = listOf(
        Chapter(
            chapterId = "bd_chap1",
            title = "SQL Avancé",
            order = 1,
            coursUrl = "https://github.com/your-org/BD_Chap1.pdf",
            fdrUrl = "https://github.com/your-org/BD_Chap1_FDR.pdf",
            videoUrl = null,
            pageCount = 24,
            estimatedReadTime = 32,
            videoDuration = 0,
            quizId = "quiz_bd_chap1",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        ),
        Chapter(
            chapterId = "bd_chap2",
            title = "NoSQL et MongoDB",
            order = 2,
            coursUrl = "https://github.com/your-org/BD_Chap2.pdf",
            fdrUrl = null,
            videoUrl = "https://www.youtube.com/watch?v=mongodb123",
            pageCount = 20,
            estimatedReadTime = 28,
            videoDuration = 35,
            quizId = "quiz_bd_chap2",
            isVideoWatched = false,
            isContentRead = false,
            isQuizCompleted = false
        )
    )

    // Map pour associer courseId → chapters
    private val chaptersMap = mapOf(
        "extraction_connaissances" to extractionChapters,
        "gestion_projet_agile" to agileChapters,
        "architecture_logicielle" to architectureChapters,
        "base_donnees" to databaseChapters
    )

    // ============================================
    // FONCTIONS UTILITAIRES
    // ============================================

    /**
     * Récupère les chapitres d'un cours spécifique
     * @param courseId ID du cours
     * @return Liste des chapitres ou liste vide si cours non trouvé
     */
    fun getChaptersForCourse(courseId: String): List<Chapter> {
        return chaptersMap[courseId] ?: emptyList()
    }

    /**
     * Récupère un chapitre spécifique
     * @param courseId ID du cours
     * @param chapterId ID du chapitre
     * @return Le chapitre ou null si non trouvé
     */
    fun getChapter(courseId: String, chapterId: String): Chapter? {
        return chaptersMap[courseId]?.find { it.chapterId == chapterId }
    }

    /**
     * Récupère un cours spécifique
     * @param courseId ID du cours
     * @return Le cours ou null si non trouvé
     */
    fun getCourse(courseId: String): Course? {
        return sampleCourses.find { it.id == courseId }
    }
}