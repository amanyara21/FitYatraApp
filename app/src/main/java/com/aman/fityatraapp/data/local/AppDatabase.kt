package com.aman.fityatraapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aman.fityatraapp.data.local.dao.*
import com.aman.fityatraapp.data.local.model.*
import com.aman.fityatraapp.utils.Converters


@Database(
    entities = [
        HealthData::class,
        UserData::class,
        Goal::class,
        Meal::class,
        DietPlanEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
    abstract fun mealDao(): MealDao
    abstract fun dietPlanDao(): DietPlanDao
}
