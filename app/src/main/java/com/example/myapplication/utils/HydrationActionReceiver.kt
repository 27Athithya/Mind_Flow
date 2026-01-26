package com.example.myapplication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HydrationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)

        when (intent.action) {
            "android.intent.action.BOOT_COMPLETED" -> {
                // Reschedule reminders on boot
                val prefsManager = SharedPrefsManager(context)
                if (prefsManager.areHydrationRemindersEnabled()) {
                    val interval = prefsManager.getReminderInterval()
                    val scheduler = HydrationScheduler(context)
                    scheduler.scheduleHydrationReminders(interval)
                }
            }
            "LOG_WATER_INTAKE" -> {
                val prefsManager = SharedPrefsManager(context)
                prefsManager.addWaterIntake(250) // Log 250ml

                // Cancel the notification
                val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
                notificationManager.cancel(NotificationHelper.HYDRATION_NOTIFICATION_ID)
            }
            else -> {
                // This is the alarm broadcast
                notificationHelper.showHydrationReminder()

                // Reschedule the alarm
                val prefsManager = SharedPrefsManager(context)
                val interval = prefsManager.getReminderInterval()
                val scheduler = HydrationScheduler(context)
                scheduler.scheduleHydrationReminders(interval)
            }
        }
    }
}
