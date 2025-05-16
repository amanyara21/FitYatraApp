package com.aman.fityatraapp.repository

import com.aman.fityatraapp.data.local.dao.GoalDao
import com.aman.fityatraapp.data.local.model.Goal
import javax.inject.Inject

class GoalRepository @Inject constructor(private val goalDao: GoalDao) {

    suspend fun insertOrUpdateGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun insertDefaultGoals() {
        val defaultGoals = listOf(
            Goal("step_count", 6000),
            Goal("calorie_intake", 3000),
            Goal("calorie_burn", 3000)
        )
        goalDao.insertAllGoals(defaultGoals)
    }

    suspend fun getAllGoals(): List<Goal> {
        return goalDao.getAllGoals()
    }

    suspend fun getGoalByType(type: String): Goal? {
        return goalDao.getGoalByType(type)
    }
    suspend fun clearGoals(){
        goalDao.clearGoals()
    }
}