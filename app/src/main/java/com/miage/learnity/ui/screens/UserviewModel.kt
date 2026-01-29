package com.miage.learnity.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.miage.learnity.data.Association
import com.miage.learnity.data.UserProfile
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.UserRepository
import com.miage.learnity.repository.UserProgressRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * État UI enrichi pour le profil et la progression
 */
data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val readChaptersCount: Int = 0,
    val totalChaptersCount: Int = 0
)

class UserViewModel(
    val repository: UserRepository = UserRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // ⭐ Gestion des Associations Dynamiques
    private val _associations = MutableStateFlow<List<Association>>(emptyList())
    val associations: StateFlow<List<Association>> = _associations.asStateFlow()

    init {
        observeProfile()
        checkAndApplyAttendancePenalty()
        refreshProgressionStats()

        // ⭐ On charge les associations au démarrage (et on fait le seed si besoin)
        fetchAssociations()
        // ⚠️ DÉCOMMENTE CETTE LIGNE UNE SEULE FOIS POUR REMPLIR FIREBASE, PUIS RE-COMMENTE
        seedAssociationsToFirebase()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeUserProfile()
                .catch { e ->
                    _uiState.update { it.copy(
                        error = "Erreur de connexion : ${e.message}",
                        isLoading = false
                    ) }
                }
                .collect { profile ->
                    _uiState.update { it.copy(
                        profile = profile,
                        isLoading = false,
                        error = null
                    ) }
                }
        }
    }

    /**
     * SYNCHRONISATION GLOBALE
     */
    fun refreshProgressionStats() {
        calculateReadChaptersCount()
        calculateTotalChaptersCount()
    }

    /**
     * Récupère la liste des associations depuis Firebase
     */
    private fun fetchAssociations() {
        viewModelScope.launch {
            try {
                // ⚠️ "Associations" avec majuscule comme dans ta base
                val snapshot = firestore.collection("Associations").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Association::class.java)
                }
                _associations.value = list
                Log.d("LearnityDebug", "${list.size} associations chargées.")
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur chargement associations : ${e.message}")
            }
        }
    }

    /**
     * Compte uniquement les chapitres présents dans LA progression de l'utilisateur connecté.
     * Utilisation de CollectionGroup pour contourner les erreurs de listing hiérarchique.
     */
    private fun calculateReadChaptersCount() {
        val userId = repository.getCurrentUserId() ?: return
        val projectId = firestore.app.options.projectId

        Log.d("LearnityDebug", "--- DÉBUT SCAN DYNAMIQUE ---")
        Log.d("LearnityDebug", "Projet: $projectId | User: $userId")

        viewModelScope.launch {
            try {
                val snapshot = firestore.collectionGroup("chapters").get().await()

                var count = 0
                for (doc in snapshot.documents) {
                    if (doc.reference.path.contains("user_progress/$userId")) {
                        val isCoursRead = doc.getBoolean("isCoursRead") ?: false
                        val isFdrRead = doc.getBoolean("isFdrRead") ?: false

                        if (isCoursRead || isFdrRead) {
                            count++
                        }
                    }
                }

                _uiState.update { it.copy(readChaptersCount = count) }
                Log.d("LearnityDebug", "TOTAL FINAL DÉTECTÉ : $count")

            } catch (e: Exception) {
                Log.e("LearnityDebug", "❌ Erreur Scan : ${e.message}")
            }
        }
    }

    private fun calculateTotalChaptersCount() {
        viewModelScope.launch {
            try {
                val coursesSnapshot = firestore.collection("courses").get().await()
                var globalCount = 0

                for (courseDoc in coursesSnapshot.documents) {
                    val chaptersSnapshot = courseDoc.reference.collection("chapters").get().await()
                    globalCount += chaptersSnapshot.size()
                }

                _uiState.update { it.copy(totalChaptersCount = globalCount) }
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur calcul total : ${e.message}")
            }
        }
    }

    fun updateQuizMode(newMode: String) {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile
            if (currentProfile != null) {
                _uiState.update { it.copy(profile = currentProfile.copy(quizMode = newMode)) }
            }
            repository.updateQuizMode(newMode).onFailure { e ->
                _uiState.update { it.copy(error = "Erreur mode quiz : ${e.message}") }
            }
        }
    }

    fun processQuizResult(
        quizType: PointsManager.QuizType,
        score: Int,
        totalQuestions: Int,
        courseId: String,
        chapterId: String
    ) {
        val profile = _uiState.value.profile ?: return
        viewModelScope.launch {
            val progress = progressRepository.getChapterProgress(courseId, chapterId).getOrNull()
            val absoluteBestScore = progress?.bestScore ?: 0
            val wasAlreadyPerfect = absoluteBestScore >= totalQuestions

            val result = PointsManager.calculateResults(
                type = quizType,
                score = score,
                totalQuestions = totalQuestions,
                oldBestScore = absoluteBestScore,
                profile = profile,
                wasAlreadyPerfect = wasAlreadyPerfect
            )

            repository.updateStatsWithHighscore(
                courseId = courseId,
                chapterId = chapterId,
                newScore = score,
                totalQuestions = totalQuestions,
                quizType = quizType,
                pointsCalculated = result.progressionPoints,
                bonusCalculated = result.bonusGained,
                debtCalculated = result.debtAdded
            ).onSuccess {
                refreshProgressionStats()
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Erreur sauvegarde : ${e.message}") }
            }
        }
    }

    // ⚠️ Fonction temporaire : À lancer UNE SEULE FOIS pour remplir la base
    fun seedAssociationsToFirebase() {
        val associations = listOf(
            // FRANCE (5)
            Association("Fédération ATENA", "https://www.helloasso.com/associations/federation-atena/formulaires/1", "logo_atena", "Épicerie solidaire pour étudiants à Bordeaux.", "France"),
            Association("Les Restos du Cœur", "https://www.restosducoeur.org/faire-un-don-financier/", "logo_restosducoeur", "Aide alimentaire et accompagnement social.", "France"),
            Association("Secours Populaire", "https://www.secourspopulaire.fr/don", "logo_secours_populaire_francais", "Lutte contre la pauvreté et l'exclusion.", "France"),
            Association("Linkee", "https://www.helloasso.com/associations/linkee-bordeaux/formulaires/3", "logo_linkee", "Anti-gaspillage alimentaire étudiants.", "France"),
            Association("M-Tech", "https://www.helloasso.com/associations/association-m-tech/formulaires/1", "logo_mtech", "Association technologique étudiante.", "France"),

            // SÉNÉGAL (3)
            Association("ENDA Pronat", "https://endapronat.org/", "logo_enda_senegal", "Agriculture durable Sénégal.", "Sénégal"),
            Association("SOS Villages Sénégal", "https://www.sosve.org/senegal", "logo_sosenfantvillage_senegal", "Protection enfants vulnérables.", "Sénégal"),
            Association("APAF Sénégal", "https://www.apaf-afrique.org/", "logo_apafsenegal", "Formation agricole jeunes.", "Sénégal"),

            // MALI (3)
            Association("UNICEF Mali", "https://www.unicef.org/mali/", "logo_unicefmali", "Protection enfants maliens.", "Mali"),
            Association("Croix-Rouge Mali", "https://www.ifrc.org/our-network/national-societies/mali", "logo_croixrougemali", "Secours d'urgence Mali.", "Mali"),
            Association("MSF Mali", "https://www.msf.org/mali", "logo_medecinsansfrontiere", "Soins zones conflit.", "Mali"),

            // RDC (3)
            Association("Caritas Congo", "https://www.caritas.org/where-caritas-work/africa/democratic-republic-of-congo/", "logo_caritascongo", "Aide humanitaire RDC.", "RDC"),
            Association("World Vision RDC", "https://www.worldvision.org/our-work/countries/democratic-republic-of-congo", "logo_worldvisoncongo", "Parrainage enfants RDC.", "RDC"),
            Association("PAM RDC", "https://www.wfp.org/countries/democratic-republic-congo", "logo_pamcongo", "Aide alimentaire RDC.", "RDC"),

            // MARTINIQUE (3)
            Association("Secours Populaire 972", "https://www.secourspopulaire.fr/", "logo_secourspopulairemartinique", "Aide alimentaire Martinique.", "Martinique"),
            Association("Banque Alimentaire 972", "https://www.banquealimentaire.org/", "logo_banquealimentairemartinique", "Redistribution 972.", "Martinique"),
            Association("Secours Catholique 972", "https://www.secours-catholique.org/", "logo_secourscatholiquemartinique", "Solidarité 972.", "Martinique"),

            // MAROC (2)
            Association("AMADE Maroc", "https://www.amade.ma/", "logo_amadmaroc", "Protection enfants Maroc.", "Maroc"),
            Association("Fondation Mohammed V", "https://fm5.ma/", "logo_fondationmaroc", "Solidarité Maroc.", "Maroc")
        )

        viewModelScope.launch {
            associations.forEach { asso ->
                firestore.collection("Associations")
                    .document(asso.name)
                    .set(asso)
                    .addOnSuccessListener { Log.d("LearnitySeed", "Succès : ${asso.name}") }
                    .addOnFailureListener { Log.e("LearnitySeed", "Erreur : ${asso.name}") }
            }
        }
    }

    fun checkAndApplyAttendancePenalty() {
        viewModelScope.launch {
            val profile = repository.getUserProfile().getOrNull() ?: return@launch
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            if (profile.lastDailyQuizDate == null || profile.lastDailyQuizDate == todayStr) return@launch

            try {
                val lastDate = sdf.parse(profile.lastDailyQuizDate)
                val diffMillis = Date().time - lastDate!!.time
                val daysDiff = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                if (daysDiff > 1) {
                    val missedDays = (daysDiff - 1).toDouble()
                    val totalPenalty = profile.redevanceSoutienUnitaire * missedDays
                    repository.applyAbsenteeismPenalty(totalPenalty)
                }
            } catch (e: Exception) {
                Log.e("LearnityDebug", "Erreur assiduité : ${e.message}")
            }
        }
    }

    fun updateRedevance(newValue: Double) {
        viewModelScope.launch { repository.updateRedevanceUnitaire(newValue) }
    }

    fun makeDonation(amount: Double) {
        viewModelScope.launch {
            repository.deductFromDebt(amount).onFailure { e ->
                _uiState.update { it.copy(error = "Erreur lors du don : ${e.message}") }
            }
        }
    }
}