package com.aman.fityatraapp.data.local.dao

import androidx.room.*
import com.aman.fityatraapp.data.local.model.Meal

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<Meal>): List<Long>

    @Query("SELECT * FROM Meal WHERE mealId = :mealId LIMIT 1")
    suspend fun getMealById(mealId: Int): Meal?

    @Query("DELETE FROM Meal")
    suspend fun clearAllMeals()
}
