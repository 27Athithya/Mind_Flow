package com.example.myapplication.models

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var emoji: String = "🎯", // Default emoji for habits
    var icon: String,
    var frequency: String,
    var reminderTime: String?,
    val completionDates: MutableList<String> = mutableListOf(),
    val createdDate: String
) {
    fun getProgressPercentage(): Float {
        // Calculate progress based on completion dates in the last 7 days
        val today = java.time.LocalDate.now()
        val lastWeek = (0..6).map { today.minusDays(it.toLong()).toString() }
        val completedDays = completionDates.intersect(lastWeek.toSet()).size
        return (completedDays / 7f) * 100f
    }

    fun getWeeklyProgress(): String {
        val today = java.time.LocalDate.now()
        val lastWeek = (0..6).map { today.minusDays(it.toLong()).toString() }
        val completedDays = completionDates.intersect(lastWeek.toSet()).size
        return "$completedDays/7 days completed this week"
    }

    fun isCompletedToday(): Boolean {
        val today = java.time.LocalDate.now().toString()
        return completionDates.contains(today)
    }

    fun markCompleted() {
        val today = java.time.LocalDate.now().toString()
        if (!completionDates.contains(today)) {
            completionDates.add(today)
        }
    }

    fun markIncomplete() {
        val today = java.time.LocalDate.now().toString()
        completionDates.remove(today)
    }

    fun getStreak(): Int {
        if (completionDates.isEmpty()) {
            return 0
        }

        val sortedDates = completionDates.map { java.time.LocalDate.parse(it) }.sortedDescending()

        var streak = 0
        var currentDate = java.time.LocalDate.now()

        for (date in sortedDates) {
            if (date.isEqual(currentDate)) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date.isBefore(currentDate)) {
                // Check if there's a gap - if so, break
                val expectedPreviousDate = currentDate.plusDays(1)
                if (date.isBefore(expectedPreviousDate)) {
                    break
                }
                currentDate = currentDate.minusDays(1)
            }
        }

        return streak
    }
}
