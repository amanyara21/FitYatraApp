package com.aman.fityatraapp.utils

import com.aman.fityatraapp.models.CaloriesBurnResponse
import com.aman.fityatraapp.models.CaloriesIntakeResponse
import com.aman.fityatraapp.models.DietPlan
import com.aman.fityatraapp.models.Item
import com.aman.fityatraapp.models.UserData
import com.aman.fityatraapp.models.exerItem

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiServices {

    @GET("/")
    suspend fun startServer(): String //This is for the server start after render server stop

    @Headers("Content-Type: application/json")
    @POST("/calculate")
    suspend fun calculateCalories(@Body items: List<Item>): Response<CaloriesIntakeResponse>

    @Headers("Content-Type: application/json")
    @POST("/calculate_calories")
    suspend fun calculateCaloriesBurn(@Body items: exerItem): Response<CaloriesBurnResponse>

    @Headers("Content-Type: application/json")
    @POST("/generate_diet_plan")
    suspend fun generateDietPlan(@Body request: UserData): Response<List<DietPlan>>
}
