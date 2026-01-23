package com.miage.learnity.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    // État du mode sombre
    var isDarkMode = mutableStateOf(true)

    // État de la police : 0f (Petit), 0.5f (Moyen), 1f (Grand)
    var fontScale = mutableStateOf(0.5f)

    fun toggleTheme() {
        isDarkMode.value = !isDarkMode.value
    }
}