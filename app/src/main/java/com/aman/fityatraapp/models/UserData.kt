package com.aman.fityatraapp.models

data class UserData(
    val name: String = "",
    val Height: Int = 0,
    val Weight: Float = 0.0f,
    val Preference: String = "",
    val Age: Int = 0,
    val Activity: Double = 0.0,
    val Sex: String = "",
    val Goal: String = "",
    val sleepSchedule: String = "",
    val medicalProblems: String = "",
    var HbA1c: Float = 0.0f,
)
