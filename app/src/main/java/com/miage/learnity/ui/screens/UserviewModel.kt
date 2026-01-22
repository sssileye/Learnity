package com.miage.learnity.ui.screens


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.UnityPointsModel
import com.miage.learnity.model.VirtualDebtModel
import com.miage.learnity.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class UserViewModel(
    private val repository: UserRepository = UserRepository(),
    private val debtModel: VirtualDebtModel = VirtualDebtModel(),
    private val pointsModel: UnityPointsModel = UnityPointsModel()
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    /**
     * Logique de vérification d'assiduité au démarrage
     */
    fun refreshUserStats() {
        viewModelScope.launch {
            repository.getUserProfile().onSuccess { profile ->
                if (profile == null) return@onSuccess

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Date())

                if (profile.lastDailyQuizDate != null && profile.lastDailyQuizDate != todayStr) {
                    val lastDate = sdf.parse(profile.lastDailyQuizDate)
                    val todayDate = sdf.parse(todayStr)
                    val diff = TimeUnit.DAYS.convert(todayDate.time - lastDate.time, TimeUnit.MILLISECONDS).toInt()

                    if (diff > 1) {
                        // Sanction pour les jours ratés
                        val missed = diff - 1
                        val updated = profile.copy(
                            detteCumulee = profile.detteCumulee + debtModel.getAbsencePenalty(profile.redevanceSoutienUnitaire) * missed,
                            currentStreak = 0,
                            lastDailyQuizDate = todayStr
                        )
                        repository.saveUserProfile(updated)
                        _userProfile.value = updated
                        return@launch
                    }
                }
                _userProfile.value = profile
            }
        }
    }

    fun updateRedevance(newX: Double) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(redevanceSoutienUnitaire = newX)
            repository.saveUserProfile(updated).onSuccess { _userProfile.value = updated }
        }
    }
}