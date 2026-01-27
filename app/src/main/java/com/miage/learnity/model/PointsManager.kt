package com.miage.learnity.model

import com.miage.learnity.data.UserProfile
import kotlin.math.roundToInt

object PointsManager {

    enum class QuizType {
        CHAPTER,    // 5 questions -> Bonus Perfect +3
        DAILY,      // 10 questions -> Bonus Perfect +5 + Multiplicateur + Dette
        EXAM        // 20 questions -> Bonus Perfect +10
    }

    data class QuizResult(
        val pointsGained: Int, // Points de base (score)
        val bonusGained: Int,  // Bonus "Perfect" uniquement
        val debtAdded: Double,
        val isPerfect: Boolean
    )

    /**
     * Calcule le gain potentiel d'une session.
     * Note : La logique de comparaison avec l'ancien record (High Score)
     * se fera dans le Repository lors de la transaction.
     */
    fun calculateResults(
        type: QuizType,
        score: Int,
        totalQuestions: Int,
        profile: UserProfile
    ): QuizResult {
        val isPerfect = score == totalQuestions
        var debt = 0.0
        var bonus = 0

        // 1. Calcul du Bonus "Perfect" (One-shot)
        if (isPerfect) {
            bonus = when (type) {
                QuizType.CHAPTER -> 3
                QuizType.DAILY -> 5
                QuizType.EXAM -> 10
            }
        }

        // 2. Calcul de la Dette (Uniquement QDJ)
        // Correction du bug : (Redevance / Nb total questions) * erreurs
        if (type == QuizType.DAILY) {
            val errors = totalQuestions - score
            if (errors > 0) {
                val costPerError = profile.redevanceSoutienUnitaire / totalQuestions.toDouble()
                debt = costPerError * errors
            }
        }

        // 3. Application du Multiplicateur (Uniquement QDJ)
        // Si c'est un QDJ, on multiplie le score de base et le bonus
        val finalBasePoints = if (type == QuizType.DAILY) {
            (score * getStreakMultiplier(profile.currentStreak)).roundToInt()
        } else {
            score
        }

        val finalBonus = if (type == QuizType.DAILY) {
            (bonus * getStreakMultiplier(profile.currentStreak)).roundToInt()
        } else {
            bonus
        }

        return QuizResult(
            pointsGained = finalBasePoints,
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