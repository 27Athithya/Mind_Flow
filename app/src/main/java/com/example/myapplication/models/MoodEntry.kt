package com.example.myapplication.models

import java.util.UUID

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val time: String,
    val emoji: String,
    val moodLevel: Int, // 1-5 scale
    val note: String?,
    val tags: List<String> = listOf()
) {
    companion object {
        fun getMoodEmojis() = listOf("😢", "😔", "😐", "😊", "😄")
        fun getMoodColor(level: Int): String {
            return when(level) {
                1 -> "#F48FB1" // Soft pink for very sad
                2 -> "#FFB74D" // Soft orange for sad
                3 -> "#FFF176" // Soft yellow for neutral
                4 -> "#A5D6A7" // Soft light green for happy
                5 -> "#81C784" // Mint green for very happy
                else -> "#9E9E9E"
            }
        }
    }
}
