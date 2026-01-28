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
        val progressionPoints: Int, // Gain réel par rapport au record
        val bonusGained: Int,       // Bonus si c'est le premier Perfect
        val debtAdded: Double,
        val isPerfect: Boolean
    )

    /**
     * Calcule le gain REEL d'une session.
     * Prend en compte l'ancien record pour ne pas payer deux fois les mêmes points.
     */
    fun calculateResults(
        type: QuizType,
        score: Int,
        totalQuestions: Int,
        oldBestScore: Int,      // ✅ Ajouté pour calculer la différence
        profile: UserProfile,
        wasAlreadyPerfect: Boolean = false // ✅ Ajouté pour le bonus one-shot
    ): QuizResult {
        val isPerfect = score == totalQuestions
        var debt = 0.0
        var rawBonus = 0

        // 1. Calcul de la Progression Réelle
        // Si je fais 5/20 et que mon record était 5/20, progression = 0.
        val rawProgression = if (score > oldBestScore) score - oldBestScore else 0

        // 2. Calcul du Bonus "Perfect"
        // Accordé uniquement si score max ET jamais fait de perfect avant
        if (isPerfect && !wasAlreadyPerfect) {
            rawBonus = when (type) {
                QuizType.CHAPTER -> 3
                QuizType.DAILY -> 5
                QuizType.EXAM -> 10
            }
        }

        // 3. Calcul de la Dette (Uniquement Quotidien)
        if (type == QuizType.DAILY) {
            val errors = totalQuestions - score
            if (errors > 0) {
                val costPerError = profile.redevanceSoutienUnitaire / totalQuestions.toDouble()
                debt = costPerError * errors
            }
        }

        // 4. Application du Multiplicateur de Streak (Uniquement Quotidien)
        val multiplier = if (type == QuizType.DAILY) getStreakMultiplier(profile.currentStreak) else 1.0

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