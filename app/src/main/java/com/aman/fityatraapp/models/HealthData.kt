package com.aman.fityatraapp.models

import com.aman.fityatraapp.data.api.ExerciseAdd
import java.util.Date

data class HealthData(
    val exercises: MutableList<ExerciseAdd>? = mutableListOf(),
    val meals: MutableList<MealAdd>? = mutableListOf(),
    var stepCount: Int? = 0,
    var calorieIntake: Int? = 0,
    var calorieBurn: Int? = 0,
    var weight: Float? = 0.0f,
    var glucoseLevel: Float? = 0.0f,
    val date: Long = Date().time
)