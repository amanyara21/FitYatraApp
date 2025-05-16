package com.aman.fityatraapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.Goal
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.data.local.model.UserData
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.repository.GoalRepository
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData>
        get() = _userData

    private val _todayData = MutableLiveData<HealthData>()
    val todayData: LiveData<HealthData>
        get() = _todayData

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>>
        get() = _goals


    private val firebaseUtils = FirebaseUtils()
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    init {
        getUser()
        getTodayData()
        getGoals()
    }

    private fun getUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getUserData()
                _userData.postValue(user)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in getUser coroutine: ${e.message}", e)
            }
        }
    }

    private fun getTodayData() {
        viewModelScope.launch {
            try {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val healthData= healthRepository.getHealthDataByDate(todayStart)
                if(healthData!=null){
                    _todayData.postValue(healthData)
                }else{
                    val data = HealthData(System.currentTimeMillis(), 0, 0, 0, 0f,0f,
                        emptyList(), emptyList())
                    _todayData.postValue(data)
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching today's data: ${e.message}")
            }
        }
    }

    private fun getGoals() {
        viewModelScope.launch {
            try {
                val goals= goalRepository.getAllGoals()
                _goals.postValue(goals)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching today's data: ${e.message}")
            }
        }
    }

    fun getExercises() {
        viewModelScope.launch {
            firebaseUtils.getAllExercises { exerciseList ->
                _exercises.postValue(exerciseList)
            }
        }
    }
}

