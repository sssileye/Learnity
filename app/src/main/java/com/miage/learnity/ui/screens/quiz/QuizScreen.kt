package com.miage.learnity.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    viewModel: QuizViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val isAnswerRevealed by viewModel.isCurrentAnswerRevealed.collectAsState()
    val maxIndexReached by viewModel.maxIndexReached.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(courseId, chapterId) {
        viewModel.loadQuiz(courseId, chapterId)
    }

    Scaffold(
        topBar = {
            if (questions.isNotEmpty() && !isQuizFinished) {
                QuizTopBar(
                    title = "Quiz de chapitre",
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
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

                questions.isEmpty() && !isLoading -> ErrorState(
                    msg = "Aucun quiz trouvé",
                    onRetry = { viewModel.loadQuiz(courseId, chapterId) }
                )

                isQuizFinished -> FinalResultContent(
                    questions = questions,
                    userAnswers = userAnswers,
                    score = score,
                    onReviewQuestion = { index -> viewModel.goToQuestionForReview(index) },
                    onRetry = { viewModel.resetQuiz() },
                    onBackToCourse = onBackClick
                )

                else -> {
                    val currentQuestion = questions.getOrNull(currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuizContent(
                            currentQuestion = currentQuestion,
                            currentIndex = currentQuestionIndex,
                            totalQuestions = questions.size,
                            userAnswerIndex = userAnswers[currentQuestionIndex],
                            isAnswerRevealed = isAnswerRevealed,
                            // Le bouton retour apparaît si on a déjà fini le quiz (maxIndex >= size)
                            showReturnToSummary = maxIndexReached >= questions.size,
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
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        currentQuestion.options.forEachIndexed { index, option ->
            QuizOptionCard(
                text = option,
                id = index + 1,
                isSelected = userAnswerIndex == index,
                isCorrect = isAnswerRevealed && index == currentQuestion.correctAnswerIndex,
                isWrong = isAnswerRevealed && userAnswerIndex == index && index != currentQuestion.correctAnswerIndex,
                onClick = { if (!isAnswerRevealed) onAnswerSelected(index) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isAnswerRevealed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Explication",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentQuestion.explanation ?: "Pas d'explication disponible.",
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showReturnToSummary) {
                OutlinedButton(
                    onClick = onReturnToSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retour au récapitulatif")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0
                ) {
                    Text("Précédent")
                }

                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier
                        .width(150.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = when {
                            !isAnswerRevealed -> "Valider"
                            currentIndex == totalQuestions - 1 && !showReturnToSummary -> "Terminer"
                            else -> "Suivant"
                        }
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
    onReviewQuestion: (Int) -> Unit, // Rebranché ici
    onRetry: () -> Unit,
    onBackToCourse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Terminé",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2))))
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ton Score", color = Color.White, fontSize = 14.sp)
                Text(
                    text = "${(score.toFloat() / questions.size * 100).toInt()}%",
                    fontSize = 36.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text("$score / ${questions.size}", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "RÉCAPITULATIF (Clique pour les détails)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            questions.forEachIndexed { index, question ->
                val userChoice = userAnswers[index]
                val isCorrect = userChoice == question.correctAnswerIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReviewQuestion(index) }, // Action de review
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Q${index + 1}: ${question.questionText}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Ta réponse : ${question.options.getOrNull(userChoice ?: -1) ?: "Aucune"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Correct : ${question.options[question.correctAnswerIndex]}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Recommencer le quiz")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackToCourse,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Retour au cours")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(
    title: String,
    currentQuestion: Int,
    totalQuestions: Int,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "$currentQuestion/$totalQuestions",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(backgroundColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = borderColor.copy(alpha = 0.2f),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = id.toString(), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    msg: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = msg,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}