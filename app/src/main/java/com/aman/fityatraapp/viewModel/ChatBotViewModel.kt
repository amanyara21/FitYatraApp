package com.aman.fityatraapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.ui.Question
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.data.local.model.UserData
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.repository.DietPlanRepository
import com.aman.fityatraapp.repository.HealthRepository
import com.aman.fityatraapp.repository.UserRepository
import com.aman.fityatraapp.utils.ApiServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    val userData = mutableMapOf<String, String>()
    val todayHealthData = MutableLiveData<HealthData?>()
    val isUserDataAvailable = MutableLiveData<Boolean>()
    val savedUserData = MutableLiveData<UserData?>()
    val last7daysData = MutableLiveData<List<HealthData>>()

    init{
        checkUserAvailability()
    }

    private fun checkUserAvailability() {
        viewModelScope.launch {
            val available = userRepository.isUserDataAvailable()
            isUserDataAvailable.value = available
        }
    }


    private fun saveUserData(user: UserData) {
        viewModelScope.launch {
            userRepository.insertUserData(user)
//            Server Memory exceed so hardcoded the diet plan

//            val response = apiService.generateDietPlan(user)
//                Log.d("UserData", response.body().toString())
//            if (response.isSuccessful) {
//                response.body()?.let {
////                    dietPlanRepository.saveDietPlan(it)
//                }
//                checkUserAvailability()
//            }
        }
    }


    fun fetchTodayHealthData() {
        viewModelScope.launch {
            val date = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            todayHealthData.postValue(healthRepository.getHealthDataByDate(date))
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val data = userRepository.getUserData()
            savedUserData.postValue(data)
        }
    }

    fun fetchLast7DaysData() {
        viewModelScope.launch {
            last7daysData.postValue(healthRepository.getLast7DaysHealthData())
        }
    }

    private fun mapActivityLevel(activity: String): Double {
        return when (activity) {
            "Basal Metabolic Rate (BMR)" -> 1.0
            "Sedentary: little or no exercise" -> 1.2
            "Light: exercise 1-3 times/week" -> 1.375
            "Moderate: exercise 4-5 times/week" -> 1.465
            "Active: daily exercise or intense exercise 3-4 times/week" -> 1.55
            "Very Active: intense exercise 6-7 times/week" -> 1.725
            "Extra Active: very intense exercise daily, or physical job" -> 1.9
            else -> 1.0
        }
    }

    private fun mapFitnessGoal(goal: String): String {
        return when (goal) {
            "Maintain weight" -> "m"
            "Mild weight loss of 0.5 lb (0.25 kg) per week" -> "l"
            "Weight loss of 1 lb (0.5 kg) per week" -> "l1"
            "Extreme weight loss of 2 lb (1 kg) per week" -> "l2"
            "Mild weight gain of 0.5 lb (0.25 kg) per week" -> "g"
            "Weight gain of 1 lb (0.5 kg) per week" -> "g1"
            "Extreme weight gain of 2 lb (1 kg) per week" -> "g2"
            else -> "m"
        }
    }

    private fun convertHeightToCm(height: String): Int {
        val parts = height.split(" ")
        return when (parts.size) {
            2 -> {
                parts[0].toInt()
            }
            4 -> {
                val feet = parts[0].toInt()
                val inches = parts[2].toInt()
                (feet * 30.48 + inches * 2.54).toInt()
            }
            else -> {
                throw IllegalArgumentException("Invalid height format")
            }
        }
    }

    fun addUserData(questions: List<Question>) {
        val name = userData[questions[0].question]!!
        val heightInCm = convertHeightToCm(userData[questions[2].question]!!)
        val weightInKg = userData[questions[3].question]!!.toFloat()
        val gender = if (userData[questions[4].question] == "Male") "m" else "f"
        val mealPreference = if (userData[questions[5].question] == "Veg") "veg" else "non-veg"
        val activityLevel =  mapActivityLevel(userData[questions[6].question]!!)
        val goal = mapFitnessGoal(userData[questions[7].question]!!)
        val sleepSchedule = userData[questions[8].question]!!
        val medicalProblems = userData[questions[9].question]!!
        val HbA1c = userData[questions[10].question]!!.toFloat()

        val userData = UserData(
            name = name,
            Height = heightInCm,
            Weight = weightInKg,
            Preference = mealPreference,
            Age = userData[questions[1].question]!!.toInt(),
            Activity = activityLevel,
            Sex = gender,
            Goal = goal,
            sleepSchedule = sleepSchedule,
            medicalProblems = medicalProblems,
            HbA1c = HbA1c
        )


        saveUserData(userData)
//        addChatMessage("Diet Plan Generated Successfully")
//
//        navigateToMainActivity()

    }


}
