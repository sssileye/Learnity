package com.miage.learnity.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.miage.learnity.MainActivity
import com.miage.learnity.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Appelé quand un nouveau token est généré (première installation ou refresh).
     * C'est ce token qui permet d'envoyer des messages ciblés à cet appareil.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nouveau Token généré : $token")
        // Note : On l'enregistrera dans Firestore via le UserViewModel
        // ou au démarrage de l'app pour l'associer à l'utilisateur actuel.
    }

    /**
     * Appelé lorsqu'un message est reçu alors que l'application est au premier plan (Foreground).
     * Si l'app est fermée, le système Android gère l'affichage tout seul si c'est une "Notification".
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message reçu de : ${remoteMessage.from}")

        // Analyse du contenu de la notification
        remoteMessage.notification?.let {
            Log.d("FCM", "Contenu de la notif : ${it.body}")
            showNotification(it.title ?: "Learnity", it.body ?: "")
        }
    }

    /**
     * Construit et affiche la notification visuelle sur le téléphone.
     */
    private fun showNotification(title: String, message: String) {
        val channelId = "learnity_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configuration pour Android 8.0+ (Oreo) : Les canaux sont obligatoires
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels Learnity", // Nom visible par l'utilisateur dans les réglages
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les rappels de quiz et d'activité"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action lors du clic sur la notification : Ouvrir l'application
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construction de la notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_learnity) // Assure-toi que cette ressource existe
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Ouvre l'app au clic
            .setAutoCancel(true) // Disparaît quand on clique dessus

        // Affichage (l'ID 0 peut être remplacé par un ID unique si tu veux plusieurs notifs séparées)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}