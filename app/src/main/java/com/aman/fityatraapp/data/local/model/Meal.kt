package com.aman.fityatraapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Meal")
data class Meal(
    @PrimaryKey(autoGenerate = true) val mealId: Int = 0,
    val carbohydrateByDifference: Double,
    val category: String,
    val calorie: Double,
    val foodName: String,
    val meal: String,
    val protein: Double,
    val sugarsTotalIncludingNLEA: Double,
    val totalLipidFat: Double
)
