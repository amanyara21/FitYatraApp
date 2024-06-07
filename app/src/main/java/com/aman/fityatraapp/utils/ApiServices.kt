package com.aman.fityatraapp.utils

import com.aman.fityatraapp.models.DietPlan
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.models.User
import com.aman.fityatraapp.models.UserHealthData

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.Serializable
import java.time.Duration
import java.util.Date


data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)
data class SendOtpRequest(val email: String)
data class VerifyOtpRequest(val email: String, val otp: String)
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val image: String
)


data class ProfileData(
    val age: Number,
    val height: Number,
    val weight: Number,
    val gender: String,
    val postureProblems: String
)

data class UserInfo(
    val user: User,
    val age: String,
    val height: String,
    val weight: String,
    val gender: String,
    val postureProblems: String
)

data class HealthData(
    val exercises: MutableList<ExerciseAdd>? = mutableListOf(),
    val meals: MutableList<MealAdd>? = mutableListOf(),
    var stepCount: Int? = 0,
    var calorieIntake: Int? = 0,
    var calorieBurn: Int? = 0,
    var weight: Float? = 0.0f,
    var glucoseLevel: Float? = 0.0f,
    val date: Long = Date().time
)

data class Activities(
    val activity: String = "",
    val backgroundImage: String = "",
    val image: String = ""
) : Serializable


data class ImageUploadResponse(val imagePath: String)

data class HealthApiResponse(
    val date: Date?,
    val stepCount: Int?,
    val calorieBurn: Float?,
    val calorieIntake: Float?,
    val exercises: List<ExerciseAdd>?,
    val meals: List<MealAdd>?,
)



data class Item(val item: String, val quantity: Int)
data class exerItem(val exercise_name: String, val duration:  Int, val weight: Int =60)
data class CaloriesIntakeResponse(val total_calories: Float)
data class CaloriesBurnResponse(val calories_burnt: Float)


interface ApiServices {
    @Headers("Content-Type: application/json")
    @POST("/calculate")
    suspend fun calculateCalories(@Body items: List<Item>): Response<CaloriesIntakeResponse>

    @Headers("Content-Type: application/json")
    @POST("/calculate_calories")
    suspend fun calculateCaloriesBurn(@Body items: exerItem): Response<CaloriesBurnResponse>

    @Headers("Content-Type: application/json")
    @POST("/generate_diet_plan")
    suspend fun generateDietPlan(@Body request: UserHealthData): Response<List<DietPlan>>
}
