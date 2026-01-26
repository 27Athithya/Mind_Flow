package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.models.Habit
import com.example.myapplication.models.MoodEntry
import com.example.myapplication.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SharedPrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wellnesshub_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Background executor for heavy operations
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Cache frequently accessed data to avoid repeated JSON parsing
    @Volatile
    private var cachedHabits: List<Habit>? = null
    @Volatile
    private var cachedMoodEntries: List<MoodEntry>? = null
    @Volatile
    private var cacheTime = 0L
    private val CACHE_EXPIRY_MS = 30000L // 30 seconds cache

    // Authentication methods - optimized for main thread safety
    fun saveUser(user: User) {
        try {
            val hashedPassword = hashPassword(user.password)
            val userWithHashedPassword = user.copy(password = hashedPassword)
            val userJson = gson.toJson(userWithHashedPassword)

            // Use apply() instead of commit() to avoid blocking
            prefs.edit()
                .putString("user_data", userJson)
                .putBoolean("is_logged_in", true)
                .apply()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    fun getUser(): User? {
        return try {
            val userData = prefs.getString("user_data", null)
            if (userData != null) {
                gson.fromJson(userData, User::class.java)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return try {
            prefs.getBoolean("is_logged_in", false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun setRememberMe(remember: Boolean) {
        try {
            prefs.edit().putBoolean("remember_me", remember).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shouldRememberUser(): Boolean {
        return try {
            prefs.getBoolean("remember_me", false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun logout() {
        try {
            // Clear cache first
            cachedHabits = null
            cachedMoodEntries = null
            cacheTime = 0L

            prefs.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun validateLoginAsync(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val user = getUser()
                user != null && user.email == email && user.password == hashPassword(password)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun validateLogin(email: String, password: String): Boolean {
        return try {
            val user = getUser()
            user != null && user.email == email && user.password == hashPassword(password)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Optimized Habits methods with better caching and error handling
    fun saveHabits(habits: List<Habit>) {
        try {
            cachedHabits = habits
            cacheTime = System.currentTimeMillis()

            // Save to SharedPrefs in background to avoid blocking
            backgroundExecutor.execute {
                try {
                    val habitsJson = gson.toJson(habits)
                    prefs.edit().putString("habits", habitsJson).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHabits(): List<Habit> {
        return try {
            // Return cached data if still valid
            if (cachedHabits != null && System.currentTimeMillis() - cacheTime < CACHE_EXPIRY_MS) {
                return cachedHabits!!
            }

            val habitsJson = prefs.getString("habits", null)
            val habits = if (habitsJson != null && habitsJson.isNotEmpty()) {
                try {
                    val type = object : TypeToken<List<Habit>>() {}.type
                    gson.fromJson<List<Habit>>(habitsJson, type) ?: emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }

            // Update cache
            cachedHabits = habits
            cacheTime = System.currentTimeMillis()
            habits
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getHabitsAsync(): List<Habit> {
        return withContext(Dispatchers.IO) {
            getHabits()
        }
    }

    fun addHabit(habit: Habit) {
        try {
            val habits = getHabits().toMutableList()
            habits.add(habit)
            saveHabits(habits)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateHabit(updatedHabit: Habit) {
        try {
            val habits = getHabits().toMutableList()
            val index = habits.indexOfFirst { it.id == updatedHabit.id }
            if (index != -1) {
                habits[index] = updatedHabit
                saveHabits(habits)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteHabit(habitId: String) {
        try {
            val habits = getHabits().toMutableList()
            habits.removeAll { it.id == habitId }
            saveHabits(habits)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Optimized Mood entries methods with better error handling
    fun saveMoodEntries(moodEntries: List<MoodEntry>) {
        try {
            cachedMoodEntries = moodEntries
            cacheTime = System.currentTimeMillis()

            // Save to SharedPrefs in background
            backgroundExecutor.execute {
                try {
                    val moodEntriesJson = gson.toJson(moodEntries)
                    prefs.edit().putString("mood_entries", moodEntriesJson).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMoodEntries(): List<MoodEntry> {
        return try {
            // Return cached data if still valid
            if (cachedMoodEntries != null && System.currentTimeMillis() - cacheTime < CACHE_EXPIRY_MS) {
                return cachedMoodEntries!!
            }

            val moodEntriesJson = prefs.getString("mood_entries", null)
            val moodEntries = if (moodEntriesJson != null && moodEntriesJson.isNotEmpty()) {
                try {
                    val type = object : TypeToken<List<MoodEntry>>() {}.type
                    gson.fromJson<List<MoodEntry>>(moodEntriesJson, type) ?: emptyList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else {
                emptyList()
            }

            // Update cache
            cachedMoodEntries = moodEntries
            cacheTime = System.currentTimeMillis()
            moodEntries
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMoodEntriesAsync(): List<MoodEntry> {
        return withContext(Dispatchers.IO) {
            getMoodEntries()
        }
    }

    fun addMoodEntry(moodEntry: MoodEntry) {
        try {
            val moodEntries = getMoodEntries().toMutableList()
            moodEntries.add(0, moodEntry) // Add to beginning for newest first
            saveMoodEntries(moodEntries)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteMoodEntry(entryId: String) {
        try {
            val moodEntries = getMoodEntries().toMutableList()
            moodEntries.removeAll { it.id == entryId }
            saveMoodEntries(moodEntries)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Hydration settings - optimized
    fun setDailyWaterLimit(ml: Int) {
        try {
            prefs.edit().putInt("daily_water_limit", ml).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDailyWaterLimit(): Int {
        return try {
            prefs.getInt("daily_water_limit", 2000) // Default 2000ml (2 liters)
        } catch (e: Exception) {
            e.printStackTrace()
            2000
        }
    }

    fun setWaterIntakeToday(ml: Int) {
        try {
            val today = java.time.LocalDate.now().toString()
            prefs.edit().putInt("water_intake_$today", ml).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getWaterIntakeToday(): Int {
        return try {
            val today = java.time.LocalDate.now().toString()
            prefs.getInt("water_intake_$today", 0)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun addWaterIntake(ml: Int = 250) { // Default 250ml per glass
        try {
            val current = getWaterIntakeToday()
            setWaterIntakeToday(current + ml)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getWaterIntakeForLast7Days(): List<Pair<String, Int>> {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            val result = mutableListOf<Pair<String, Int>>()

            for (i in 6 downTo 0) {
                calendar.time = java.util.Date()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
                val date = dateFormat.format(calendar.time)
                val intake = prefs.getInt("water_intake_$date", 0)
                result.add(Pair(date, intake))
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Notification settings
    fun setHydrationRemindersEnabled(enabled: Boolean) {
        try {
            prefs.edit().putBoolean("hydration_reminders_enabled", enabled).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun areHydrationRemindersEnabled(): Boolean {
        return try {
            prefs.getBoolean("hydration_reminders_enabled", true)
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    fun setReminderInterval(seconds: Long) {
        try {
            prefs.edit().putLong("reminder_interval", seconds).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getReminderInterval(): Long {
        return try {
            prefs.getLong("reminder_interval", 30) // Default 30 seconds for testing
        } catch (e: Exception) {
            e.printStackTrace()
            30
        }
    }

    // Theme settings
    fun setDarkModeEnabled(enabled: Boolean) {
        try {
            prefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDarkModeEnabled(): Boolean {
        return try {
            prefs.getBoolean("dark_mode_enabled", false)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Optimized step counter methods
    fun saveStepCount(steps: Int) {
        try {
            val today = java.time.LocalDate.now().toString()
            // Use background thread for step count saving to avoid blocking sensor events
            backgroundExecutor.execute {
                try {
                    prefs.edit().putInt("step_count_$today", steps).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStepCountToday(): Int {
        return try {
            val today = java.time.LocalDate.now().toString()
            prefs.getInt("step_count_$today", 0)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun setStepGoal(goal: Int) {
        try {
            prefs.edit().putInt("step_goal", goal).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStepGoal(): Int {
        return try {
            prefs.getInt("step_goal", 10000) // Default 10,000 steps
        } catch (e: Exception) {
            e.printStackTrace()
            10000
        }
    }

    fun getStepCountForLast7Days(): List<Pair<String, Int>> {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            val result = mutableListOf<Pair<String, Int>>()

            for (i in 6 downTo 0) {
                calendar.time = java.util.Date()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -i)
                val date = dateFormat.format(calendar.time)
                val stepCount = prefs.getInt("step_count_$date", 0)
                result.add(Pair(date, stepCount))
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Cache management methods
    fun clearCache() {
        try {
            cachedHabits = null
            cachedMoodEntries = null
            cacheTime = 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun invalidateCache() {
        clearCache()
    }

    // Cleanup method to prevent memory leaks
    fun cleanup() {
        try {
            clearCache()
            backgroundExecutor.shutdown()
            if (!backgroundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            backgroundExecutor.shutdownNow()
        }
    }

    // Helper methods
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            password // Fallback to plain password if hashing fails
        }
    }

    fun getTodaysHabitCompletion(): Float {
        return try {
            val habits = getHabits()
            if (habits.isEmpty()) return 0f

            val completedToday = habits.count { it.isCompletedToday() }
            (completedToday.toFloat() / habits.size) * 100f
        } catch (e: Exception) {
            e.printStackTrace()
            0f
        }
    }

    fun getTodaysMoodEntry(): MoodEntry? {
        return try {
            val today = java.time.LocalDate.now().toString()
            getMoodEntries().find { it.date == today }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
