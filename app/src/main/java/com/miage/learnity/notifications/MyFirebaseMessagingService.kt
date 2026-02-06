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


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nouveau Token généré : $token")

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message reçu de : ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM", "Contenu de la notif : ${it.body}")
            showNotification(it.title ?: "Learnity", it.body ?: "")
        }
    }


    private fun showNotification(title: String, message: String) {
        val channelId = "learnity_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels Learnity",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les rappels de quiz et d'activité"
            }
            notificationManager.createNotificationChannel(channel)
        }


        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_learnity)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}