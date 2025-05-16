package com.aman.fityatraapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.Converters

@Entity(tableName = "health")
@TypeConverters(Converters::class)
data class HealthData(
    @PrimaryKey val date: Long,
    val stepCount: Int = 0,
    val calorieIntake: Int = 0,
    val calorieBurn: Int = 0,
    val weight: Float = 0.0f,
    val glucoseLevel: Float = 0.0f,
    val exercises: List<ExerciseAdd> = emptyList(),
    val meals: List<MealAdd> = emptyList()
)