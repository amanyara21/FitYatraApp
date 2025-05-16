package com.aman.fityatraapp.utils

import com.aman.fityatraapp.data.local.model.DietPlanEntity
import com.aman.fityatraapp.data.api.CaloriesBurnResponse
import com.aman.fityatraapp.data.api.CaloriesIntakeResponse
import com.aman.fityatraapp.data.api.Item
import com.aman.fityatraapp.data.local.model.UserData
import com.aman.fityatraapp.data.api.exerItem

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiServices {

    @GET("/")
    suspend fun startServer(): String

    @Headers("Content-Type: application/json")
    @POST("/calculate")
    suspend fun calculateCalories(@Body items: List<Item>): Response<CaloriesIntakeResponse>

    @Headers("Content-Type: application/json")
    @POST("/calculate_calories")
    suspend fun calculateCaloriesBurn(@Body items: exerItem): Response<CaloriesBurnResponse>

    @Headers("Content-Type: application/json")
    @POST("/generate_diet_exercise")
    suspend fun generateDietPlan(@Body request: UserData): Response<List<DietPlanEntity>>
}
