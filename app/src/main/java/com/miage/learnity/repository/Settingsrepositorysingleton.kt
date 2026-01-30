package com.miage.learnity.repository

import android.content.Context

/**
 * Singleton pour garantir qu'une seule instance de SettingsRepository existe
 * dans toute l'application. Cela résout les problèmes de synchronisation
 * entre SettingsViewModel et ThemeViewModel.
 */
object SettingsRepositorySingleton {
    @Volatile
    private var instance: SettingsRepository? = null

    /**
     * Retourne l'instance unique du SettingsRepository
     * Thread-safe grâce à la double vérification avec synchronized
     */
    fun getInstance(context: Context): SettingsRepository {
        return instance ?: synchronized(this) {
            instance ?: SettingsRepository(context.applicationContext).also {
                instance = it
                println("✅ SettingsRepositorySingleton - Instance unique créée")
            }
        }
    }

    /**
     * Réinitialise l'instance (utile pour les tests)
     */
    @Suppress("unused")
    fun reset() {
        synchronized(this) {
            instance = null
            println("🔄 SettingsRepositorySingleton - Instance réinitialisée")
        }
    }
}