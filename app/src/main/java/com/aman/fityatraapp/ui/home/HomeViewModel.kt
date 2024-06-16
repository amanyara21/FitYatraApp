package com.aman.fityatraapp.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.models.Goal
import com.aman.fityatraapp.models.UserData
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.models.HealthData
import com.aman.fityatraapp.utils.SQLiteUtils
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _userData = MutableLiveData<UserData>()
    private val sqLiteUtils = SQLiteUtils(application)
    val userData: LiveData<UserData>
        get() = _userData

    private val _todayData = MutableLiveData<HealthData>()
    val todayData: LiveData<HealthData>
        get() = _todayData

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>>
        get() = _goals

    private val _errorLiveData = MutableLiveData<String>()


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
                sqLiteUtils.getUserData(
                    onSuccess = { userData ->
                        _userData.postValue(userData ?: UserData())
                    },
                    onFailure = { exception ->
                        Log.e(
                            "HomeViewModel",
                            "Error fetching user data: ${exception.message}",
                            exception
                        )
                        _userData.postValue(UserData())
                    }
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in getUser coroutine: ${e.message}", e)
            }
        }
    }

    private fun getTodayData() {
        viewModelScope.launch {
            try {
                sqLiteUtils.getTodayHealthData(
                    onSuccess = { userData ->
                        _todayData.postValue(userData ?: HealthData())
                    },
                    onFailure = { exception ->
                        Log.e(
                            "HomeViewModel",
                            "Error fetching user data: ${exception.message}",
                            exception
                        )
                        _todayData.postValue(HealthData())
                    }
                )

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching today's data: ${e.message}")
            }
        }
    }

    private fun getGoals() {
        sqLiteUtils.getGoals(
            onSuccess = { goals ->
                _goals.postValue(goals)
            },
            onFailure = { exception ->
                _errorLiveData.postValue("Failed to fetch goals: ${exception.message}")
            }
        )
    }

    fun getExercises() {
        viewModelScope.launch {
            firebaseUtils.getAllExercises { exerciseList ->
                _exercises.postValue(exerciseList)
            }
        }
    }
}

