package com.miage.learnity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.miage.learnity.ui.navigation.MainScreen
import com.miage.learnity.ui.theme.LearnityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permet d'afficher le contenu sous les barres système (statut/navigation)
        enableEdgeToEdge()

        setContent {
            LearnityTheme {
                // Le conteneur Surface adapte la couleur de fond selon le thème
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialisation du contrôleur de navigation
                    val navController = rememberNavController()

                    // Lancement de ton écran principal
                    MainScreen(navController = navController)
                }
            }
        }
    }
}

// --- SECTION PREVIEW ---
// Cette fonction permet de voir ton interface sans lancer l'émulateur
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    LearnityTheme {
        // Dans une Preview, on crée un NavController "fictif"
        val navController = rememberNavController()
        MainScreen(navController = navController)
    }
}