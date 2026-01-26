package com.example.myapplication.models

data class User(
    val name: String,
    val email: String,
    val password: String, // Hashed
    val registeredDate: String
)
