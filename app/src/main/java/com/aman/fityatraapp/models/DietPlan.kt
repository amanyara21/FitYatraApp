package com.aman.fityatraapp.models

import com.google.gson.annotations.SerializedName


data class DietPlan(
    @SerializedName("Day 1") val day1: DayPlan = DayPlan(),
    @SerializedName("Day 2") val day2: DayPlan = DayPlan(),
    @SerializedName("Day 3") val day3: DayPlan = DayPlan(),
    @SerializedName("Day 4") val day4: DayPlan = DayPlan(),
    @SerializedName("Day 5") val day5: DayPlan = DayPlan(),
    @SerializedName("Day 6") val day6: DayPlan = DayPlan(),
    @SerializedName("Day 7") val day7: DayPlan = DayPlan()
)

data class DayPlan(
    val Breakfast: Meal = Meal(),
    val Lunch: Meal = Meal(),
    val Dinner: Meal = Meal()
)

data class Meal(
    @SerializedName("Carbohydrate by difference(g)") val carbohydrateByDifference: Double = 0.0,
    @SerializedName("Category") val category: String = "",
    @SerializedName("Energy(kcal)") val energy: Double = 0.0,
    @SerializedName("Food name") val foodName: String = "",
    @SerializedName("Meal") val meal: String = "",
    @SerializedName("Protein(g)") val protein: Double = 0.0,
    @SerializedName("Sugars total including NLEA(g)") val sugarsTotalIncludingNLEA: Double = 0.0,
    @SerializedName("Total lipid (fat)(g)") val totalLipidFat: Double = 0.0,
    @SerializedName("dietID") val dietId: Int = 0
)
