package com.aman.fityatraapp.utils

import androidx.room.TypeConverter
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromExerciseList(value: List<ExerciseAdd>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExerciseList(value: String): List<ExerciseAdd> {
        val listType = object : TypeToken<List<ExerciseAdd>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMealList(value: List<MealAdd>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMealList(value: String): List<MealAdd> {
        val listType = object : TypeToken<List<MealAdd>>() {}.type
        return gson.fromJson(value, listType)
    }
}

