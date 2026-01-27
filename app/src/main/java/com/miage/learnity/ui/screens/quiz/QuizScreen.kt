package com.miage.learnity.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question
import com.miage.learnity.model.PointsManager
import com.miage.learnity.ui.screens.UserViewModel
import com.miage.learnity.ui.utils.*

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
    val oldBestScore by viewModel.oldBestScore.collectAsState() // ✅ Récupération du record
    val userUiState by userViewModel.uiState.collectAsState()

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
                    onBackClick = onBackClick,
                    dimensions = dimensions
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            when {
                isLoading -> LoadingState(progress = loadingProgress, dimensions = dimensions)
                questions.isEmpty() && !isLoading -> ErrorState(msg = "Aucun quiz trouvé.", onRetry = { onBackClick() }, dimensions = dimensions)
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
                        oldBestScore = oldBestScore, // ✅ Passé ici
                        quizType = quizType,
                        isReviewMode = isReviewMode,
                        wasAlreadyDone = wasAlreadyDone,
                        redevance = userUiState.profile?.redevanceSoutienUnitaire ?: 1.0,
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
            .background(Color(0xFFF5F7FA))
            .padding(dimensions.screenPaddingHorizontal)
            .verticalScroll(rememberScrollState())
    ) {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.itemSpacing / 1.5f)
                .clip(RoundedCornerShape(dimensions.cornerRadiusSmall / 2)),
            color = Color(0xFF673AB7)
        )

        Spacer(modifier = Modifier.height(dimensions.itemSpacing * 1.5f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
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

        if (isAnswerRevealed) {
            Spacer(modifier = Modifier.height(dimensions.itemSpacing / 2))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(dimensions.cornerRadiusSmall)
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding)) {
                    Text("💡 Explication", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(dimensions.itemSpacing / 1.5f))
                    Text(
                        text = currentQuestion.explanation ?: "Pas d'explication disponible.",
                        fontSize = dimensions.bodyMedium,
                        color = Color(0xFF1B5E20),
                        lineHeight = (dimensions.bodyMedium.value * 1.4).sp
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimensions.itemSpacing).height(dimensions.buttonHeightSmall),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(1.dp, Color(0xFF673AB7))
                ) {
                    Icon(Icons.Default.List, null, tint = Color(0xFF673AB7))
                    Spacer(Modifier.width(dimensions.itemSpacing / 1.5f))
                    Text("Retour au récapitulatif", color = Color(0xFF673AB7))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall).padding(end = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    border = BorderStroke(2.dp, if (currentIndex > 0) Color(0xFF673AB7) else Color.LightGray)
                ) {
                    Text("Précédent", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier.weight(1f).height(dimensions.buttonHeightSmall).padding(start = dimensions.itemSpacing / 2),
                    shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
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
        isCorrect -> Color(0xFFE8F5E9)
        isWrong -> Color(0xFFFFEBEE)
        isSelected -> Color(0xFFE3F2FD)
        else -> Color.White
    }
    val borderColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFF44336)
        isSelected -> Color(0xFF3F51B5)
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
                Surface(shape = CircleShape, color = Color(0xFF5E35B1), modifier = Modifier.size(dimensions.iconSizeMedium * 0.9f).align(Alignment.TopEnd)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = id.toString(), fontSize = dimensions.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Text(text = text, fontSize = dimensions.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).fillMaxWidth(), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }

        if (showPreview) {
            Popup(alignment = Alignment.Center, onDismissRequest = { showPreview = false }) {
                Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF323232))) {
                    Text(text = text, color = Color.White, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
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
    oldBestScore: Int, // ✅ Ajouté ici
    quizType: PointsManager.QuizType,
    isReviewMode: Boolean,
    wasAlreadyDone: Boolean,
    redevance: Double,
    onReviewQuestion: (Int) -> Unit,
    onRetry: () -> Unit,
    onBackToCourse: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).padding(dimensions.screenPaddingHorizontal).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Résultats", fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cornerRadiusLarge)) {
            Column(
                modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)))).padding(dimensions.cardPadding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ton Score", color = Color.White)
                Text(text = "${if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0}%", fontSize = dimensions.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$score / ${questions.size}", color = Color.White, fontSize = dimensions.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        if ((quizType == PointsManager.QuizType.DAILY && wasAlreadyDone) || isReviewMode) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.itemSpacing),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(dimensions.cardPadding), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Mode Entraînement", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = dimensions.bodyLarge)
                    Text(
                        text = if (quizType == PointsManager.QuizType.DAILY)
                            "Tes points et ta dette pour ce quiz ont déjà été enregistrés lors de ta première tentative aujourd'hui."
                        else "Consultation de tes résultats précédents. Aucun point n'est ajouté.",
                        textAlign = TextAlign.Center,
                        fontSize = dimensions.bodySmall,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        } else {
            // ✅ Correction de l'ordre des arguments : score, total, type, redevance, oldBestScore, dimensions
            QuizRewardCard(score, questions.size, quizType, redevance, oldBestScore, dimensions)
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Text("DÉTAILS DES RÉPONSES", fontWeight = FontWeight.ExtraBold, fontSize = dimensions.bodySmall, color = Color.Gray)

        questions.forEachIndexed { index, question ->
            val isCorrect = userAnswers[index] == question.correctAnswerIndex
            ResultItem(index, isCorrect, onClick = { onReviewQuestion(index) }, dimensions = dimensions)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(dimensions.itemSpacing))

        Button(onClick = onBackToCourse, modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight), shape = RoundedCornerShape(dimensions.cornerRadiusLarge), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) {
            Text("Quitter", fontWeight = FontWeight.Bold)
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
            containerColor = if (hasProgressed) Color(0xFFFFFDE7) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasProgressed) Color(0xFFFFD600) else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nouveaux Unity Points",
                    fontSize = dimensions.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = if (hasProgressed) "+$totalNetGain" else "0",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasProgressed) Color(0xFF2E7D32) else Color.Gray
                )

                if (!hasProgressed && score > 0) {
                    Text(
                        text = "Record : $oldBestScore pts",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }

            if (type == PointsManager.QuizType.DAILY) {
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.LightGray))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Impact Dette", fontSize = dimensions.bodySmall, color = Color.Gray)
                    val errors = total - score
                    val debt = (redevance / total.toDouble()) * errors
                    Text(
                        text = "+${"%.2f".format(debt)}€",
                        fontWeight = FontWeight.Bold,
                        color = if (errors > 0) Color.Red else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun ResultItem(index: Int, isCorrect: Boolean, onClick: () -> Unit, dimensions: ResponsiveDimensions) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel, null, tint = if (isCorrect) Color(0xFF4CAF50) else Color.Red)
            Spacer(Modifier.width(16.dp))
            Text("Question ${index + 1}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(title: String, currentQuestion: Int, totalQuestions: Int, onBackClick: () -> Unit, dimensions: ResponsiveDimensions) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = dimensions.titleMedium) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
        actions = {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(end = 8.dp)) {
                Text(text = "$currentQuestion/$totalQuestions", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun LoadingState(progress: Float, dimensions: ResponsiveDimensions) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Chargement...", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.8f).height(8.dp).clip(CircleShape), color = Color(0xFF673AB7))
    }
}

@Composable
private fun ErrorState(msg: String, onRetry: () -> Unit, dimensions: ResponsiveDimensions) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(msg, color = Color.Red)
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}