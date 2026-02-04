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

            // Au 1er essai, on donne des points pour chaque bonne réponse
            // Barème suggéré : 2 points par bonne réponse * multiplicateur
            val basePoints = score * 2
            val bonusPerfect = if (isPerfect) 5 else 0

            val errors = totalQuestions - score
            val costPerError = (profile.redevanceSoutienUnitaire ?: 1.0) / totalQuestions.toDouble()
            val debt = costPerError * errors

            return QuizResult(
                progressionPoints = (basePoints * multiplier).roundToInt(),
                bonusGained = (bonusPerfect * multiplier).roundToInt(),
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
            streak >= 30 -> 2.0
            streak >= 15 -> 1.5
            streak >= 7 -> 1.2
            streak >= 3 -> 1.1
            else -> 1.0
        }
    }
}