package com.miage.learnity.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miage.learnity.R
import com.miage.learnity.ui.theme.LearnityTheme
import com.miage.learnity.ui.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: (Double) -> Unit
) {
    val dimensions = rememberResponsiveDimensions()
    val primaryColor = Color(0xFF635BFF)
    val pagerState = rememberPagerState(pageCount = { 4 }) // ✅ Passé à 4 pages
    val scope = rememberCoroutineScope()

    var selectedRedevance by remember { mutableStateOf(1.0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Bienvenue sur Learnity",
                    description = buildAnnotatedString {
                        append("Apprendre n'a jamais eu autant d'impact.\n\nChaque quiz réussi vous rapporte des ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("points") }
                        append(" et soutient une ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("cause concrète") }
                        append(".")
                    },
                    imageRes = R.drawable.icon_learnity,
                    dimensions = dimensions
                )
                1 -> OnboardingPage(
                    title = "Le défi du quotidien",
                    description = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("La régularité est la clé.") }
                        append("\n\nSi vous manquez votre quiz ou faites des erreurs, une ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("'dette virtuelle'") }
                        append(" s'accumule pour booster votre motivation.")
                    },
                    imageRes = R.drawable.les_enfants_page_de_connexion,
                    dimensions = dimensions
                )
                2 -> OnboardingPage(
                    title = "Zéro pression bancaire",
                    description = buildAnnotatedString {
                        append("Cette dette est une ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("jauge morale") }
                        append(", pas un prélèvement.\n\nVous restez ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("libre") }
                        append(" de transformer cet engagement en don réel quand vous le voulez.")
                    },
                    imageRes = R.drawable.solidarite,
                    dimensions = dimensions
                )
                3 -> RedevanceSelectorPage(
                    value = selectedRedevance,
                    onValueChange = { selectedRedevance = it },
                    onConfirm = { onFinished(selectedRedevance) },
                    color = primaryColor,
                    imageRes = R.drawable.icon_learnity,
                    dimensions = dimensions
                )
            }
        }

        OnboardingBottomBar(
            currentPage = pagerState.currentPage,
            pageCount = 4, // ✅ Mis à jour
            primaryColor = primaryColor,
            onNext = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            onPrevious = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            }
        )
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: AnnotatedString, // ✅ Changé en AnnotatedString
    imageRes: Int,
    dimensions: ResponsiveDimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.screenPaddingHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(dimensions.itemSpacing * 2))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(dimensions.itemSpacing))

        Text(
            text = description, // ✅ Affichera le gras et les retours à la ligne
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun RedevanceSelectorPage(
    value: Double,
    onValueChange: (Double) -> Unit,
    onConfirm: () -> Unit,
    color: Color,
    imageRes: Int,
    dimensions: ResponsiveDimensions
) {
    var textValue by remember { mutableStateOf(String.format("%.2f", value).replace(",", ".")) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val isError = textValue.toDoubleOrNull()?.let { it < 1.0 || it > 100.0 } ?: true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.screenPaddingHorizontal)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Règle d'engagement", fontSize = dimensions.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showInfoDialog = true }) {
                // Ici on garde l'icône système info car c'est un bouton d'action UI standard
                Icon(painterResource(R.drawable.icon_learnity), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Fixez votre montant symbolique par jour d'absence. \nRappel : aucun prélèvement n'est effectué.",
            textAlign = TextAlign.Center,
            fontSize = dimensions.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(dimensions.itemSpacing * 1.5f))

        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() || it == '.' }) {
                    textValue = newValue
                    newValue.toDoubleOrNull()?.let { if (it in 1.0..100.0) onValueChange(it) }
                }
            },
            label = { Text("Montant (€)") },
            suffix = { Text("€") },
            isError = isError,
            singleLine = true,
            modifier = Modifier.width(150.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(dimensions.cornerRadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = color, focusedLabelColor = color)
        )

        if (isError) {
            Text("Le montant doit être entre 1€ et 100€", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(Modifier.height(dimensions.itemSpacing))

        Surface(color = color.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Vous pourrez modifier ce montant à tout moment dans votre profil.", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = color, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(dimensions.itemSpacing * 2))

        Button(
            onClick = {
                val finalValue = textValue.toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 } ?: 1.0
                onValueChange(finalValue)
                onConfirm()
            },
            enabled = !isError,
            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
            shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text("Lancer Learnity", fontWeight = FontWeight.Bold, fontSize = dimensions.bodyLarge)
        }
    }

    if (showInfoDialog) {
        RedevanceExplanationDialog(primaryColor = color, dimensions = dimensions, onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    primaryColor: Color,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // RETOUR (Aligné à gauche)
        if (currentPage > 0) {
            TextButton(
                onClick = onPrevious,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text("RETOUR", color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
        }

        // DOTS (Strictement centrés)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { iteration ->
                val color = if (currentPage == iteration) primaryColor else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        // SUIVANT (Aligné à droite)
        if (currentPage < pageCount - 1) {
            TextButton(
                onClick = onNext,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("SUIVANT", color = primaryColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RedevanceExplanationDialog(primaryColor: Color, dimensions: ResponsiveDimensions, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Image(painter = painterResource(R.drawable.icon_learnity), contentDescription = null, modifier = Modifier.size(dimensions.iconSizeLarge)) },
        title = { Text("Engagement solidaire", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = dimensions.titleLarge * 0.8f) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing / 2)) {
                Text("Ce système est symbolique et pédagogique. AUCUNE information bancaire ne sera demandée.", fontWeight = FontWeight.Bold, color = primaryColor, textAlign = TextAlign.Center, fontSize = dimensions.bodyMedium)
                Text("• Erreur : Une fraction de votre redevance est ajoutée (Redevance ÷ Nb questions).", fontSize = dimensions.bodySmall)
                Text("• Absentéisme : Si vous ratez votre Quiz du Jour, le montant total est ajouté.", fontSize = dimensions.bodySmall)
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(4.dp))
                Text("Cette dette est une jauge morale. Vous restez libre de verser ou non ce montant à l'association de votre choix. Learnity ne prélève rien.", style = MaterialTheme.typography.bodySmall, fontSize = dimensions.bodySmall * 0.9f)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = primaryColor), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cornerRadiusMedium)) {
                Text("J'ai compris", fontSize = dimensions.bodyLarge)
            }
        }
    )
}

// --- PREVIEWS CORRIGÉES ---

@Preview(
    name = "Mode Interactif - Flux Complet",
    showBackground = true,
    device = "spec:width=411dp,height=891dp", // Pixel 4
    showSystemUi = true
)
@Composable
fun OnboardingInteractivePreview() {
    // Utilise ton thème personnalisé pour un rendu fidèle
    LearnityTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OnboardingScreen(
                onFinished = { montant ->
                    // Log pour débugger dans l'onglet 'Interactive'
                    println("Onboarding terminé avec : $montant €")
                }
            )
        }
    }
}

@Preview(name = "Page Engagement - Petit écran", widthDp = 320, heightDp = 640, showBackground = true)
@Preview(name = "Page Engagement - Tablette", widthDp = 600, heightDp = 900, showBackground = true)
@Composable
fun RedevancePageStaticPreview() {
    LearnityTheme {
        Surface {
            RedevanceSelectorPage(
                value = 1.0,
                onValueChange = {},
                onConfirm = {},
                color = Color(0xFF635BFF),
                // ✅ Ajout de l'image obligatoire que nous avons définie dans le composant
                imageRes = R.drawable.icon_learnity,
                dimensions = rememberResponsiveDimensions()
            )
        }
    }
}