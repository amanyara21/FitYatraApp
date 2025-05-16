package com.aman.fityatraapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val goalType: String,
    val goalValue: Int
)
