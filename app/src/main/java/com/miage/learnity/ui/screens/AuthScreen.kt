package com.miage.learnity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.miage.learnity.R
import androidx.compose.ui.tooling.preview.Preview
import com.miage.learnity.ui.theme.*

// Couleurs de la charte graphique
val BackgroundColor = Color(0xFFF5F5F5)
val PrimaryPurple = Color(0xFF9E5ECE)
val GradientBlue1 = Color(0xFF4E54C8)
val GradientBlue2 = Color(0xFF8F94FB)
val GradientOrange1 = Color(0xFFFF4B2B)
val GradientOrange2 = Color(0xFFFF8C37)
val TextDark = Color(0xFF1A1A2E)
val TextGray = Color(0xFF4A4A4A)
@Composable
fun AuthScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Logo section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_learnity),
                        contentDescription = "Logo Learnity",
                        modifier = Modifier.size(130.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Titre "LEARNITY"
                Text(
                    text = "LEARNITY",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slogan
                Text(
                    text = "Réviser pour soi, donner pour les autres",
                    fontSize = 16.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }

            // Illustration
            Image(
                painter = painterResource(R.drawable.pageauth),
                contentDescription = "Illustration personnages",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Fit
            )

            // Boutons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bouton "Se connecter" (dégradé bleu)
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientBlue1, GradientBlue2)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Se connecter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Bouton "S'inscrire" (dégradé orange)
                Button(
                    onClick = onSignupClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(GradientOrange1, GradientOrange2)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "S'inscrire",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onLoginClick = {},
        onSignupClick = {}
    )
}