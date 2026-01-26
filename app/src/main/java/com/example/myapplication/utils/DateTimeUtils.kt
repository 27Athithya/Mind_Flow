package com.example.myapplication.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    fun getCurrentDate(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun getCurrentTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    fun getGreeting(): String {
        val hour = LocalDateTime.now().hour
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    fun formatDateForDisplay(date: String): String {
        return try {
            val localDate = java.time.LocalDate.parse(date)
            localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            date
        }
    }

    fun formatTimeForDisplay(time: String): String {
        return try {
            val localTime = java.time.LocalTime.parse(time)
            localTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e: Exception) {
            time
        }
    }
}
