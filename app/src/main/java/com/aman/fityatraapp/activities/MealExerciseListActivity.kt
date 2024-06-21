package com.aman.fityatraapp.activities

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
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.SQLiteUtils

class MealExerciseListActivity : AppCompatActivity() {

    private lateinit var listExercises: ListView
    private lateinit var listMeals: ListView
    private lateinit var btnExercise: Button
    private lateinit var btnMeal: Button


    private lateinit var headName: TextView
    private lateinit var headDetail: TextView
    private lateinit var title: TextView

    private lateinit var exerciseAdapter: ExerciseListAdapter
    private lateinit var mealAdapter: MealListAdapter
    private lateinit var sqliteUtils: SQLiteUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_exercise_list)

        supportActionBar?.hide()
        sqliteUtils = SQLiteUtils(this)
        
        title = findViewById(R.id.headerTitle)
        listExercises = findViewById(R.id.list_exercises)
        listMeals = findViewById(R.id.list_meals)
        headName = findViewById(R.id.head_name)
        headDetail = findViewById(R.id.head_detail)

        exerciseAdapter = ExerciseListAdapter(this, getExerciseData()!!)
        mealAdapter = MealListAdapter(this, generateMealData()!!)

        listExercises.adapter = exerciseAdapter
        listMeals.adapter = mealAdapter

        btnExercise = findViewById(R.id.btn_exercise)
        btnMeal = findViewById(R.id.btn_meal)

        listExercises.visibility = View.VISIBLE
        listMeals.visibility = View.GONE

        btnExercise.isSelected = true
        btnMeal.isSelected = false

        title.text= "Exercise List"

        btnExercise.setOnClickListener {
            listExercises.visibility = View.VISIBLE
            listMeals.visibility = View.GONE
            btnExercise.isSelected = true
            btnMeal.isSelected = false
            title.text= "Exercise List"
            headName.text= "Exercise Name"
            headDetail.text= "Duration (in sec.)"
        }

        btnMeal.setOnClickListener {
            listExercises.visibility = View.GONE
            listMeals.visibility = View.VISIBLE
            btnExercise.isSelected = false
            btnMeal.isSelected = true
            title.text="Meal List"
            headName.text= "Dish Name"
            headDetail.text= "Quantity (in grams)"
        }
    }

    private fun getExerciseData(): MutableList<ExerciseAdd>? {
        var exercises: MutableList<ExerciseAdd>? = null
        sqliteUtils.getTodayHealthData(onSuccess = {userData->
             exercises = userData?.exercises
         }, onFailure = {

         })

        return exercises
    }

    private fun generateMealData(): MutableList<MealAdd>? {
        var meals : MutableList<MealAdd>? = null
        sqliteUtils.getTodayHealthData(onSuccess = {userData->
            meals = userData?.meals
        }, onFailure = {

        })
        Log.d("meals", meals.toString())
        return meals
    }
}


class ExerciseListAdapter(context: Context, exercises: MutableList<ExerciseAdd>) : ArrayAdapter<ExerciseAdd>(context, 0, exercises) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val exercise = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        }

        val tvExerciseName = view!!.findViewById<TextView>(R.id.tv_item_name)
        val tvExerciseDuration = view.findViewById<TextView>(R.id.tv_item_detail)

        exercise?.let {
            tvExerciseName?.text = it.exerciseName 
            tvExerciseDuration?.text = "${it.duration} mins" 
        }

        return view
    }
}


class MealListAdapter(context: Context, meals: MutableList<MealAdd>) : ArrayAdapter<MealAdd>(context, 0, meals) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val meal = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        }

        val tvMealName = view!!.findViewById<TextView>(R.id.tv_item_name)
        val tvMealQuantity = view.findViewById<TextView>(R.id.tv_item_detail)

        meal?.let {
            tvMealName?.text = it.dishName
            tvMealQuantity?.text = "${it.quantity} grams"
        }

        return view
    }
}

