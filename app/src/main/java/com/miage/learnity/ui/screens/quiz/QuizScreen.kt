package com.miage.learnity.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question
import com.miage.learnity.model.PointsManager
import com.miage.learnity.ui.screens.UserViewModel
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.theme.successColors
import com.miage.learnity.ui.utils.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    isReviewMode: Boolean = false,
    viewModel: QuizViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val dimensions = rememberResponsiveDimensions()

    // --- ÉTAT DU POP-UP DE SÉCURITÉ ---
    var showExitDialog by remember { mutableStateOf(false) }

    // Observation des titres et états
    val courseTitle: String by viewModel.courseTitle.collectAsState()
    val chapterTitle: String by viewModel.chapterTitle.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val isAnswerRevealed by viewModel.isCurrentAnswerRevealed.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val hasSeenSummary by viewModel.hasSeenSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val wasAlreadyDone by viewModel.wasAlreadyCompleted.collectAsState()
    val oldBestScore by viewModel.oldBestScore.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()

    // ⭐ INTERCEPTION BOUTON RETOUR TÉLÉPHONE
    BackHandler(enabled = !isQuizFinished) {
        showExitDialog = true
    }

    val quizType = remember(chapterId) {
        when (chapterId) {
            "DISCOVERY", "REVIEW" -> PointsManager.QuizType.DAILY
            "ALL_CHAPTERS" -> PointsManager.QuizType.EXAM
            else -> PointsManager.QuizType.CHAPTER
        }
    }

    LaunchedEffect(courseId, chapterId) {
        when (chapterId) {
            "DISCOVERY" -> viewModel.loadDailyQuiz(isDiscoveryMode = true)
            "REVIEW" -> viewModel.loadDailyQuiz(isDiscoveryMode = false)
            "ALL_CHAPTERS" -> viewModel.loadMegaQuiz(courseId)
            else -> viewModel.loadQuiz(courseId, chapterId)
        }
    }

    Scaffold(
        topBar = {
            if (questions.isNotEmpty() && !isQuizFinished) {
                QuizTopBar(
                    title = when (quizType) {
                        PointsManager.QuizType.DAILY -> "Quiz du Jour"
                        PointsManager.QuizType.EXAM -> "Synthèse de l'UE"
                        else -> "Quiz de chapitre"
                    },
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    // ⭐ DÉCLENCHE LE POP-UP VIA LA FLÈCHE TOPBAR
                    onBackClick = { showExitDialog = true },
                    dimensions = dimensions
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {

            // --- LOGIQUE D'AFFICHAGE ---
            when {
                isLoading -> LoadingState(progress = loadingProgress, dimensions = dimensions)
                questions.isEmpty() && !isLoading -> ErrorState(
                    msg = "Aucun quiz trouvé.",
                    onRetry = {
                        when (chapterId) {
                            "DISCOVERY" -> viewModel.loadDailyQuiz(isDiscoveryMode = true)
                            "REVIEW" -> viewModel.loadDailyQuiz(isDiscoveryMode = false)
                            "ALL_CHAPTERS" -> viewModel.loadMegaQuiz(courseId)
                            else -> viewModel.loadQuiz(courseId, chapterId)
                        }
                    },
                    dimensions = dimensions
                )
                isQuizFinished -> {
                    LaunchedEffect(Unit) {
                        viewModel.markSummaryAsSeen()
                        if (!isReviewMode) {
                            viewModel.processFinalResults(quizType, userViewModel, courseId, chapterId)
                        }
                    }
                    FinalResultContent(
                        questions = questions,
                        userAnswers = userAnswers,
                        score = score,
                        oldBestScore = oldBestScore,
                        quizType = quizType,
                        isReviewMode = isReviewMode,
                        wasAlreadyDone = wasAlreadyDone,
                        redevance = userUiState.profile?.redevanceSoutienUnitaire ?: 1.0,
                        onReviewQuestion = { index -> viewModel.goToQuestionForReview(index) },
                        onBackToCourse = onBackClick,
                        dimensions = dimensions
                    )
                }
                else -> {
                    val currentQuestion = questions.getOrNull(currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuizContent(
                            courseTitle = courseTitle,
                            chapterTitle = chapterTitle,
                            currentQuestion = currentQuestion,
                            currentIndex = currentQuestionIndex,
                            totalQuestions = questions.size,
                            userAnswerIndex = userAnswers[currentQuestionIndex],
                            isAnswerRevealed = isAnswerRevealed,
                            showReturnToSummary = hasSeenSummary,
                            onAnswerSelected = { viewModel.selectAnswer(it) },
                            onValidate = { viewModel.validateAnswer() },
                            onNext = { viewModel.nextQuestion() },
                            onPrevious = { viewModel.previousQuestion() },
                            onReturnToSummary = { viewModel.returnToSummary() },
                            dimensions = dimensions
                        )
                    }
                }
            }

            // ⭐ LE POP-UP "GNAGNAGNA" (AlertDialog de sécurité)
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = {
                        Text("Quitter le quiz ?", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text("Attention ! Si tu quittes maintenant, ta progression sera perdue et tes Unity Points ne seront pas enregistrés.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitDialog = false
                                onBackClick() // On quitte vraiment ici
                            }
                        ) {
                            Text("Quitter", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showExitDialog = false }) {
                            Text("Continuer le quiz")
                        }
                    },
                    shape = RoundedCornerShape(dimensions.cornerRadiusMedium)
                )
            }
        }
    }
}


