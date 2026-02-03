package com.miage.learnity.ui.screens.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz
import com.miage.learnity.data.QuizHistory
import com.miage.learnity.model.PointsManager
import com.miage.learnity.repository.QuizRepository
import com.miage.learnity.repository.UserProgressRepository
import com.miage.learnity.ui.screens.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository(),
    private val progressRepository: UserProgressRepository = UserProgressRepository()
) : ViewModel() {

    // --- États du Quiz ---
    private val _quiz = MutableStateFlow<Quiz?>(null)
    val quiz: StateFlow<Quiz?> = _quiz.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    // ⭐ ÉTATS POUR LES TITRES (Initialisés avec "" pour éviter les erreurs de type)
    private val _courseTitle = MutableStateFlow<String>("")
    val courseTitle: StateFlow<String> = _courseTitle.asStateFlow()

    private val _chapterTitle = MutableStateFlow<String>("")
    val chapterTitle: StateFlow<String> = _chapterTitle.asStateFlow()

    private var isResultSaved = false

    private val _oldBestScore = MutableStateFlow(0)
    val oldBestScore: StateFlow<Int> = _oldBestScore.asStateFlow()

    private val _wasAlreadyCompleted = MutableStateFlow(false)
    val wasAlreadyCompleted: StateFlow<Boolean> = _wasAlreadyCompleted.asStateFlow()

    // --- Navigation & Progression ---
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _maxIndexReached = MutableStateFlow(0)
    val maxIndexReached: StateFlow<Int> = _maxIndexReached.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    // --- États d'Affichage & Résultats ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _hasSeenSummary = MutableStateFlow(false)
    val hasSeenSummary: StateFlow<Boolean> = _hasSeenSummary.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _sessionPointsGained = MutableStateFlow(0)
    val sessionPointsGained: StateFlow<Int> = _sessionPointsGained.asStateFlow()

    private val _isCurrentAnswerRevealed = MutableStateFlow(false)
    val isCurrentAnswerRevealed: StateFlow<Boolean> = _isCurrentAnswerRevealed.asStateFlow()

    // ============================================
    // ⭐ GESTION DU CONTEXTE (TITRES)
    // ============================================

    private suspend fun fetchContextTitles(courseId: String, chapterId: String?) {
        try {
            // 1. Cas Daily Quiz
            if (courseId == "GLOBAL" || chapterId == "DISCOVERY" || chapterId == "REVIEW") {
                _courseTitle.value = "Quiz du Jour"
                _chapterTitle.value = if (chapterId == "DISCOVERY") "Mode Découverte" else "Mode Révisions"
                return
            }

            // 2. Charger le titre du cours (UE)
            repository.getCourseDetails(courseId).onSuccess { course ->
                _courseTitle.value = course.title
            }

            // 3. Charger le titre du chapitre
            when (chapterId) {
                "ALL_CHAPTERS" -> _chapterTitle.value = "Synthèse de l'UE"
                null -> _chapterTitle.value = ""
                else -> {
                    repository.getChapterDetails(courseId, chapterId).onSuccess { chapter ->
                        _chapterTitle.value = chapter.title
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("QuizVM", "Erreur fetchContextTitles: ${e.message}")
        }
    }

    // ============================================
    // CHARGEMENT DES MODES
    // ============================================

    fun loadQuiz(courseId: String, chapterId: String) {
        isResultSaved = false
        viewModelScope.launch {
            _isLoading.value = true
            fetchContextTitles(courseId, chapterId)
            fetchUserProgress(courseId, chapterId)
            repository.getQuizForChapter(courseId, chapterId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
            }
            _isLoading.value = false
        }
    }

    fun loadMegaQuiz(courseId: String) {
        isResultSaved = false
        viewModelScope.launch {
            _isLoading.value = true
            fetchContextTitles(courseId, "ALL_CHAPTERS")
            progressRepository.getChapterProgress(courseId, "ALL_CHAPTERS").onSuccess { progress ->
                _oldBestScore.value = progress?.bestScore ?: 0
                _wasAlreadyCompleted.value = (progress?.bestScore ?: 0) >= 20
            }
            repository.getMegaQuizForCourse(courseId).onSuccess { loadedQuiz ->
                _quiz.value = loadedQuiz
                _questions.value = loadedQuiz.questions
            }
            _isLoading.value = false
        }
    }

    fun loadDailyQuiz(isDiscoveryMode: Boolean) {
        isResultSaved = false

        viewModelScope.launch {
            _isLoading.value = true

            // 1. On vérifie d'abord s'il existe UN score aujourd'hui, peu importe le mode
            // Cela permet de verrouiller la session sur le premier mode choisi
            repository.getLastDailyQuizScore().onSuccess { result ->
                if (result != null) {
                    _oldBestScore.value = result.first
                    android.util.Log.d("QuizVM_Debug", "📥 DB -> Score existant trouvé (${result.first}). Mode verrouillé pour aujourd'hui.")
                } else {
                    _oldBestScore.value = 0
                    android.util.Log.d("QuizVM_Debug", "📥 DB -> Aucun score. Première tentative du jour.")
                }
            }.onFailure { e ->
                android.util.Log.e("QuizVM_Debug", "❌ Erreur check score: ${e.message}")
            }

            // 2. Détermination du mode à charger
            val mode = if (isDiscoveryMode) "DISCOVERY" else "REVIEW"
            android.util.Log.d("QuizVM_Debug", "🚀 Chargement du contenu - Mode: $mode")

            fetchContextTitles("GLOBAL", mode)

            // 3. Chargement effectif du quiz
            repository.getDailyQuiz(isDiscoveryMode) { progress ->
                _loadingProgress.value = progress
            }.onSuccess { dailyQuiz ->
                _quiz.value = dailyQuiz
                _questions.value = dailyQuiz.questions
                android.util.Log.d("QuizVM_Debug", "✅ Questions chargées avec succès.")
            }.onFailure { e ->
                android.util.Log.e("QuizVM_Debug", "❌ Erreur chargement questions: ${e.message}")
            }

            _isLoading.value = false
        }
    }

    private suspend fun fetchUserProgress(courseId: String, chapterId: String) {
        progressRepository.getChapterProgress(courseId, chapterId).onSuccess { progress ->
            _wasAlreadyCompleted.value = progress?.isQuizCompleted == true
            _oldBestScore.value = progress?.bestScore ?: 0
        }
    }

    // ============================================
    // ⭐ LOGIQUE DE FINALISATION
    // ============================================

    fun processFinalResults(
        quizType: PointsManager.QuizType,
        userViewModel: UserViewModel,
        courseId: String,
        chapterId: String
    ) {
        if (isResultSaved) return
        isResultSaved = true

        val profile = userViewModel.uiState.value.profile ?: return
        calculateAndSetScore(_userAnswers.value)

        val finalScore = _score.value
        val totalQuestions = _questions.value.size

        // ⭐ LOG DE DIAGNOSTIC INITIAL
        android.util.Log.d("QuizVM_Debug", "=== FIN DE QUIZ DETECTÉE ===")
        android.util.Log.d("QuizVM_Debug", "📍 Mode: $chapterId | Score: $finalScore/$totalQuestions")
        android.util.Log.d("QuizVM_Debug", "🔄 Valeur de _oldBestScore en mémoire: ${_oldBestScore.value}")

        // 1. Détermination du mode (Premier essai vs Entraînement)
        val isDailyQuiz = (chapterId == "REVIEW" || chapterId == "DISCOVERY")
        val isAlreadyDone = isDailyQuiz && _oldBestScore.value > 0

        if (isAlreadyDone) {
            android.util.Log.w("QuizVM_Debug", "⚠️ VERROU ACTIVÉ : Un score de ${_oldBestScore.value} existe déjà. Mode Entraînement activé.")
        } else {
            android.util.Log.i("QuizVM_Debug", "✅ VERROU DÉSACTIVÉ : Premier essai détecté (ou nouveau record hors QDJ).")
        }

        // 2. Calcul des résultats théoriques
        val calculation = PointsManager.calculateResults(
            type = quizType,
            score = finalScore,
            totalQuestions = totalQuestions,
            oldBestScore = _oldBestScore.value,
            profile = profile,
            wasAlreadyPerfect = _wasAlreadyCompleted.value
        )

        _sessionPointsGained.value = calculation.progressionPoints + calculation.bonusGained

        // 3. Analytics (Uniquement premier essai QDJ)
        if (isDailyQuiz && !isAlreadyDone) {
            com.google.firebase.ktx.Firebase.analytics.logEvent("qdj_completed_today") {
                param("score", finalScore.toLong())
                param("mode", chapterId)
            }
            android.util.Log.d("LearnityAnalytics", "📊 Firebase Analytics : Événement qdj_completed_today envoyé.")
        }

        viewModelScope.launch {
            try {
                val historyEntry = QuizHistory(
                    date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date()),
                    hour = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    score = finalScore,
                    total = totalQuestions,
                    pointsGained = _sessionPointsGained.value,
                    timestamp = System.currentTimeMillis()
                )

                // 4. Sauvegarde historique (le Repository gère l'unicité en DB)
                repository.saveQuizHistory(courseId, chapterId, historyEntry, _userAnswers.value)

                // 5. Mise à jour réelle du profil Firestore
                if (!isAlreadyDone) {
                    userViewModel.repository.updateStatsWithHighscore(
                        courseId = courseId,
                        chapterId = chapterId,
                        newScore = finalScore,
                        totalQuestions = totalQuestions,
                        quizType = quizType,
                        pointsCalculated = calculation.progressionPoints,
                        bonusCalculated = calculation.bonusGained,
                        debtCalculated = calculation.debtAdded
                    )
                    android.util.Log.d("QuizVM_Debug", "💰 Firestore mis à jour : +${calculation.progressionPoints} pts / Dette: ${calculation.debtAdded}€")
                } else {
                    android.util.Log.w("QuizVM_Debug", "🚫 Firestore IGNORE : Pas de modification de points/dette (Doublon QDJ).")
                }

                // 6. Rafraîchissement UI
                userViewModel.refreshProgressionStats()
                userViewModel.refreshDailyStats()

                android.util.Log.d("QuizVM_Debug", "🏁 Fin du process. Homepage prête à être synchronisée.")

            } catch (e: Exception) {
                android.util.Log.e("QuizVM_Debug", "❌ ERREUR FATALE dans processFinalResults: ${e.message}")
            }
        }
    }

    // ============================================
    // LOGIQUE DU JEU & NAVIGATION
    // ============================================

    fun selectAnswer(selectedIndex: Int) {
        if (!_isCurrentAnswerRevealed.value && _currentQuestionIndex.value >= _maxIndexReached.value) {
            _userAnswers.value = _userAnswers.value + (_currentQuestionIndex.value to selectedIndex)
        }
    }

    fun validateAnswer() {
        _isCurrentAnswerRevealed.value = true
        if (_currentQuestionIndex.value >= _maxIndexReached.value) {
            _maxIndexReached.value = _currentQuestionIndex.value + 1
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value++
            _isCurrentAnswerRevealed.value = _currentQuestionIndex.value < _maxIndexReached.value
        } else {
            calculateAndSetScore(_userAnswers.value)
            _isQuizFinished.value = true
        }
    }

    private fun calculateAndSetScore(answers: Map<Int, Int>) {
        var finalScore = 0
        _questions.value.forEachIndexed { index, question ->
            if (answers[index] == question.correctAnswerIndex) finalScore++
        }
        _score.value = finalScore
    }

    fun markSummaryAsSeen() { _hasSeenSummary.value = true }

    fun goToQuestionForReview(index: Int) {
        _currentQuestionIndex.value = index
        _isCurrentAnswerRevealed.value = true
        _isQuizFinished.value = false
    }

    fun returnToSummary() { _isQuizFinished.value = true }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
            _isCurrentAnswerRevealed.value = true
        }
    }
}