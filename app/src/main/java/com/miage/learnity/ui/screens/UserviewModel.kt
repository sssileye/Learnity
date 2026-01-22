package com.miage.learnity.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.UnityPointsModel
import com.miage.learnity.model.VirtualDebtModel
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * État UI pour le profil utilisateur
 */
data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel pour gérer le profil utilisateur et ses statistiques
 */
class UserViewModel(
    private val repository: UserRepository = UserRepository(),
    private val debtModel: VirtualDebtModel = VirtualDebtModel(),
    private val pointsModel: UnityPointsModel = UnityPointsModel()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // ============================================
    // CHARGEMENT DU PROFIL
    // ============================================

    /**
     * Charge le profil utilisateur
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getUserProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                    println("✅ UserViewModel - Profil chargé")
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erreur de chargement"
                    )
                    println("❌ UserViewModel - Erreur : ${exception.message}")
                }
        }
    }

    /**
     * 🔥 Observe le profil en temps réel
     */
    fun observeProfile() {
        viewModelScope.launch {
            repository.observeUserProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
                println("🔥 UserViewModel - Profil mis à jour en temps réel")
            }
        }
    }

    // ============================================
    // VÉRIFICATION D'ASSIDUITÉ
    // ============================================

    /**
     * Vérifie l'assiduité et applique les pénalités si nécessaire
     */
    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            val profile = _uiState.value.profile ?: return@launch

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            // Si pas de dernière date, pas de pénalité
            if (profile.lastDailyQuizDate == null) {
                println("ℹ️ UserViewModel - Première connexion, pas de pénalité")
                repository.updateLastQuizDate(todayStr)
                return@launch
            }

            // Vérifier si c'est le même jour
            if (profile.lastDailyQuizDate == todayStr) {
                println("ℹ️ UserViewModel - Quiz déjà fait aujourd'hui")
                return@launch
            }

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate!!)
                val todayDate = sdf.parse(todayStr)

                val diffMillis = todayDate!!.time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                println("📅 UserViewModel - Jours écoulés : $daysDiff")

                when {
                    daysDiff == 1 -> {
                        // Jour consécutif : pas de pénalité
                        println("✅ UserViewModel - Jour consécutif, pas de pénalité")
                    }

                    daysDiff > 1 -> {
                        // Jours manqués : appliquer pénalité
                        val missedDays = daysDiff - 1
                        val penalty = debtModel.getAbsencePenalty(profile.redevanceSoutienUnitaire) * missedDays

                        println("⚠️ UserViewModel - $missedDays jours manqués, pénalité : $penalty€")

                        // Ajouter la dette et réinitialiser le streak
                        repository.addDebt(penalty)
                        repository.updateStreak(0)
                    }
                }

            } catch (e: Exception) {
                println("❌ UserViewModel - Erreur calcul assiduité : ${e.message}")
            }
        }
    }

    // ============================================
    // MISE À JOUR DU PROFIL
    // ============================================

    /**
     * Met à jour la redevance unitaire
     */
    fun updateRedevance(newValue: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.updateUserFields(mapOf("redevanceSoutienUnitaire" to newValue))
                .onSuccess {
                    _uiState.value.profile?.let { profile ->
                        _uiState.value = _uiState.value.copy(
                            profile = profile.copy(redevanceSoutienUnitaire = newValue),
                            isLoading = false
                        )
                    }
                    println("✅ UserViewModel - Redevance mise à jour : $newValue€")
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                    println("❌ UserViewModel - Erreur : ${exception.message}")
                }
        }
    }

    /**
     * Ajoute des Unity Points
     */
    fun addPoints(points: Int) {
        viewModelScope.launch {
            repository.addUnityPoints(points)
                .onSuccess {
                    loadProfile() // Recharger pour obtenir la nouvelle valeur
                    println("✅ UserViewModel - Points ajoutés : +$points")
                }
                .onFailure { exception ->
                    println("❌ UserViewModel - Erreur ajout points : ${exception.message}")
                }
        }
    }

    /**
     * Incrémente le streak
     */
    fun incrementStreak() {
        viewModelScope.launch {
            val currentStreak = _uiState.value.profile?.currentStreak ?: 0
            val newStreak = currentStreak + 1

            repository.updateStreak(newStreak)
                .onSuccess {
                    loadProfile()
                    println("✅ UserViewModel - Streak incrémenté : $newStreak")
                }
        }
    }

    /**
     * Réinitialise le streak
     */
    fun resetStreak() {
        viewModelScope.launch {
            repository.updateStreak(0)
                .onSuccess {
                    loadProfile()
                    println("✅ UserViewModel - Streak réinitialisé")
                }
        }
    }

    // ============================================
    // GESTION QUIZ DU JOUR
    // ============================================

    /**
     * Marque le quiz du jour comme terminé
     */
    fun markDailyQuizCompleted() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            repository.updateLastQuizDate(todayStr)
                .onSuccess {
                    loadProfile()
                    println("✅ UserViewModel - Quiz du jour marqué comme complété")
                }
        }
    }
}