@Composable
private fun QuizContent(
    courseTitle: String,
    chapterTitle: String,
    currentQuestion: Question,
    currentIndex: Int,
    totalQuestions: Int,
    userAnswerIndex: Int?,
    isAnswerRevealed: Boolean,
    showReturnToSummary: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onValidate: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onReturnToSummary: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensions.screenPaddingHorizontal)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. EN-TÊTE HIÉRARCHIQUE ---
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            // UE en grand (ou "Quiz du Jour" / UE spécifique si Daily)
            Text(
                text = currentQuestion.courseTitle ?: courseTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Chapitre en petit juste en dessous
            val subTitle = currentQuestion.chapterTitle ?: chapterTitle
            if (subTitle.isNotEmpty()) {
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. BARRE DE PROGRESSION ---
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.itemSpacing / 1.5f)
                .clip(RoundedCornerShape(dimensions.cornerRadiusSmall / 2)),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.2f))

        // --- 3. CARTE DE LA QUESTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))))
                    .padding(dimensions.cardPadding)
            ) {
                Text(
                    text = currentQuestion.questionText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = dimensions.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

        // --- 4. OPTIONS DE RÉPONSE ---
        val chunks = currentQuestion.options.chunked(2)
        chunks.forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
            ) {
                pair.forEachIndexed { columnIndex, option ->
                    val actualIndex = rowIndex * 2 + columnIndex
                    val isSelected = userAnswerIndex == actualIndex

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        QuizOptionCard(
                            text = option,
                            id = actualIndex + 1,
                            isSelected = isSelected,
                            isCorrect = isAnswerRevealed && actualIndex == currentQuestion.correctAnswerIndex,
                            isWrong = isAnswerRevealed && isSelected && actualIndex != currentQuestion.correctAnswerIndex,
                            onClick = { if (!isAnswerRevealed) onAnswerSelected(actualIndex) },
                            dimensions = dimensions
                        )
                    }
                }
                if (pair.size < 2) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        }

        // --- 5. EXPLICATION (SI RÉVÉLÉ) ---
        if (isAnswerRevealed) {
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.successColors.successContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.successColors.success.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall)
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                    Text(
                        text = "💡 Explication",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge,
                        color = MaterialTheme.successColors.success
                    )
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
                    Text(
                        text = currentQuestion.explanation ?: "Pas d'explication disponible.",
                        fontSize = dimensions.bodyMedium,
                        color = MaterialTheme.successColors.onSuccessContainer,
                        lineHeight = dimensions.bodyMedium * 1.4f
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 6. BOUTONS DE NAVIGATION ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showReturnToSummary) {
                OutlinedButton(
                    onClick = onReturnToSummary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimensions.itemSpacing).height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(dimensions.itemSpacing / 1.5f))
                    Text("Retour au récapitulatif", color = MaterialTheme.colorScheme.primary, fontSize = dimensions.bodyLarge)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall).padding(end = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(2.dp, if (currentIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Précédent", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall).padding(start = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = when {
                            !isAnswerRevealed -> "Valider"
                            currentIndex == totalQuestions - 1 && !showReturnToSummary -> "Terminer"
                            else -> "Suivant"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuizOptionCard(
    text: String,
    id: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    var showPreview by remember { mutableStateOf(false) }
    val backgroundColor = when {
        isCorrect -> MaterialTheme.successColors.successContainer
        isWrong -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isCorrect -> MaterialTheme.successColors.success
        isWrong -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().height(dimensions.iconSizeLarge * 2.3f).pointerInput(Unit) {
                detectTapGestures(onLongPress = { showPreview = true }, onTap = { onClick() })
            },
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(if (isSelected || isCorrect || isWrong) 3.dp else 1.dp, borderColor.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(dimensions.itemSpacing / 1.5f)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(dimensions.iconSizeMedium * 0.9f)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = id.toString(),
                            fontSize = dimensions.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Text(text = text, fontSize = dimensions.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).fillMaxWidth(), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }

        if (showPreview) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPreview = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(dimensions.screenPaddingHorizontal),
                    shape = RoundedCornerShape(dimensions.cornerRadiusSmall),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(dimensions.cardPadding),
                        textAlign = TextAlign.Center,
                        fontSize = dimensions.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FinalResultContent(
    questions: List<Question>,
    userAnswers: Map<Int, Int>,
    score: Int,
    oldBestScore: Int,
    quizType: PointsManager.QuizType,
    isReviewMode: Boolean,
    wasAlreadyDone: Boolean,
    redevance: Double,
    onReviewQuestion: (Int) -> Unit,
    // ✅ onRetry supprimé ici
    onBackToCourse: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    // On retire le .verticalScroll sur la Column pour laisser la LazyColumn gérer le flux
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensions.screenPaddingHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER FIXE ---
        Text(
            text = "Résultats",
            fontSize = dimensions.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = dimensions.itemSpacing)
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        // --- LISTE SCROLLABLE CONTENANT TOUT LE RESTE ---
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 1.5f),
            contentPadding = PaddingValues(bottom = dimensions.itemSpacing)
        ) {
            // Section Score
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cornerRadiusLarge)) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))))
                            .padding(dimensions.cardPadding)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isReviewMode) "Score enregistré" else "Ton Score",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = dimensions.bodyMedium
                        )
                        Text(
                            text = "${if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0}%",
                            fontSize = dimensions.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$score / ${questions.size}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = dimensions.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Section Récompenses ou Mode Entraînement
            item {
                if ((quizType == PointsManager.QuizType.DAILY && wasAlreadyDone) || isReviewMode) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(dimensions.cardPadding), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Points déjà enregistrés pour aujourd'hui.", fontSize = dimensions.bodySmall)
                        }
                    }
                } else {
                    QuizRewardCard(score, questions.size, quizType, redevance, oldBestScore, dimensions)
                }
            }

            item {
                Text(
                    text = "DÉTAILS DES RÉPONSES",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Liste des questions
            itemsIndexed(questions) { index, question ->
                val userChoice = userAnswers[index]
                val isCorrect = userChoice == question.correctAnswerIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReviewQuestion(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) MaterialTheme.successColors.successContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                    border = BorderStroke(1.dp, if (isCorrect) MaterialTheme.successColors.success else MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                        Text("Q${index + 1}: ${question.questionText}", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyMedium)
                        Text(
                            text = "Ta réponse : ${question.options.getOrNull(userChoice ?: -1) ?: "Aucune"}",
                            fontSize = dimensions.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        // --- BOUTON FIXE EN BAS ---
        Button(
            onClick = onBackToCourse,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensions.itemSpacing)
                .height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
        ) {
            Text(if (isReviewMode) "Retour" else "Quitter le quiz", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
        }
    }
}

@Composable
fun QuizRewardCard(
    score: Int,
    total: Int,
    type: PointsManager.QuizType,
    redevance: Double,
    oldBestScore: Int,
    dimensions: ResponsiveDimensions
) {
    val pointsGained = if (score > oldBestScore) (score - oldBestScore) else 0
    val isNewPerfect = score == total && oldBestScore < total
    val bonus = if (isNewPerfect) {
        when(type) {
            PointsManager.QuizType.CHAPTER -> 3
            PointsManager.QuizType.DAILY -> 5
            PointsManager.QuizType.EXAM -> 10
        }
    } else 0

    val totalNetGain = pointsGained + bonus
    val hasProgressed = totalNetGain > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (hasProgressed)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasProgressed)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensions.cardPadding).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nouveaux Unity Points",
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Stars,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconSizeSmall)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (hasProgressed) "+$totalNetGain" else "0",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = dimensions.titleMedium,
                        color = if (hasProgressed)
                            MaterialTheme.successColors.success
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!hasProgressed && score > 0) {
                    Text(
                        text = "Record : $oldBestScore pts",
                        fontSize = dimensions.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (type == PointsManager.QuizType.DAILY) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Impact Dette",
                        fontSize = dimensions.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val errors = total - score
                    val debt = (redevance / total.toDouble()) * errors
                    Text(
                        text = "+${"%.2f".format(debt)}€",
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.titleMedium,
                        color = if (errors > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.successColors.success
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(
    title: String,
    currentQuestion: Int,
    totalQuestions: Int,
    onBackClick: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = dimensions.titleMedium) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
        actions = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "$currentQuestion/$totalQuestions",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun LoadingState(progress: Float, dimensions: ResponsiveDimensions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Analyse de tes cours...",
            fontSize = dimensions.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(dimensions.itemSpacing)
                .clip(RoundedCornerShape(dimensions.itemSpacing / 2)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = dimensions.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))

        Text(
            text = "Préparation de ton quiz personnalisé",
            fontSize = dimensions.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ErrorState(msg: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = msg, fontSize = dimensions.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        Button(
            onClick = onRetry,
            modifier = Modifier.height(dimensions.buttonHeightSmall),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Réessayer", fontSize = dimensions.bodyLarge)
        }
    }
}

@Preview(name = "Petit (320dp)", widthDp = 320, heightDp = 640)
@Preview(name = "Moyen (360dp)", widthDp = 360, heightDp = 720)
@Preview(name = "Grand (410dp)", widthDp = 410, heightDp = 820)
@Preview(name = "Tablette (600dp)", widthDp = 600, heightDp = 960)
@Composable
fun QuizScreenPreview() {
    LearnityTheme {
        QuizScreen(
            courseId = "test",
            chapterId = "test",
            onBackClick = {}
        )
    }
}