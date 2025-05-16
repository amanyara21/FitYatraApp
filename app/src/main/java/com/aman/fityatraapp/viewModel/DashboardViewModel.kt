package com.aman.fityatraapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.Goal
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.repository.GoalRepository
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val healthRepository: HealthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _goalsLiveData = MutableLiveData<List<Goal>>()
    val goalsLiveData: LiveData<List<Goal>> = _goalsLiveData

    fun loadGoals() {
        viewModelScope.launch {
            _goalsLiveData.postValue(goalRepository.getAllGoals())
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.insertOrUpdateGoal(goal)
            loadGoals()
        }
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            val today = getTodayStartMillis()
            val data = healthRepository.getHealthDataByDate(today)
            val updated = data?.copy(weight = weight) ?: HealthData(date = today, weight = weight)
            healthRepository.insertOrUpdate(updated)
        }
    }

    fun updateGlucose(glucose: Float) {
        viewModelScope.launch {
            val today = getTodayStartMillis()
            val user = userRepository.getUserData()
            user?.let {
                it.HbA1c = glucose
                userRepository.insertUserData(it)
            }

            val health = healthRepository.getHealthDataByDate(today)
            val updated = health?.copy(glucoseLevel = glucose) ?: HealthData(date = today, glucoseLevel = glucose)
            healthRepository.insertOrUpdate(updated)
        }
    }

    private fun getTodayStartMillis(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }
}
