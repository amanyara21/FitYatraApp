package com.aman.fityatraapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.data.api.exerItem
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.utils.ApiServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExerciseAddViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val apiService: ApiServices
) : ViewModel() {

    val exerciseList = MutableLiveData(mutableListOf(ExerciseAdd()))
    val successEvent = MutableLiveData<Boolean>()
    val errorEvent = MutableLiveData<String>()

    fun addExercise() {
        val current = exerciseList.value ?: mutableListOf()
        current.add(ExerciseAdd())
        exerciseList.value = current
    }

    fun removeExercise(position: Int) {
        val current = exerciseList.value ?: return
        if (position in current.indices) {
            current.removeAt(position)
            exerciseList.value = current
        }
    }

    fun calculateCalories() {
        val exercises = exerciseList.value ?: return
        val exerciseItems = exercises.map { exerItem(it.exerciseName, it.duration) }

        viewModelScope.launch {
            var totalCalories = 0
            try {
                for (exercise in exerciseItems) {
                    val response = apiService.calculateCaloriesBurn(exercise)
                    if (response.isSuccessful) {
                        totalCalories += response.body()?.calories_burnt?.toInt() ?: 0
                    } else {
                        errorEvent.value = "Failed for ${exercise.exercise_name}"
                        return@launch
                    }
                }
                Log.d("Calories", totalCalories.toString())

                saveExerciseData(exercises, totalCalories)

            } catch (e: Exception) {
                errorEvent.value = "Error: ${e.message}"
            }
        }
    }

    private suspend fun saveExerciseData(exercises: List<ExerciseAdd>, totalCalories: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis

        val existingData = healthRepository.getHealthDataByDate(todayStart)
        val updatedData = existingData?.copy(
            exercises = existingData.exercises + exercises,
            calorieBurn = existingData.calorieBurn + totalCalories
        ) ?: HealthData(
            date = todayStart,
            exercises = exercises,
            calorieBurn = totalCalories
        )

        healthRepository.insertOrUpdate(updatedData)
        successEvent.postValue(true)
    }
}
