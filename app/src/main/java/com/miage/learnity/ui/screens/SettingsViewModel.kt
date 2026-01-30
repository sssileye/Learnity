package com.miage.learnity.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.FontSize
import com.miage.learnity.repository.SettingsRepository
import com.miage.learnity.repository.SettingsRepositorySingleton
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
// 📊 UI STATE
// ═══════════════════════════════════════════════════════════════

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val fontSize: FontSize = FontSize.MEDIUM,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════
// 🎯 VIEWMODEL
// ═══════════════════════════════════════════════════════════════

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    // État UI exposé au composable
    val uiState: StateFlow<SettingsUiState> = repository.settingsFlow
        .map { settingsData ->
            SettingsUiState(
                isDarkMode = settingsData.isDarkMode,
                fontSize = settingsData.fontSize,
                isLoading = false,
                error = null
            )
        }
        .catch { exception ->
            // En cas d'erreur, émettre un état avec l'erreur
            emit(
                SettingsUiState(
                    isLoading = false,
                    error = "Erreur de chargement des paramètres"
                )
            )
            println("❌ SettingsViewModel - Erreur flow: ${exception.message}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(isLoading = true)
        )

    /**
     * Toggle le mode sombre
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                val currentValue = uiState.value.isDarkMode
                repository.setDarkMode(!currentValue)
                println("🌙 SettingsViewModel - Dark mode toggled: ${!currentValue}")
            } catch (e: Exception) {
                println("❌ SettingsViewModel - Erreur toggle dark mode: ${e.message}")
                // TODO: Gérer l'erreur dans l'UI si nécessaire
            }
        }
    }

    /**
     * Change la taille de police
     */
    fun setFontSize(size: FontSize) {
        viewModelScope.launch {
            try {
                repository.setFontSize(size)
                println("📏 SettingsViewModel - Font size changed: $size")
            } catch (e: Exception) {
                println("❌ SettingsViewModel - Erreur set font size: ${e.message}")
                // TODO: Gérer l'erreur dans l'UI si nécessaire
            }
        }
    }

    /**
     * Réinitialise les paramètres par défaut
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                repository.resetToDefaults()
                println("🔄 SettingsViewModel - Reset to defaults")
            } catch (e: Exception) {
                println("❌ SettingsViewModel - Erreur reset: ${e.message}")
                // TODO: Gérer l'erreur dans l'UI si nécessaire
            }
        }
    }

    /**
     * Rafraîchit les paramètres (utile en cas d'erreur)
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                val currentSettings = repository.getCurrentSettings()
                println("🔄 SettingsViewModel - Refresh: $currentSettings")
            } catch (e: Exception) {
                println("❌ SettingsViewModel - Erreur refresh: ${e.message}")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 🏭 VIEWMODEL FACTORY
// ═══════════════════════════════════════════════════════════════

class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                repository = SettingsRepositorySingleton.getInstance(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}