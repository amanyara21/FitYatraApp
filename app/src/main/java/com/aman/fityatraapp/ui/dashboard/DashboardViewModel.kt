package com.aman.fityatraapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aman.fityatraapp.models.Goal
import com.aman.fityatraapp.utils.SQLiteUtils

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sqliteUtils: SQLiteUtils = SQLiteUtils(application)

    private val _goalsLiveData = MutableLiveData<List<Goal>>()
    val goalsLiveData: LiveData<List<Goal>>
        get() = _goalsLiveData

    private val _updateGoalSuccess = MutableLiveData<Unit>()

    private val _errorLiveData = MutableLiveData<String>()

    init {
        getGoals()
    }

    fun getGoals() {
        sqliteUtils.getGoals(
            onSuccess = { goals ->
                _goalsLiveData.postValue(goals)
            },
            onFailure = { exception ->
                _errorLiveData.postValue("Failed to fetch goals: ${exception.message}")
            }
        )
    }

    fun updateGoal(goal: Goal) {
        sqliteUtils.updateGoal(
            goal = goal,
            onSuccess = {
                _updateGoalSuccess.postValue(Unit)
                // Reload goals after successful update
                getGoals()
            },
            onFailure = { exception ->
                _errorLiveData.postValue("Failed to update goal: ${exception.message}")
            }
        )
    }
}
