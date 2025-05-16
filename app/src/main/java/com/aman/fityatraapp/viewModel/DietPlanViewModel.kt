package com.aman.fityatraapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.Meal
import com.aman.fityatraapp.repository.DietPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietPlannerViewModel @Inject constructor(
    private val dietPlanRepository: DietPlanRepository
) : ViewModel() {

    private val _mealPlan = MutableLiveData<Map<String, Map<String, Meal>>>()
    val mealPlan: LiveData<Map<String, Map<String, Meal>>> = _mealPlan

    fun loadWeeklyDietPlan() {
        viewModelScope.launch {
            val plan = mutableMapOf<String, Map<String, Meal>>()

            val days = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7")
            val mealTimes = listOf("Breakfast", "Lunch", "Dinner")

            for (day in days) {
                val mealsForDay = mutableMapOf<String, Meal>()
                for (mealTime in mealTimes) {
                    val dietPlan = dietPlanRepository.getDietMealFor(day, mealTime)
                    val meal = dietPlan?.mealId?.let { dietPlanRepository.getMealById(it) }
                    if (meal != null) {
                        mealsForDay[mealTime] = meal
                    }
                }
                plan[day] = mealsForDay
            }

            _mealPlan.postValue(plan)
        }
    }
}
