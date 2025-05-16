package com.aman.fityatraapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.DietPlanEntity
import com.aman.fityatraapp.data.local.model.Meal
import com.aman.fityatraapp.repository.DietPlanRepository
import com.aman.fityatraapp.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val application: Application,
    private val dietPlanRepository: DietPlanRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    fun saveDefaultDietPlanToRoom() {
        viewModelScope.launch {
            goalRepository.insertDefaultGoals()
            val jsonString = loadJsonFromAssets("diet.json")
            if (jsonString != null) {
                val json = JSONObject(jsonString)
                val (meals, dayWisePlan) = convertJsonToEntities(json)
                dietPlanRepository.saveDietPlan(dayWisePlan, meals)
            }
        }
    }

    private fun loadJsonFromAssets(fileName: String): String? {
        return try {
            val inputStream = application.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertJsonToEntities(json: JSONObject): Pair<List<Meal>, List<DietPlanEntity>> {
        val mealList = mutableListOf<Meal>()
        val dietPlanList = mutableListOf<DietPlanEntity>()

        val days = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7")

        for (day in days) {
            val dayObject = json.getJSONObject(day)

            for (mealTime in listOf("Breakfast", "Lunch", "Dinner")) {
                val mealJson = dayObject.getJSONObject(mealTime)

                val meal = Meal(
                    foodName = mealJson.getString("food"),
                    carbohydrateByDifference = mealJson.getDouble("carbs"),
                    calorie = mealJson.getDouble("calorie"),
                    category = mealJson.getString("food_type"),
                    meal = mealTime,
                    protein = mealJson.getDouble("protein"),
                    sugarsTotalIncludingNLEA = mealJson.getDouble("sugar"),
                    totalLipidFat = mealJson.getDouble("fat")
                )

                mealList.add(meal)
            }
        }

        val insertedMealIds = mealList.indices.map { it + 1 }

        var index = 0
        for (day in days) {
            for (mealTime in listOf("Breakfast", "Lunch", "Dinner")) {
                dietPlanList.add(
                    DietPlanEntity(
                        day = day,
                        mealTime = mealTime,
                        mealId = insertedMealIds[index++]
                    )
                )
            }
        }

        return Pair(mealList, dietPlanList)
    }
}
