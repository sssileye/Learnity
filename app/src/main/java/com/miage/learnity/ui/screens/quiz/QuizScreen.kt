package com.miage.learnity.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
    var showExitDialog by remember { mutableStateOf(false) }

    // Observation des états du ViewModel
    val courseTitle by viewModel.courseTitle.collectAsState()
    val chapterTitle by viewModel.chapterTitle.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val isAnswerRevealed by viewModel.isCurrentAnswerRevealed.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()

    // ⭐ Nouveaux états synchronisés pour le bilan final
    val sessionPoints by viewModel.sessionPointsGained.collectAsState()
    val sessionDebt by viewModel.sessionDebtAdded.collectAsState()
    val multiplier by viewModel.multiplierUsed.collectAsState()
    val isFirstAttempt by viewModel.isFirstAttempt.collectAsState()

    BackHandler(enabled = !isQuizFinished) { showExitDialog = true }

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
                    title = if (quizType == PointsManager.QuizType.DAILY) "Quiz du Jour" else "Quiz de chapitre",
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    onBackClick = { showExitDialog = true },
                    dimensions = dimensions
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            when {
                isLoading -> LoadingState(progress = loadingProgress, dimensions = dimensions)
                isQuizFinished -> {
                    LaunchedEffect(Unit) {
                        if (!isReviewMode) {
                            viewModel.processFinalResults(quizType, userViewModel, courseId, chapterId)
                        }
                    }
                    FinalResultContent(
                        questions = questions,
                        userAnswers = userAnswers,
                        score = score,
                        isReviewMode = isReviewMode,
                        isFirstAttempt = isFirstAttempt,
                        sessionPoints = sessionPoints,
                        sessionDebt = sessionDebt,
                        multiplier = multiplier,
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
                            showReturnToSummary = false,
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

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Quitter le quiz ?", fontWeight = FontWeight.Bold) },
                    text = { Text("Attention ! Ta progression et tes Unity Points de cette session seront perdus.") },
                    confirmButton = {
                        TextButton(onClick = { showExitDialog = false; onBackClick() }) {
                            Text("Quitter", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showExitDialog = false }) { Text("Continuer") }
                    }
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
        // --- 1. EN-TÊTE HIÉRARCHIQUE (SOBRE) ---
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(
                text = currentQuestion.courseTitle ?: courseTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black, // Plus d'autorité
                color = MaterialTheme.colorScheme.onBackground
            )

            val subTitle = currentQuestion.chapterTitle ?: chapterTitle
            if (subTitle.isNotEmpty()) {
                Text(
                    text = subTitle.uppercase(), // Majuscules pour le style "Pro"
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. BARRE DE PROGRESSION ---
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.2f))

        // --- 3. CARTE DE LA QUESTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )))
                    .padding(dimensions.cardPadding)
            ) {
                Text(
                    text = currentQuestion.questionText,
                    color = Color.White,
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

        // --- 5. EXPLICATION (SANS EMOJI) ---
        if (isAnswerRevealed) {
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall)
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                    Text(
                        text = "EXPLICATION",
                        fontWeight = FontWeight.Black,
                        fontSize = dimensions.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
                    Text(
                        text = currentQuestion.explanation ?: "Aucune explication additionnelle.",
                        fontSize = dimensions.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 6. BOUTONS DE NAVIGATION (MAJUSCULES) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("PRÉCÉDENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
                ) {
                    Text(
                        text = when {
                            !isAnswerRevealed -> "VALIDER"
                            currentIndex == totalQuestions - 1 -> "TERMINER"
                            else -> "SUIVANT"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
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
            modifier = Modifier.fillMaxWidth().height(dimensions.iconSizeLarge * 2.3f).combinedClickable(
                onClick = onClick,
                onLongClick = { showPreview = true }
            ),
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
    isReviewMode: Boolean,
    isFirstAttempt: Boolean,
    sessionPoints: Int,
    sessionDebt: Double,
    multiplier: Double,
    onReviewQuestion: (Int) -> Unit,
    onBackToCourse: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensions.screenPaddingHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Résultats",
            fontSize = dimensions.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = dimensions.itemSpacing)
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 1.5f),
            contentPadding = PaddingValues(bottom = dimensions.itemSpacing)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cornerRadiusLarge)) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))))
                            .padding(dimensions.cardPadding)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Votre Score", color = Color.White, fontSize = dimensions.bodyMedium)
                        Text(
                            text = "$score / ${questions.size}",
                            fontSize = dimensions.displayLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // ⭐ SECTION RÉCOMPENSE : Alignée, Sobre, sans Emojis
            item {
                if (isFirstAttempt && !isReviewMode) {
                    QuizRewardCard(
                        points = sessionPoints,
                        debt = sessionDebt,
                        multiplier = multiplier,
                        dimensions = dimensions
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Mode entraînement : aucun point supplémentaire n'est attribué pour cette tentative.",
                            modifier = Modifier.padding(dimensions.cardPadding),
                            textAlign = TextAlign.Center,
                            fontSize = dimensions.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "DÉTAILS DES RÉPONSES",
                    fontWeight = FontWeight.Black,
                    fontSize = dimensions.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(questions) { index, question ->
                val userChoice = userAnswers[index]
                val isCorrect = userChoice == question.correctAnswerIndex
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onReviewQuestion(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) MaterialTheme.successColors.successContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                        Text("Q${index + 1}: ${question.questionText}", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyMedium)
                        Text(
                            text = "Votre réponse : ${question.options.getOrNull(userChoice ?: -1) ?: "Non répondue"}",
                            fontSize = dimensions.bodySmall,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        Button(
            onClick = onBackToCourse,
            modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.itemSpacing).height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
        ) {
            Text("Quitter le quiz", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
        }
    }
}

@Composable
fun QuizRewardCard(
    points: Int,      // Reçu du ViewModel (déjà multiplié)
    debt: Double,     // Reçu du ViewModel
    multiplier: Double, // Reçu du ViewModel
    dimensions: ResponsiveDimensions
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.cardPadding)
        ) {
            // --- TITRE DE SECTION ---
            Text(
                text = "BILAN DE LA SESSION",
                fontSize = dimensions.bodySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))

            // --- LIGNE DE SÉPARATION SOBRE ---
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(dimensions.itemSpacing))

            // --- LIGNE UNITY POINTS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unity Points",
                        fontSize = dimensions.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (multiplier > 1.0) {
                        Text(
                            text = "Série actuelle : x${multiplier}",
                            fontSize = dimensions.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Text(
                    text = "+$points pts",
                    fontSize = dimensions.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.successColors.success
                )
            }

            Spacer(modifier = Modifier.height(dimensions.itemSpacing))

            // --- LIGNE DETTE VIRTUELLE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dette Virtuelle",
                    fontSize = dimensions.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "+${String.format("%.2f", debt)} €",
                    fontSize = dimensions.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (debt > 0) MaterialTheme.colorScheme.error else MaterialTheme.successColors.success
                )
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
                    text = "$currentQuestion / $totalQuestions",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
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