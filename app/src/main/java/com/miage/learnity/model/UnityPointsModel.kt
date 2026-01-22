package com.miage.learnity.model

/**
 * Logique de calcul des Unity Points.
 * Seul le Quiz du Jour bénéficie des multiplicateurs de Winstreak.
 */
class UnityPointsModel {

    // Constantes de Bonus Perfect
    companion object {
        const val BONUS_CHAPTER = 3
        const val BONUS_MEGA_QUIZ = 10
        const val BONUS_DAILY_QUIZ = 5
    }

    /**
     * Calcule le multiplicateur basé sur le Winstreak (uniquement pour le QDJ)
     */
    fun getWinstreakMultiplier(streak: Int): Double {
        return when {
            streak >= 30 -> 2.0
            streak >= 15 -> 1.5
            streak >= 7  -> 1.2
            streak >= 3  -> 1.1
            else         -> 1.0
        }
    }

    /**
     * Calcule les points totaux gagnés.
     * @param correctAnswers Nombre de bonnes réponses (1 pt / réponse)
     * @param bonus Points bonus si le quiz est un sans-faute
     * @param streak Série actuelle (utilisée uniquement si isDailyQuiz = true)
     * @param isDailyQuiz Indique si on doit appliquer le multiplicateur
     */
    fun calculateFinalPoints(
        correctAnswers: Int,
        bonus: Int,
        streak: Int,
        isDailyQuiz: Boolean
    ): Int {
        val basePoints = correctAnswers + bonus

        return if (isDailyQuiz) {
            val multiplier = getWinstreakMultiplier(streak)
            (basePoints * multiplier).toInt()
        } else {
            basePoints
        }
    }
}