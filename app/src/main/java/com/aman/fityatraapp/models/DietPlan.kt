package com.aman.fityatraapp.models

data class Meal(
    val CarbohydrateByDifference: Double,
    val Category: String,
    val Energy: Int,
    val FoodName: String,
    val Meal: String,
    val Protein: Double,
    val SugarsTotalIncludingNLEA: Double,
    val TotalLipid: Double,
    val dietID: Int
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
