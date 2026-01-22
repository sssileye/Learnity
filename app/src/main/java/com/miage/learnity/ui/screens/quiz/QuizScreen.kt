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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    isReviewMode: Boolean = false,
    viewModel: QuizViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val isAnswerRevealed by viewModel.isCurrentAnswerRevealed.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val hasSeenSummary by viewModel.hasSeenSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ⭐ Nouveau : Récupérer la progression du chargement
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    // Chargement initial des questions
    LaunchedEffect(courseId, chapterId) {
        when (chapterId) {
            "DISCOVERY" -> viewModel.loadDailyQuiz(isDiscoveryMode = true)
            "REVIEW" -> viewModel.loadDailyQuiz(isDiscoveryMode = false)
            "ALL_CHAPTERS" -> viewModel.loadMegaQuiz(courseId)
            else -> viewModel.loadQuiz(courseId, chapterId)
        }
    }

    // Logique pour le mode "Revoir"
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
                        "ALL_CHAPTERS" -> "Synthèse de l'UE"
                        else -> "Quiz de chapitre"
                    },
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    onBackClick = onBackClick
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                // ⭐ Passage du progrès à la vue de chargement
                isLoading -> LoadingState(progress = loadingProgress)

                questions.isEmpty() && !isLoading -> ErrorState(
                    msg = "Aucun quiz trouvé pour cette sélection.",
                    onRetry = {
                        when (chapterId) {
                            "DISCOVERY" -> viewModel.loadDailyQuiz(isDiscoveryMode = true)
                            "REVIEW" -> viewModel.loadDailyQuiz(isDiscoveryMode = false)
                            "ALL_CHAPTERS" -> viewModel.loadMegaQuiz(courseId)
                            else -> viewModel.loadQuiz(courseId, chapterId)
                        }
                    }
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
                        onBackToCourse = onBackClick
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
                            onReturnToSummary = { viewModel.returnToSummary() }
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
    onReturnToSummary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF673AB7)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(24.dp)
            ) {
                Text(
                    text = currentQuestion.questionText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val chunks = currentQuestion.options.chunked(2)
        chunks.forEachIndexed { rowIndex, pair ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            onClick = { if (!isAnswerRevealed) onAnswerSelected(actualIndex) }
                        )
                    }
                }
                if (pair.size < 2) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isAnswerRevealed) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "💡 Explication", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.explanation ?: "Pas d'explication disponible.",
                        fontSize = 14.sp,
                        color = Color(0xFF1B5E20),
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showReturnToSummary) {
                OutlinedButton(
                    onClick = onReturnToSummary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF673AB7))
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFF673AB7))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retour au récapitulatif", color = Color(0xFF673AB7))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.width(150.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, if (currentIndex > 0) Color(0xFF673AB7) else Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF673AB7)
                    )
                ) {
                    Text("Précédent", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier.width(150.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
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
    onClick: () -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showPreview = true }, onTap = { onClick() })
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(backgroundColor),
            border = BorderStroke(
                width = if (isSelected || isCorrect || isWrong) 3.dp else 1.dp,
                color = borderColor.copy(alpha = if (isSelected || isCorrect || isWrong) 1f else 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF5E35B1),
                    modifier = Modifier.size(22.dp).align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = id.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
        }

        if (showPreview) {
            Popup(alignment = Alignment.Center, onDismissRequest = { showPreview = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Text(text = text, color = Color.White, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, fontSize = 15.sp)
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
    onBackToCourse: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (isReviewOnly) "Récapitulatif" else "Quiz Terminé", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isReviewOnly) "Score enregistré" else "Ton Score", color = Color.White, fontSize = 14.sp)
                Text(
                    text = "${if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0}%",
                    fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold
                )
                Text("$score / ${questions.size}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "RÉCAPITULATIF", fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            itemsIndexed(questions) { index, question ->
                val userChoice = userAnswers[index]
                val isCorrect = userChoice == question.correctAnswerIndex

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onReviewQuestion(index) },
                    colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Q${index + 1}: ${question.questionText}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "Ta réponse : ${question.options.getOrNull(userChoice ?: -1) ?: "Aucune"}",
                            fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline
                        )
                        if (!isCorrect) {
                            Text(text = "Correct : ${question.options[question.correctAnswerIndex]}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isReviewOnly) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(2.dp, Color(0xFF673AB7))
            ) {
                Text("Recommencer le quiz", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onBackToCourse,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
        ) {
            Text(if (isReviewOnly) "Retour à l'accueil" else "Quitter", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(title: String, currentQuestion: Int, totalQuestions: Int, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
        actions = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = "$currentQuestion/$totalQuestions", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    )
}

@Composable
private fun LoadingState(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Analyse de tes cours...",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF673AB7),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ⭐ Barre de progression horizontale dynamique
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color(0xFF673AB7),
            trackColor = Color(0xFFE1BEE7)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Affichage du pourcentage textuel
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Préparation de ton quiz personnalisé",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray
        )
    }
}

@Composable
private fun ErrorState(msg: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = msg, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))) { Text("Réessayer") }
    }
}