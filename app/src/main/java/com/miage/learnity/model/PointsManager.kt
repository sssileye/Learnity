package com.miage.learnity.model

import com.miage.learnity.data.UserProfile
import kotlin.math.roundToInt

object PointsManager {

    // ✅ Les 3 types de quiz que nous avons identifiés
    enum class QuizType {
        CHAPTER,    // 5 questions -> Bonus Perfect +3
        DAILY,      // 10 questions -> Bonus Perfect +5 + Multiplicateur + Dette possible
        EXAM        // 20 questions -> Bonus Perfect +10
    }

    data class QuizResult(
        val pointsGained: Int,
        val debtAdded: Double,
        val isPerfect: Boolean
    )

    /**
     * Calcule le gain et la redevance à la fin d'un quiz
     */
    fun calculateResults(
        type: QuizType,
        score: Int,
        totalQuestions: Int,
        profile: UserProfile
    ): QuizResult {
        var basePoints = score.toDouble()
        var bonus = 0.0
        var debt = 0.0
        val isPerfect = score == totalQuestions

        when (type) {
            QuizType.CHAPTER -> {
                if (isPerfect) bonus = 3.0
            }
            QuizType.DAILY -> {
                if (isPerfect) bonus = 5.0

                // Application du Multiplicateur de Streak sur (Score + Bonus)
                val multiplier = getStreakMultiplier(profile.currentStreak)
                basePoints = (basePoints + bonus) * multiplier
                bonus = 0.0 // On l'a déjà intégré dans le calcul précédent

                // Calcul de la Dette (uniquement QDJ) : X / 10 par erreur
                val errors = totalQuestions - score
                if (errors > 0) {
                    debt = (profile.redevanceSoutienUnitaire / 10.0) * errors
                }
            }
            QuizType.EXAM -> {
                if (isPerfect) bonus = 10.0
            }
        }

        return QuizResult(
            pointsGained = (basePoints + bonus).roundToInt(),
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