package com.miage.learnity.ui.screens.quiz

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question
import com.miage.learnity.model.PointsManager
import com.miage.learnity.ui.screens.UserViewModel
import com.miage.learnity.ui.theme.successColors
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
    val isLoading by viewModel.isLoading.collectAsState()
    val userUiState by userViewModel.uiState.collectAsState()

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
                    title = if (chapterId.contains("DAILY")) "Quiz du Jour" else "Quiz Classique",
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = questions.size,
                    onBackClick = onBackClick
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> LoadingState()
                isQuizFinished -> {
                    FinalResultContent(
                        score = score,
                        total = questions.size,
                        onRetry = { viewModel.resetQuiz() },
                        onBack = onBackClick
                    )
                }
                else -> {
                    questions.getOrNull(currentQuestionIndex)?.let { question ->
                        QuizContent(
                            currentQuestion = question,
                            currentIndex = currentQuestionIndex,
                            totalQuestions = questions.size,
                            userAnswerIndex = userAnswers[currentQuestionIndex],
                            isAnswerRevealed = isAnswerRevealed,
                            onAnswerSelected = { viewModel.selectAnswer(it) },
                            onValidate = { viewModel.validateAnswer() },
                            onNext = { viewModel.nextQuestion() },
                            onPrevious = { viewModel.previousQuestion() },
                            dimensions = dimensions
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizContent(
    currentQuestion: Question,
    currentIndex: Int,
    totalQuestions: Int,
    userAnswerIndex: Int?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onValidate: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    dimensions: ResponsiveDimensions
) {
    //
    Box(modifier = Modifier.fillMaxSize()) {
        // --- ZONE 1 : CONTENU SCROLLABLE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp) // ✅ Empêche le chevauchement avec les boutons
                .verticalScroll(rememberScrollState())
                .padding(dimensions.screenPaddingHorizontal)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalQuestions },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(20.dp)) {
                    Text(text = currentQuestion.questionText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GRILLE DE RÉPONSES (Fixed Rows pour éviter les bugs de clic)
            currentQuestion.options.chunked(2).forEachIndexed { rowIndex, pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEachIndexed { columnIndex, optionText ->
                        val actualIndex = rowIndex * 2 + columnIndex
                        QuizOptionCard(
                            modifier = Modifier.weight(1f), // ✅ Distribution égale pour clic précis
                            text = optionText,
                            id = actualIndex + 1,
                            isSelected = userAnswerIndex == actualIndex,
                            isCorrect = isAnswerRevealed && actualIndex == currentQuestion.correctAnswerIndex,
                            isWrong = isAnswerRevealed && userAnswerIndex == actualIndex && actualIndex != currentQuestion.correctAnswerIndex,
                            onClick = { if (!isAnswerRevealed) onAnswerSelected(actualIndex) }
                        )
                    }
                    if (pair.size < 2) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            if (isAnswerRevealed) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Text(text = "💡 ${currentQuestion.explanation}", modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }
        }

        // --- ZONE 2 : NAVIGATION FIXÉE EN BAS ---
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shadowElevation = 12.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Précédent")
                }
                Button(
                    onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
                    enabled = userAnswerIndex != null,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(if (!isAnswerRevealed) "Valider" else if (currentIndex == totalQuestions - 1) "Terminer" else "Suivant")
                }
            }
        }
    }
}

// --- COMPOSANTS INTERNES ---

@Composable
fun QuizOptionCard(
    modifier: Modifier = Modifier,
    text: String,
    id: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val color = when {
        isCorrect -> Color(0xFFC8E6C9)
        isWrong -> Color(0xFFFFCDD2)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.White
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }, // ✅ Le clic est maintenant prioritaire sur le scroll
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(id.toString(), color = Color.White, fontSize = 12.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTopBar(title: String, currentQuestion: Int, totalQuestions: Int, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } },
        actions = { Text("$currentQuestion / $totalQuestions", Modifier.padding(end = 16.dp), fontWeight = FontWeight.Bold) }
    )
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FinalResultContent(score: Int, total: Int, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Félicitations !", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Score final : $score / $total", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(40.dp))
        Button(onRetry, Modifier.fillMaxWidth()) { Text("Recommencer") }
        TextButton(onBack) { Text("Quitter") }
    }
}