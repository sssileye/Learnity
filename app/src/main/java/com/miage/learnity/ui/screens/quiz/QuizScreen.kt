
package com.miage.learnity.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.mock.MockData
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    viewModel: QuizViewModel = viewModel(),
    onQuizComplete: (score: Int, total: Int) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val quiz by viewModel.quiz.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // États locaux
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var isAnswerRevealed by remember { mutableStateOf(false) }
    var showFinalResult by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(180) }
    var totalTime by remember { mutableStateOf(180) }

    // Charger le quiz
    LaunchedEffect(courseId, chapterId) {
        viewModel.loadQuiz(courseId, chapterId)
    }

    // Timer - démarre quand le quiz est chargé
    LaunchedEffect(quiz) {
        if (quiz != null) {
            val quizTime = quiz!!.timeLimit ?: 180
            timeRemaining = quizTime
            totalTime = quizTime
            while (timeRemaining > 0 && !showFinalResult) {
                delay(1000L)
                timeRemaining--
            }
            if (timeRemaining == 0 && !showFinalResult) {
                showFinalResult = true
            }
        }
    }

    Scaffold(
        topBar = {
            if (quiz != null && !showFinalResult) {
                QuizTopBar(
                    title = quiz!!.title,
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = quiz!!.questions.size,
                    timeRemaining = timeRemaining,
                    totalTime = totalTime,
                    onBackClick = onBackClick
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingState()
                error != null -> ErrorState(error ?: "Erreur", { viewModel.loadQuiz(courseId, chapterId) })
                quiz != null -> {
                    if (showFinalResult) {
                        FinalResultContent(
                            score = selectedAnswers.count { (qIndex, selectedOption) ->
                                quiz!!.questions.getOrNull(qIndex)?.correctAnswerIndex == selectedOption
                            },
                            total = quiz!!.questions.size,
                            onRetry = {
                                currentQuestionIndex = 0
                                selectedAnswers = mapOf()
                                isAnswerRevealed = false
                                showFinalResult = false
                                val quizTime = quiz!!.timeLimit ?: 180
                                timeRemaining = quizTime
                                totalTime = quizTime
                            },
                            onBackToCourse = onBackClick
                        )
                    } else {
                        val currentQuestion = quiz!!.questions.getOrNull(currentQuestionIndex)

                        if (currentQuestion != null) {
                            QuizContent(
                                quiz = quiz!!,
                                currentQuestion = currentQuestion,
                                currentQuestionIndex = currentQuestionIndex,
                                selectedAnswers = selectedAnswers,
                                isAnswerRevealed = isAnswerRevealed,
                                onAnswerSelected = { optionIndex ->
                                    if (!isAnswerRevealed) {
                                        selectedAnswers = selectedAnswers + (currentQuestionIndex to optionIndex)
                                    }
                                },
                                onValidate = { isAnswerRevealed = true },
                                onNext = {
                                    if (currentQuestionIndex < quiz!!.questions.size - 1) {
                                        currentQuestionIndex++
                                        isAnswerRevealed = false
                                    } else {
                                        showFinalResult = true
                                    }
                                },
                                onPrevious = {
                                    if (currentQuestionIndex > 0) {
                                        currentQuestionIndex--
                                        isAnswerRevealed = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * TopBar du Quiz avec barre de progression timer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(
    title: String,
    currentQuestion: Int,
    totalQuestions: Int,
    timeRemaining: Int,
    totalTime: Int,
    onBackClick: () -> Unit
) {
    val progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 0f

    // Orange par défaut, rouge quand moins de 30 secondes
    val progressColor = if (timeRemaining <= 30) Color.Red else Color(0xFFFF9800)

    Column {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontSize = 16.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour au chapitre"
                    )
                }
            },
            actions = {
                // Badge question
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "$currentQuestion/$totalQuestions",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Barre de progression = Timer (orange)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun QuizContent(
    quiz: com.miage.learnity.data.Quiz,
    currentQuestion: com.miage.learnity.data.Question,
    currentQuestionIndex: Int,
    selectedAnswers: Map<Int, Int>,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onValidate: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zone Bleue : Question
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(24.dp)
            ) {
                Text(
                    currentQuestion.questionText,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grille 2x2 des réponses
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            currentQuestion.options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOptions.forEachIndexed { colIndex, option ->
                        val optionIndex = rowIndex * 2 + colIndex
                        QuizOptionCard(
                            text = option,
                            id = optionIndex + 1,
                            selectedId = selectedAnswers[currentQuestionIndex]?.plus(1),
                            isCorrect = isAnswerRevealed && currentQuestion.correctAnswerIndex == optionIndex,
                            isWrong = isAnswerRevealed &&
                                    selectedAnswers[currentQuestionIndex] == optionIndex &&
                                    currentQuestion.correctAnswerIndex != optionIndex,
                            onClick = { onAnswerSelected(optionIndex) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Zone de correction
        if (isAnswerRevealed) {
            val isCorrect = selectedAnswers[currentQuestionIndex] == currentQuestion.correctAnswerIndex
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isCorrect) "✅ Bonne réponse !" else "❌ Mauvaise réponse",
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentQuestion.explanation ?: "Pas d'explication",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boutons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentQuestionIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Précédent", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (!isAnswerRevealed) onValidate() else onNext()
                },
                enabled = selectedAnswers[currentQuestionIndex] != null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = when {
                        !isAnswerRevealed -> "Valider"
                        currentQuestionIndex == quiz.questions.size - 1 -> "Terminer"
                        else -> "Suivant"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FinalResultContent(
    score: Int,
    total: Int,
    onRetry: () -> Unit,
    onBackToCourse: () -> Unit
) {
    val percentage = (score.toFloat() / total * 100).toInt()
    val isPassed = percentage >= 50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isPassed) "🎉 Bravo !" else "💪 Courage !",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Score", color = Color.White.copy(0.9f), fontSize = 16.sp)
                Text("$percentage%", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
                Text("$score/$total réponses correctes", color = Color.White.copy(0.9f), fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackToCourse,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Continuer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Refaire le quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❌", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(message)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Réessayer") }
        }
    }
}

@Composable
fun QuizOptionCard(
    text: String,
    id: Int,
    selectedId: Int?,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = id == selectedId
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
        else -> Color.LightGray
    }

    Box(modifier = modifier.height(110.dp)) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(backgroundColor),
            border = if (isSelected || isCorrect || isWrong) BorderStroke(2.dp, borderColor) else null
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(20.dp),
            shape = CircleShape,
            color = if (isSelected || isCorrect || isWrong) borderColor else Color(0xFF9E9E9E)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(id.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================
// PREVIEWS
// ============================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuizContentPreview() {
    val quiz = MockData.getQuiz("quiz_ec_chap1")!!

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var isAnswerRevealed by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(120) }

    Column {
        QuizTopBar(
            title = quiz.title,
            currentQuestion = currentQuestionIndex + 1,
            totalQuestions = quiz.questions.size,
            timeRemaining = timeRemaining,
            totalTime = 180,
            onBackClick = {}
        )

        QuizContent(
            quiz = quiz,
            currentQuestion = quiz.questions[currentQuestionIndex],
            currentQuestionIndex = currentQuestionIndex,
            selectedAnswers = selectedAnswers,
            isAnswerRevealed = isAnswerRevealed,
            onAnswerSelected = { optionIndex ->
                if (!isAnswerRevealed) {
                    selectedAnswers = selectedAnswers + (currentQuestionIndex to optionIndex)
                }
            },
            onValidate = { isAnswerRevealed = true },
            onNext = {
                if (currentQuestionIndex < quiz.questions.size - 1) {
                    currentQuestionIndex++
                    isAnswerRevealed = false
                }
            },
            onPrevious = {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--
                    isAnswerRevealed = false
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FinalResultPreview() {
    FinalResultContent(
        score = 2,
        total = 3,
        onRetry = {},
        onBackToCourse = {}
    )
}