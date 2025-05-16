package com.aman.fityatraapp.data.local.dao

import androidx.room.*
import com.aman.fityatraapp.data.local.model.DietPlanEntity

@Dao
interface DietPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietPlan(dietPlans: List<DietPlanEntity>)

    @Query("SELECT * FROM DietPlan WHERE day = :day AND mealTime = :mealTime LIMIT 1")
    suspend fun getDietMealFor(day: String, mealTime: String): DietPlanEntity?

    @Query("DELETE FROM DietPlan")
    suspend fun clearDietPlan()
}