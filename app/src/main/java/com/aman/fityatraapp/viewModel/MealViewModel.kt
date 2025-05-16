package com.aman.fityatraapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.data.api.Item
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.utils.ApiServices

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MealViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val apiService: ApiServices
) : ViewModel() {
    val mealList = MutableLiveData(mutableListOf(MealAdd()))
    val successEvent = MutableLiveData<Boolean>()
    val errorEvent = MutableLiveData<String>()

    fun addMealItem() {
        val currentList = mealList.value ?: mutableListOf()
        currentList.add(MealAdd())
        mealList.value = currentList
    }

    fun removeMealItem(position: Int) {
        val currentList = mealList.value ?: return
        if (position in currentList.indices) {
            currentList.removeAt(position)
            mealList.value = currentList
        }
    }

    fun calculateCalories() {
        val meals = mealList.value ?: return
        val mealData = meals.map { Item(it.dishName, it.quantity) }

        viewModelScope.launch {
            try {
                val response = apiService.calculateCalories(mealData)
                if (response.isSuccessful) {
                    val totalCalories = response.body()?.total_calories?.toInt() ?: 0
                    saveMealData(meals, totalCalories)
                } else {
                    errorEvent.value = "Failed to calculate calories"
                }
            } catch (e: Exception) {
                errorEvent.value = "Error: ${e.message}"
            }
        }
    }

    private suspend fun saveMealData(meals: List<MealAdd>, totalCalories: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val existingData = healthRepository.getHealthDataByDate(todayStart)
        val updatedData = existingData?.copy(
            meals = existingData.meals + meals,
            calorieIntake = existingData.calorieIntake + totalCalories
        ) ?: HealthData(
            date = todayStart,
            meals = meals,
            calorieIntake = totalCalories
        )

        healthRepository.insertOrUpdate(updatedData)
        successEvent.postValue(true)
    }
}
