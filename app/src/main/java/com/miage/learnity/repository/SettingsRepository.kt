package com.miage.learnity.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.miage.learnity.data.FontSize  // ✅ Import depuis DataClass
import com.miage.learnity.data.SettingsData
import kotlinx.coroutines.flow.*

// Extension pour créer le DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ═══════════════════════════════════════════════════════════════
// 💾 SETTINGS REPOSITORY
// ═══════════════════════════════════════════════════════════════

class SettingsRepository(private val context: Context) {

    // Clés de préférences
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
    }

    /**
     * Flow qui observe les changements de paramètres
     */
    val settingsFlow: Flow<SettingsData> = context.dataStore.data
        .catch { exception ->
            println("⚠️ SettingsRepository - Erreur lecture DataStore: ${exception.message}")
            emit(emptyPreferences())
        }
        .map { preferences ->
            SettingsData(
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                fontSize = try {
                    FontSize.valueOf(
                        preferences[PreferencesKeys.FONT_SIZE] ?: FontSize.MEDIUM.name
                    )
                } catch (e: IllegalArgumentException) {
                    println("⚠️ SettingsRepository - FontSize invalide, reset à MEDIUM")
                    FontSize.MEDIUM
                }
            )
        }

    /**
     * Active ou désactive le mode sombre
     */
    suspend fun setDarkMode(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_MODE] = enabled
            }
            println("✅ SettingsRepository - Dark mode sauvegardé: $enabled")
        } catch (e: Exception) {
            println("❌ SettingsRepository - Erreur sauvegarde dark mode: ${e.message}")
            throw e
        }
    }

    /**
     * Définit la taille de police
     */
    suspend fun setFontSize(size: FontSize) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.FONT_SIZE] = size.name
            }
            println("✅ SettingsRepository - Font size sauvegardé: $size")
        } catch (e: Exception) {
            println("❌ SettingsRepository - Erreur sauvegarde font size: ${e.message}")
            throw e
        }
    }

    /**
     * Réinitialise tous les paramètres aux valeurs par défaut
     */
    suspend fun resetToDefaults() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            println("✅ SettingsRepository - Paramètres réinitialisés")
        } catch (e: Exception) {
            println("❌ SettingsRepository - Erreur reset: ${e.message}")
            throw e
        }
    }

    /**
     * Récupère les paramètres actuels de manière synchrone (une seule fois)
     */
    suspend fun getCurrentSettings(): SettingsData {
        return try {
            val preferences = context.dataStore.data.first()
            SettingsData(
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                fontSize = try {
                    FontSize.valueOf(
                        preferences[PreferencesKeys.FONT_SIZE] ?: FontSize.MEDIUM.name
                    )
                } catch (e: IllegalArgumentException) {
                    FontSize.MEDIUM
                }
            )
        } catch (e: Exception) {
            println("❌ SettingsRepository - Erreur lecture settings: ${e.message}")
            SettingsData()
        }
    }
}