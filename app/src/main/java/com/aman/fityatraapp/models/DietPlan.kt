package com.aman.fityatraapp.models

data class Meal(
    val Carbohydrate: Double = 0.0,
    val Category: String = "",
    val Energy: Int = 0,
    val Foodname: String = "",
    val Meal: String = "",
    val Protein: Double = 0.0,
    val Sugars: Double = 0.0,
    val fat: Double = 0.0,
    val DietID: Int = 0
)

data class DayPlan(
    val Breakfast: Meal,
    val Lunch: Meal,
    val Dinner: Meal
)

data class DietPlan(
    val day1: DayPlan,
    val day2: DayPlan,
    val day3: DayPlan,
    val day4: DayPlan,
    val day5: DayPlan,
    val day6: DayPlan,
    val day7: DayPlan
)
