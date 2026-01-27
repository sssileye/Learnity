package com.miage.learnity.ui.screens.quiz

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.theme.successColors
import com.miage.learnity.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    isReviewMode: Boolean = false,
    viewModel: QuizViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val dimensions = rememberResponsiveDimensions()
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val isAnswerRevealed by viewModel.isCurrentAnswerRevealed.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val hasSeenSummary by viewModel.hasSeenSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    LaunchedEffect(courseId, chapterId) {
        when (chapterId) {
            "DISCOVERY" -> viewModel.loadDailyQuiz(isDiscoveryMode = true)
            "REVIEW" -> viewModel.loadDailyQuiz(isDiscoveryMode = false)
            "ALL_CHAPTERS" -> viewModel.loadMegaQuiz(courseId)
            else -> viewModel.loadQuiz(courseId, chapterId)
        }
    }

    LaunchedEffect(questions, isReviewMode) {
        if (isReviewMode && questions.isNotEmpty() && !isQuizFinished) {
            viewModel.loadOldAnswers()
            viewModel.returnToSummary()
        }
    }

    Scaffold(
        topBar = {
            if (questions.isNotEmpty() && !isQuizFinished) {
                QuizTopBar(
                    title = when (chapterId) {
                        "DISCOVERY", "REVIEW" -> "Quiz du Jour"
                        "ALL_CHAPTERS" -> "Synthése de l'UE"
                        else -> "Quiz de chapitre"
                    },
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    onBackClick = onBackClick,
                    dimensions = dimensions
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())  // âœ… UNIQUEMENT le padding top
        ) {
            when {
                isLoading -> LoadingState(progress = loadingProgress, dimensions = dimensions)
                questions.isEmpty() && !isLoading -> ErrorState(
                    msg = "Aucun quiz trouvé pour cette sélection.",
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
                    LaunchedEffect(Unit) { viewModel.markSummaryAsSeen() }
                    FinalResultContent(
                        questions = questions,
                        userAnswers = userAnswers,
                        score = score,
                        isReviewOnly = isReviewMode,
                        onReviewQuestion = { index -> viewModel.goToQuestionForReview(index) },
                        onRetry = { viewModel.resetQuiz() },
                        onBackToCourse = onBackClick,
                        dimensions = dimensions
                    )
                }
                else -> {
                    val currentQuestion = questions.getOrNull(currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuizContent(
                            currentQuestion = currentQuestion,
                            currentIndex = currentQuestionIndex,
                            totalQuestions = questions.size,
                            userAnswerIndex = userAnswers[currentQuestionIndex],
                            isAnswerRevealed = isAnswerRevealed,
                            showReturnToSummary = hasSeenSummary,
                            onAnswerSelected = { index -> viewModel.selectAnswer(index) },
                            onValidate = { viewModel.validateAnswer() },
                            onNext = { viewModel.nextQuestion() },
                            onPrevious = { viewModel.previousQuestion() },
                            onReturnToSummary = { viewModel.returnToSummary() },
                            dimensions = dimensions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizContent(
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
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.itemSpacing / 1.5f)
                .clip(RoundedCornerShape(dimensions.cornerRadiusSmall / 2)),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

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

        val chunks = currentQuestion.options.chunked(2)
        chunks.forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
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
                        text = "💡Explication",
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showReturnToSummary) {
                OutlinedButton(
                    onClick = onReturnToSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.itemSpacing)
                        .height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(dimensions.itemSpacing / 1.5f))
                    Text("Retour au récapitulatif", color = MaterialTheme.colorScheme.primary, fontSize = dimensions.bodyLarge)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.buttonHeightSmall)
                        .padding(end = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(2.dp, if (currentIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Précédent", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.buttonHeightSmall)
                        .padding(start = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = when {
                            !isAnswerRevealed -> "Valider"
                            currentIndex == totalQuestions - 1 && !showReturnToSummary -> "Terminer"
                            else -> "Suivant"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensions.bodyLarge
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
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.iconSizeLarge * 2.3f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showPreview = true },
                        onTap = { onClick() }
                    )
                },
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = CardDefaults.cardColors(backgroundColor),
            border = BorderStroke(
                width = if (isSelected || isCorrect || isWrong) 3.dp else 1.dp,
                color = borderColor.copy(alpha = if (isSelected || isCorrect || isWrong) 1f else 0.2f)
            ),
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
                Text(
                    text = text,
                    fontSize = dimensions.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = dimensions.bodyMedium * 1.2f,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.itemSpacing / 3)
                )
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
                        color = MaterialTheme.colorScheme.onPrimary,
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
    isReviewOnly: Boolean = false,
    onReviewQuestion: (Int) -> Unit,
    onRetry: () -> Unit,
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
            text = if (isReviewOnly) "Récapitulatif" else "Quiz Terminé",
            fontSize = dimensions.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))))
                    .padding(dimensions.cardPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isReviewOnly) "Score enregistré" else "Ton Score",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = dimensions.bodyMedium
                )
                Text(
                    text = "${if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0}%",
                    fontSize = dimensions.titleLarge,
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

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        Text(
            text = "RECAPITULATIF",
            fontWeight = FontWeight.ExtraBold,
            fontSize = dimensions.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = dimensions.bodySmall / 6
        )
        Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 1.5f),
            contentPadding = PaddingValues(bottom = dimensions.itemSpacing)
        ) {
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
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) MaterialTheme.successColors.success else MaterialTheme.colorScheme.error
                    )
                ) {
                    Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                        Text(
                            text = "Q${index + 1}: ${question.questionText}",
                            fontWeight = FontWeight.Bold,
                            fontSize = dimensions.bodyMedium
                        )
                        Text(
                            text = "Ta réponse : ${question.options.getOrNull(userChoice ?: -1) ?: "Aucune"}",
                            fontSize = dimensions.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Correct : ${question.options[question.correctAnswerIndex]}",
                                fontSize = dimensions.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.successColors.success
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        if (!isReviewOnly) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Recommencer le quiz",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(dimensions.itemSpacing))
        }

        Button(
            onClick = onBackToCourse,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                if (isReviewOnly) "Retour à l'accueil" else "Quitter",
                fontWeight = FontWeight.Bold,
                fontSize = dimensions.bodyLarge
            )
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
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = dimensions.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSizeMedium)
                )
            }
        },
        actions = {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = dimensions.itemSpacing / 1.5f)
            ) {
                Text(
                    text = "$currentQuestion/$totalQuestions",
                    modifier = Modifier.padding(
                        horizontal = dimensions.itemSpacing,
                        vertical = dimensions.itemSpacing / 2
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.bodyMedium
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
            color = Color.Gray
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
            Text("RÃ©essayer", fontSize = dimensions.bodyLarge)
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