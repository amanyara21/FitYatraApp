package com.aman.fityatraapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val Age: Int,
    val Height: Int,
    val Weight: Float,
    val Sex: String,
    val Preference: String,
    val Activity: Double,
    val Goal: String,
    val sleepSchedule: String,
    val medicalProblems: String,
    var HbA1c : Float
)
