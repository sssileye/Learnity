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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miage.learnity.data.Question
import com.miage.learnity.data.Quiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    courseId: String,
    chapterId: String,
    viewModel: QuizViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val quiz by viewModel.questions.collectAsState() // Utilise la liste de questions du ViewModel
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizFinished by viewModel.isQuizFinished.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // États locaux pour l'UI
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerRevealed by remember { mutableStateOf(false) }

    // Charger le quiz au démarrage
    LaunchedEffect(courseId, chapterId) {
        viewModel.loadQuiz(courseId, chapterId)
    }

    Scaffold(
        topBar = {
            if (quiz.isNotEmpty() && !isQuizFinished) {
                QuizTopBar(
                    title = "Quiz de chapitre",
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = quiz.size,
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
                quiz.isEmpty() && !isLoading -> ErrorState("Aucun quiz trouvé", { viewModel.loadQuiz(courseId, chapterId) })
                isQuizFinished -> {
                    FinalResultContent(
                        score = score,
                        total = quiz.size,
                        onRetry = {
                            viewModel.resetQuiz()
                            isAnswerRevealed = false
                            selectedOptionIndex = null
                        },
                        onBackToCourse = onBackClick
                    )
                }
                else -> {
                    val currentQuestion = quiz.getOrNull(currentQuestionIndex)
                    if (currentQuestion != null) {
                        QuizContent(
                            currentQuestion = currentQuestion,
                            currentQuestionIndex = currentQuestionIndex,
                            totalQuestions = quiz.size,
                            selectedOptionIndex = selectedOptionIndex,
                            isAnswerRevealed = isAnswerRevealed,
                            onAnswerSelected = { index ->
                                if (!isAnswerRevealed) selectedOptionIndex = index
                            },
                            onValidate = { isAnswerRevealed = true },
                            onNext = {
                                viewModel.onAnswerSelected(selectedOptionIndex ?: -1)
                                isAnswerRevealed = false
                                selectedOptionIndex = null
                            }
                        )
                    }
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
private fun QuizContent(
    currentQuestion: Question,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    selectedOptionIndex: Int?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onValidate: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options
        currentQuestion.options.forEachIndexed { index, option ->
            QuizOptionCard(
                text = option,
                id = index + 1,
                isSelected = selectedOptionIndex == index,
                isCorrect = isAnswerRevealed && currentQuestion.correctAnswerIndex == index,
                isWrong = isAnswerRevealed && selectedOptionIndex == index && currentQuestion.correctAnswerIndex != index,
                onClick = { onAnswerSelected(index) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (isAnswerRevealed) {
            val isCorrect = selectedOptionIndex == currentQuestion.correctAnswerIndex
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (isCorrect) "✅ Bonne réponse !" else "❌ Mauvaise réponse",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Text(currentQuestion.explanation ?: "Pas d'explication", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { if (!isAnswerRevealed) onValidate() else onNext() },
            enabled = selectedOptionIndex != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(if (!isAnswerRevealed) "Valider" else if (currentQuestionIndex == totalQuestions - 1) "Terminer" else "Suivant")
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
    modifier: Modifier = Modifier
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
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(backgroundColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = borderColor.copy(alpha = 0.2f), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(id.toString(), fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FinalResultContent(score: Int, total: Int, onRetry: () -> Unit, onBackToCourse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quiz Terminé", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)))).padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ton Score", color = Color.White)
                Text("${(score.toFloat()/total*100).toInt()}%", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$score / $total", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackToCourse, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Continuer") }
        TextButton(onClick = onRetry) { Text("Recommencer") }
    }
}

@Composable private fun LoadingState() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
@Composable private fun ErrorState(msg: String, onRetry: () -> Unit) = Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(msg); Button(onClick = onRetry) { Text("Réessayer") } }