package com.miage.learnity.model

import com.miage.learnity.data.UserProfile
import kotlin.math.roundToInt

object PointsManager {

    enum class QuizType {
        CHAPTER,
        DAILY,
        EXAM
    }

    data class QuizResult(
        val progressionPoints: Int,
        val bonusGained: Int,
        val debtAdded: Double,
        val isPerfect: Boolean
    )

    /**
     * Calcule le gain RÉEL d'une session.
     * @param oldBestScore Doit être le record ABSOLU chargé depuis Firebase.
     */
    fun calculateResults(
        type: QuizType,
        score: Int,
        totalQuestions: Int,
        oldBestScore: Int,
        profile: UserProfile,
        wasAlreadyPerfect: Boolean = false
    ): QuizResult {
        val isPerfect = score == totalQuestions
        var debt = 0.0
        var rawBonus = 0

        // 1. CALCUL DE LA PROGRESSION (Sécurité renforcée)
        // On s'assure que même avec un bug d'entrée, on ne donne pas de points
        // si le score actuel est inférieur ou égal au record.
        val diff = score - oldBestScore
        val rawProgression = if (diff > 0) diff else 0

        // 2. CALCUL DU BONUS PERFECT (Usage unique)
        if (isPerfect && !wasAlreadyPerfect) {
            rawBonus = when (type) {
                QuizType.CHAPTER -> 3
                QuizType.DAILY -> 5
                QuizType.EXAM -> 10
            }
        }

        // 3. CALCUL DE LA DETTE (Basé sur l'essai actuel)
        if (type == QuizType.DAILY) {
            val errors = totalQuestions - score
            if (errors > 0) {
                // On utilise la redevance unitaire du profil
                val costPerError = profile.redevanceSoutienUnitaire / totalQuestions.toDouble()
                debt = costPerError * errors
            }
        }

        // 4. MULTIPLICATEUR (Appliqué uniquement au Daily)
        val multiplier = if (type == QuizType.DAILY) getStreakMultiplier(profile.currentStreak) else 1.0

        // Arrondi mathématique pour éviter les Unity Points à virgule
        val finalProgression = (rawProgression * multiplier).roundToInt()
        val finalBonus = (rawBonus * multiplier).roundToInt()

        return QuizResult(
            progressionPoints = finalProgression,
            bonusGained = finalBonus,
            debtAdded = debt,
            isPerfect = isPerfect
        )
    }

    private fun getStreakMultiplier(streak: Int): Double {
        return when {
            streak >= 30 -> 2.0
            streak >= 15 -> 1.5
            streak >= 7 -> 1.2
            streak >= 3 -> 1.1
            else -> 1.0
        }
    }
}