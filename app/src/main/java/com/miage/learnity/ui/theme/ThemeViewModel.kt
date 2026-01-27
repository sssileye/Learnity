package com.miage.learnity.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {

    // false = mode clair par défaut
    var isDarkMode = mutableStateOf(false)
        private set

    // 0f (Petit), 0.5f (Moyen), 1f (Grand)
    var fontScale = mutableStateOf(0.5f)

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
    }
}
