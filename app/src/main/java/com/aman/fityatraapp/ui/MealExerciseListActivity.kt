package com.aman.fityatraapp.ui

import android.content.Context
import com.aman.fityatraapp.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.ExerciseListAdapter
import com.aman.fityatraapp.utils.MealListAdapter
import com.aman.fityatraapp.viewModel.ExerciseMealViewModel
import com.aman.fityatraapp.viewModel.ExerciseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealExerciseListActivity : AppCompatActivity() {

    private val viewModel: ExerciseMealViewModel by viewModels()
    private lateinit var listExercises: ListView
    private lateinit var listMeals: ListView
    private lateinit var btnExercise: Button
    private lateinit var btnMeal: Button

    private lateinit var headName: TextView
    private lateinit var headDetail: TextView
    private lateinit var title: TextView

    private lateinit var exerciseAdapter: ExerciseListAdapter
    private lateinit var mealAdapter: MealListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_exercise_list)
        supportActionBar?.hide()

        initViews()
        setupButtons()

        viewModel.todayData.observe(this){
            if(it!=null){
                val exercises= it.exercises
                val meals= it.meals
                exerciseAdapter = ExerciseListAdapter(this, exercises.toMutableList())
                mealAdapter = MealListAdapter(this, meals.toMutableList())
                listExercises.adapter = exerciseAdapter
                listMeals.adapter = mealAdapter
            }
        }
    }

    private fun initViews() {
        title = findViewById(R.id.headerTitle)
        listExercises = findViewById(R.id.list_exercises)
        listMeals = findViewById(R.id.list_meals)
        headName = findViewById(R.id.head_name)
        headDetail = findViewById(R.id.head_detail)
        btnExercise = findViewById(R.id.btn_exercise)
        btnMeal = findViewById(R.id.btn_meal)
    }

    private fun setupButtons() {
        btnExercise.setOnClickListener {
            toggleList(showExercise = true)
        }
        btnMeal.setOnClickListener {
            toggleList(showExercise = false)
        }

        toggleList(showExercise = true)
    }

    private fun toggleList(showExercise: Boolean) {
        listExercises.visibility = if (showExercise) View.VISIBLE else View.GONE
        listMeals.visibility = if (showExercise) View.GONE else View.VISIBLE

        btnExercise.isSelected = showExercise
        btnMeal.isSelected = !showExercise

        title.text = if (showExercise) "Exercise List" else "Meal List"
        headName.text = if (showExercise) "Exercise Name" else "Dish Name"
        headDetail.text = if (showExercise) "Duration (in min.)" else "Quantity (in grams)"
    }

}

