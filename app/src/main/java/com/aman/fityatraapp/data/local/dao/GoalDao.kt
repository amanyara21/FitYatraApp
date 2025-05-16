package com.aman.fityatraapp.data.local.dao

import androidx.room.*
import com.aman.fityatraapp.data.local.model.Goal

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<Goal>)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<Goal>

    @Query("SELECT * FROM goals WHERE goalType = :type LIMIT 1")
    suspend fun getGoalByType(type: String): Goal?

    @Query("DELETE FROM goals")
    suspend fun clearGoals()
}