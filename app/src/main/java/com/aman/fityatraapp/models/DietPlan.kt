package com.aman.fityatraapp.models

import com.google.gson.annotations.SerializedName


data class DietPlan(
    @SerializedName("Day 1") var day1: DayPlan = DayPlan(),
    @SerializedName("Day 2") var day2: DayPlan = DayPlan(),
    @SerializedName("Day 3") var day3: DayPlan = DayPlan(),
    @SerializedName("Day 4") var day4: DayPlan = DayPlan(),
    @SerializedName("Day 5") var day5: DayPlan = DayPlan(),
    @SerializedName("Day 6") var day6: DayPlan = DayPlan(),
    @SerializedName("Day 7") var day7: DayPlan = DayPlan()
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
