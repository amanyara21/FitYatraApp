package com.aman.fityatraapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.data.api.exerItem
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.utils.ApiServices
import com.aman.fityatraapp.utils.FirebaseUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExerciseDescriptionViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val apiService: ApiServices
) : ViewModel() {

    private val _exercise = MutableLiveData<Exercise?>()
    val exercise: LiveData<Exercise?> = _exercise
    private val firebaseUtils=FirebaseUtils()
    fun getExerciseByName(name: String) {
        firebaseUtils.getExerciseByName(name) { fetchedExercise ->
            _exercise.postValue(fetchedExercise)
        }
    }

    fun saveExerciseData(exerciseName: String, duration: Int) {
        val exerciseItem = exerItem(exerciseName, duration)
        val exerciseList = listOf(ExerciseAdd(exerciseName, duration))

        viewModelScope.launch {
            try {
                val response = apiService.calculateCaloriesBurn(exerciseItem)
                if (response.isSuccessful) {
                    val totalCaloriesBurn = response.body()?.calories_burnt?.toInt() ?: 0

                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val existingData = healthRepository.getHealthDataByDate(todayStart)
                    val updatedData = existingData?.copy(
                        exercises = existingData.exercises + exerciseList,
                        calorieBurn = existingData.calorieBurn + totalCaloriesBurn
                    ) ?: HealthData(
                        date = todayStart,
                        exercises = exerciseList,
                        calorieBurn = totalCaloriesBurn
                    )

                    healthRepository.insertOrUpdate(updatedData)
                } else {
                    Log.e("API Error", "Failed to fetch calories")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Exception in saveExerciseData", e)
            }
        }
    }
}
