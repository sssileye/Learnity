package com.miage.learnity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ═══════════════════════════════════════════════════════════════
 * 🎨 GRADIENTS ADAPTATIFS POUR DARK MODE
 * ═══════════════════════════════════════════════════════════════
 *
 * Ce fichier contient toutes les fonctions de gradient qui s'adaptent
 * automatiquement au thème (light/dark mode).
 *
 * RÈGLE D'OR : Utiliser ces fonctions plutôt que des gradients hardcodés !
 */

// ═══════════════════════════════════════════════════════════════
// 📝 QUIZ GRADIENTS
// ═══════════════════════════════════════════════════════════════

/**
 * Gradient pour un quiz complété (bleu-violet en light, plus sombre en dark)
 */
@Composable
fun quizCompletedGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Utilise les couleurs du thème avec opacité réduite
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
        } else {
            // Light mode : Gradient bleu-violet vibrant
            listOf(
                Color(0xFF6B9FFF), // Bleu
                Color(0xFF7C6FFF)  // Violet
            )
        }
    )
}

/**
 * Gradient pour un quiz non complété (violet-rose en light, plus subtil en dark)
 */
@Composable
fun quizPendingGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Gradient plus doux
            listOf(
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        } else {
            // Light mode : Gradient violet-rose vibrant
            listOf(
                Color(0xFF7C6FFF), // Violet clair
                Color(0xFFFF6FB5)  // Rose
            )
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// 💰 DETTE VIRTUELLE GRADIENTS
// ═══════════════════════════════════════════════════════════════

/**
 * Gradient pour la dette (orange-rouge en light, error colors en dark)
 */
@Composable
fun debtGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Utilise les couleurs d'erreur du thème
            listOf(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        } else {
            // Light mode : Gradient orange-rouge
            listOf(
                Color(0xFFFF9A56), // Orange
                Color(0xFFFF6B6B)  // Rouge
            )
        }
    )
}

/**
 * Gradient pour pas de dette (turquoise-vert en light, success colors en dark)
 */
@Composable
fun noDebtGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Utilise les couleurs de succès du thème
            listOf(
                MaterialTheme.successColors.successContainer,
                MaterialTheme.successColors.success.copy(alpha = 0.8f)
            )
        } else {
            // Light mode : Gradient turquoise-vert
            listOf(
                Color(0xFF4ECDC4), // Turquoise
                Color(0xFF44A08D)  // Vert
            )
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// ✨ UNITY POINTS GRADIENTS
// ═══════════════════════════════════════════════════════════════

/**
 * Gradient pour Unity Points (bleu clair-bleu en light, primary colors en dark)
 */
@Composable
fun unityPointsGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Gradient basé sur le thème
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        } else {
            // Light mode : Gradient bleu vibrant
            listOf(
                Color(0xFF56CCF2), // Bleu clair
                Color(0xFF2F80ED)  // Bleu
            )
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// 🎓 EXAM/COURSE GRADIENTS
// ═══════════════════════════════════════════════════════════════

/**
 * Gradient pour examen déverrouillé (violet en light, tertiary en dark)
 */
@Composable
fun examUnlockedGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.verticalGradient(
        colors = if (isDark) {
            // Dark mode : Gradient basé sur tertiary
            listOf(
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
            )
        } else {
            // Light mode : Violet vibrant
            listOf(
                Color(0xFF673AB7),
                Color(0xFF8E24AA)
            )
        }
    )
}

/**
 * Gradient pour examen verrouillé (gris)
 */
@Composable
fun examLockedGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// 📊 HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════

/**
 * Retourne la couleur de texte appropriée pour un fond coloré
 * En dark mode : retourne onSurface (texte adaptatif)
 * En light mode : retourne blanc pour contraste maximal
 */
@Composable
fun getOnGradientTextColor(): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }
}

/**
 * Retourne une couleur de surface semi-transparente pour badges sur gradient
 */
@Composable
fun getGradientOverlayColor(alpha: Float = 0.25f): Color {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        Color.White.copy(alpha = alpha)
    }
}