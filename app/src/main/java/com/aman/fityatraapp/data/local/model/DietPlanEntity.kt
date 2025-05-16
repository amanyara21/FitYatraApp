package com.aman.fityatraapp.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "DietPlan",
    foreignKeys = [
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["mealId"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DietPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val dietId: Int = 0,
    val day: String,
    val mealTime: String,
    val mealId: Int
)
