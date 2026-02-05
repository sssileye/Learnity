package com.miage.learnity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 *
 *  HELP DIALOGS - SYSTÈME DE GUIDE UTILISATEUR
 *
 */

// QUIZ DU JOUR - EXPLICATION
@Composable
fun QuizDuJourHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📚 Quiz du Jour",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    "Le Quiz du Jour (QDJ) est un quiz quotidien de 10 questions qui change chaque jour.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Révision
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF42A5F5).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "MODE RÉVISION",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF42A5F5),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Quiz basé sur les chapitres que tu as DÉJÀ consultés\n" +
                                    "• Questions issues de tes cours étudiés\n" +
                                    "• Idéal pour réviser avant un examen",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Découverte
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7E57C2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "MODE DÉCOUVERTE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E57C2),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Quiz sur des chapitres ALEATOIRES (consultés ou non)\n" +
                                    "• Découvre de nouveaux concepts\n" +
                                    "• Te motive à explorer de nouveaux cours",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Récompenses
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF66BB6A).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "RÉCOMPENSES",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66BB6A),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Max 10 Unity Points (1 par bonne réponse)\n" +
                                    "• Bonus +5 points si sans-faute (10/10)\n" +
                                    "• Winstreak : multiplicateur de points si régulier\n",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Important
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ ATTENTION",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Si tu ne fais pas le QDJ avant minuit, ta dette augmente !\n" +
                                    "• Chaque mauvaise réponse ajoute 0,20€ à ta dette (si X=2€)\n" +
                                    "• Un jour manqué = perte du Winstreak",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Astuce - Clic long
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "ASTUCE",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Réponse trop longue et tronquée ?\n\n" +
                                    "👆 Fais un APPUI LONG sur la réponse pour voir le texte complet dans un pop-up !",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton OK
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("J'ai compris !")
                }
            }
        }
    }
}

// DETTE VIRTUELLE - EXPLICATION
@Composable
fun DetteVirtuelleHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "💰 Dette Virtuelle",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2994A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    "La Dette Virtuelle est un système qui te responsabilise dans ton apprentissage.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comment ça marche
                Text(
                    "COMMENT ÇA MARCHE ?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "À l'inscription, tu choisis un montant \"X\" (ex: 2€). Ce montant représente ton engagement envers l'association que tu soutiens.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quand la dette augmente
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF5252).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "QUAND LA DETTE AUGMENTE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Quiz du Jour non fait avant minuit : +X€\n" +
                                    "Chaque mauvaise réponse au QDJ : +0,10X€\n" +
                                    "-> (Ex: si X=2€, chaque erreur = +0,20€)",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFFF5252)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Comment solder
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF66BB6A).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "COMMENT SOLDER MA DETTE ?",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66BB6A),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "DON RÉEL\n" +
                                    " Fais un don à une association partenaire\n" +
                                    " Montant = ta dette actuelle\n\n",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note importante
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF42A5F5).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "BON À SAVOIR",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF42A5F5),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "La dette est VIRTUELLE (pas d'argent réel prélevé)\n" +
                                    "C'est un miroir de ton engagement\n" +
                                    "Tu peux ajuster \"X\" dans ton profil\n" +
                                    "Plus tu es régulier, moins tu accumules",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton OK
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF2994A)
                    )
                ) {
                    Text("J'ai compris !")
                }
            }
        }
    }
}

// UNITY POINTS - EXPLICATION
@Composable
fun UnityPointsHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⭐ Unity Points",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF42A5F5)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    "Les Unity Points sont la monnaie de la réussite ! Ils récompensent ton travail et ta précision.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Barème de base
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF42A5F5).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "BARÈME DE BASE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF42A5F5),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1 bonne réponse = 1 Unity Point\n\n" +
                                    "Quiz Chapitre (5 questions)\n" +
                                    " -> Max 5 pts\n\n" +
                                    "Quiz du Jour (10 questions)\n" +
                                    " -> Max 10 pts\n\n" +
                                    "Examen Blanc (20 questions)\n" +
                                    " -> Max 20 pts",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bonus Perfect
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFA726).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "BONUS \"PERFECT\"",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sans-faute = Bonus significatif !\n\n" +
                                    "- Quiz Chapitre (5/5) : +3 pts\n" +
                                    "- Quiz du Jour (10/10) : +5 pts\n" +
                                    "- Examen Blanc (20/20) : +10 pts",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFFFA726)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Winstreak
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF66BB6A).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "WINSTREAK (QDJ uniquement)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF66BB6A),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Multiplicateur de régularité :\n\n" +
                                    "- 10 jours consécutifs : x2.0\n" +
                                    "- 20 jours : x3.0\n" +
                                    "- 30+ jours : x4.0 \n\n" +
                                    "⚠️ 1 jour manqué = retour à zéro",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Objectif
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7E57C2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "OBJECTIF",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E57C2),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les UP servent de bouclier : 10 UP = 1 jour d'absence effacé.\n\n" +
                                    "Exemple : Avec 25 UP pour 3 jours d'absence, 20 UP couvrent 2 jours. Seul le 3ème génère une pénalité.\n\n" +
                                    "Ton impact social (Dette Virtuelle) reste lié à tes quiz.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton OK
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A5F5)
                    )
                ) {
                    Text("J'ai compris !")
                }
            }
        }
    }
}

//  TYPES DE QUIZ - COMPARAISON
@Composable
fun TypesQuizHelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎯 Types de Quiz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quiz Chapitre
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF42A5F5).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "QUIZ DE CHAPITRE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF42A5F5),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "5 questions sur UN chapitre spécifique\n" +
                                    "Accessible après avoir consulté le contenu\n" +
                                    "Max 5 Unity Points (+3 bonus si 5/5)\n" +
                                    "Pas d'impact sur la dette\n" +
                                    "Peut être refait autant de fois que voulu",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quiz du Jour
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7E57C2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "QUIZ DU JOUR",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7E57C2),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "10 questions mélangées (différents chapitres)\n" +
                                    "- Change chaque jour\n" +
                                    "- Max 10 Unity Points (+5 bonus si 10/10)\n" +
                                    "- Multiplicateur Winstreak (jusqu'à x2.0)\n" +
                                    "- Impact sur la dette si raté ou non fait\n" +
                                    "- Mode Révision OU Mode Découverte",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Examen Blanc
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFA726).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "EXAMEN BLANC",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "20 questions sur TOUT le cours\n" +
                                    "Simule un vrai examen\n" +
                                    "Max 20 Unity Points (+10 bonus si 20/20)\n" +
                                    "-> Pas d'impact sur la dette\n" +
                                    "Plus difficile mais plus de points",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton OK
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("J'ai compris !")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 📱 DIALOG GÉNÉRAL - ACCUEIL
// ═══════════════════════════════════════════════════════════════
@Composable
fun HomeScreenHelpDialog(
    onQuizDuJourClick: () -> Unit,
    onDetteClick: () -> Unit,
    onUnityPointsClick: () -> Unit,
    onTypesQuizClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Besoin d'aide ?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Boutons d'aide
                OutlinedButton(
                    onClick = onQuizDuJourClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📚 Le Quiz du Jour")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDetteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💰 La Dette Virtuelle")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onUnityPointsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⭐ Les Unity Points")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onTypesQuizClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎯 Les Types de Quiz")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton Fermer
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}