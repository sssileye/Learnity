package com.miage.learnity.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.miage.learnity.R
import com.miage.learnity.ui.theme.*
import com.miage.learnity.ui.utils.*

@Composable
fun AuthScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    // ðŸŽ¨ Dimensions responsives
    val dimensions = rememberResponsiveDimensions()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPaddingHorizontal)
                .responsiveMaxWidth(dimensions), // âœ… Limite la largeur sur grands Ã©crans
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - taille responsive
            Image(
                painter = painterResource(id = R.drawable.icon_learnity),
                contentDescription = "Logo Learnity",
                modifier = Modifier.size(dimensions.logoSize), // âœ… 100.sdp()
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 2)) // âœ… Spacing adaptatif

            // Titre - typographie responsive
            Text(
                text = "LEARNITY",
                fontSize = dimensions.displayLarge, // âœ… 40.ssp()
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(dimensions.itemSpacing * 4))

            // Bouton Connexion - hauteur responsive
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight), // âœ… 56.sdp()
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge), // âœ… 16.dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF635BFF)
                )
            ) {
                Text(
                    text = "Se connecter",
                    fontSize = dimensions.bodyLarge, // âœ… 16.ssp()
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(dimensions.itemSpacing))

            // Bouton Inscription
            OutlinedButton(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                shape = RoundedCornerShape(dimensions.cornerRadiusLarge),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF635BFF)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
            ) {
                Text(
                    text = "S'inscrire",
                    fontSize = dimensions.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}