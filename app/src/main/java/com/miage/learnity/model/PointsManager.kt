package com.miage.learnity.model

import com.miage.learnity.data.UserProfile
import kotlin.math.roundToInt

object PointsManager {

    enum class QuizType { CHAPTER, DAILY, EXAM }

    data class QuizResult(
        val progressionPoints: Int,
        val bonusGained: Int,
        val debtAdded: Double,
        val isPerfect: Boolean,
        val multiplierUsed: Double // ⭐ Ajouté pour l'affichage UI
    )

    fun calculateResults(
        type: QuizType,
        score: Int,
        totalQuestions: Int,
        oldBestScore: Int,
        profile: UserProfile,
        wasAlreadyPerfect: Boolean = false,
        isFirstAttemptToday: Boolean = true // ⭐ Nouveau paramètre
    ): QuizResult {
        val isPerfect = score == totalQuestions
        val multiplier = if (type == QuizType.DAILY) getStreakMultiplier(profile.currentStreak) else 1.0

        // --- 1. CAS DU QUIZ DU JOUR (DAILY) ---
        if (type == QuizType.DAILY) {
            // Si ce n'est pas le 1er essai, on ne donne rien (0 points, 0 dette)
            if (!isFirstAttemptToday) {
                return QuizResult(0, 0, 0.0, isPerfect, 1.0)
            }

            val progressionPoints = (score * multiplier).roundToInt()

            val bonusGained = if (isPerfect) 5 else 0

            val errors = totalQuestions - score
            val costPerError = (profile.redevanceSoutienUnitaire ?: 1.0) / totalQuestions.toDouble()
            val debt = costPerError * errors

            return QuizResult(
                progressionPoints = progressionPoints,
                bonusGained = bonusGained,
                debtAdded = debt,
                isPerfect = isPerfect,
                multiplierUsed = multiplier
            )
        }

        // --- 2. CAS DES CHAPITRES (Logic de record) ---
        val diff = score - oldBestScore
        val rawProgression = if (diff > 0) diff else 0
        var rawBonus = 0

        if (isPerfect && !wasAlreadyPerfect) {
            rawBonus = if (type == QuizType.EXAM) 10 else 3
        }

        return QuizResult(
            progressionPoints = rawProgression,
            bonusGained = rawBonus,
            debtAdded = 0.0,
            isPerfect = isPerfect,
            multiplierUsed = 1.0
        )
    }

    fun getStreakMultiplier(streak: Int): Double {
        return when {
            streak >= 30 -> 4.0
            streak >= 20 -> 3.0
            streak >= 10 -> 2.0
            else -> 1.0
        }
    }
}