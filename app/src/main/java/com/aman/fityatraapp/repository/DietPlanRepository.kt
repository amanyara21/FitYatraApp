package com.aman.fityatraapp.repository

import com.aman.fityatraapp.data.local.dao.DietPlanDao
import com.aman.fityatraapp.data.local.dao.MealDao
import com.aman.fityatraapp.data.local.model.DietPlanEntity
import com.aman.fityatraapp.data.local.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DietPlanRepository @Inject constructor(
    private val dietPlanDao: DietPlanDao,
    private val mealDao: MealDao
) {
    suspend fun saveDietPlan(
        dayWisePlan: List<DietPlanEntity>,
        meals: List<Meal>
    ) {
        withContext(Dispatchers.IO) {
            mealDao.clearAllMeals()
            dietPlanDao.clearDietPlan()

            mealDao.insertMeals(meals)

            dietPlanDao.insertDietPlan(dayWisePlan)
        }
    }

    suspend fun getMealById(mealId: Int): Meal? {
        return mealDao.getMealById(mealId)
    }

    suspend fun getDietMealFor(day: String, mealTime: String): DietPlanEntity? {
        return dietPlanDao.getDietMealFor(day, mealTime)
    }
}
