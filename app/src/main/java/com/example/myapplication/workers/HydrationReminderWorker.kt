package com.example.myapplication.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.utils.NotificationHelper
import com.example.myapplication.utils.SharedPrefsManager

class HydrationReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val prefsManager = SharedPrefsManager(applicationContext)

        // Check if reminders are enabled
        if (!prefsManager.areHydrationRemindersEnabled()) {
            return Result.success()
        }

        // Check if user hasn't reached their daily limit
        val currentIntake = prefsManager.getWaterIntakeToday()
        val dailyLimit = prefsManager.getDailyWaterLimit()

        if (currentIntake < dailyLimit) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showHydrationReminder()
        }

        return Result.success()
    }
}
