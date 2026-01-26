package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class Step(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val steps: Int
)
