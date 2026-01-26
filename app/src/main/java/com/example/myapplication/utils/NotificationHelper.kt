package com.example.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import kotlin.random.Random

class NotificationHelper(private val context: Context) {

    companion object {
        const val HYDRATION_CHANNEL_ID = "hydration_reminders"
        const val HYDRATION_NOTIFICATION_ID = 1001

        private val motivationalMessages = listOf(
            "💧 Stay hydrated, you're doing great!",
            "💧 Every sip counts! Keep up the good work!",
            "💧 Your body thanks you for staying hydrated! 🌊",
            "💧 Remember to drink water - your future self will thank you!",
            "💧 Hydration = Energy! Let's keep that momentum going! ⚡",
            "💧 You're crushing your hydration goals today! 💪",
            "💧 Small sips, big impact! Keep hydrated! 🌟",
            "💧 Water: Your secret weapon for feeling amazing! 💫"
        )
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Reminders to drink water throughout the day"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(HYDRATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHydrationReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_hydration", true)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val logIntakeIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = "LOG_WATER_INTAKE"
        }
        val logIntakePendingIntent = PendingIntent.getBroadcast(
            context, 0, logIntakeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get random motivational message
        val randomMessage = motivationalMessages[Random.nextInt(motivationalMessages.size)]

        val builder = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Hydration Reminder")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_add, "Log Glass", logIntakePendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(HYDRATION_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Handle permission denied
            }
        }
    }
}
