package com.aman.fityatraapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.utils.SQLiteUtils
import com.aman.fityatraapp.models.exerItem
import kotlinx.coroutines.launch

class ExerciseAddActivity : AppCompatActivity(), ExerciseAddAdapter.OnDeleteClickListener {

    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAddAdapter: ExerciseAddAdapter
    private var exerciseList = mutableListOf<ExerciseAdd>()
    private lateinit var saveBtn: Button
    private lateinit var title: TextView
    private var sqliteUtils: SQLiteUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_add)
        supportActionBar?.hide()
        saveBtn = findViewById(R.id.saveBtn)
        title = findViewById(R.id.headerTitle)
        title.text = getString(R.string.add_exercise)

        exerciseRecyclerView = findViewById(R.id.exercise_recycler_view)
        exerciseAddAdapter = ExerciseAddAdapter(exerciseList, this)
        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        exerciseRecyclerView.adapter = exerciseAddAdapter

        exerciseList.add(ExerciseAdd())
        exerciseAddAdapter.notifyItemInserted(exerciseList.size - 1)

        findViewById<Button>(R.id.iv_add_exercise).setOnClickListener {
            exerciseList.add(ExerciseAdd())
            exerciseAddAdapter.notifyItemInserted(exerciseList.size - 1)
        }

        saveBtn.setOnClickListener {
            saveExerciseData()
        }

        sqliteUtils = SQLiteUtils(this)
    }

    private fun saveExerciseData() {
        lifecycleScope.launch {
            var totalCalories = 0
            val exercises = exerciseList.map { "${it.exerciseName}:${it.duration}" }.joinToString(";")
            val exercisesData = exerciseList.map {  exerItem(it.exerciseName, it.duration) }

            exercisesData.forEach { exercise ->
                try {
                    val response = apiService.calculateCaloriesBurn(exercise)
                    if (response.isSuccessful) {
                        val caloriesForExercise = response.body()?.calories_burnt?.toInt() ?: 0
                        totalCalories += caloriesForExercise
                    } else {
                        Log.e("Error", "Failed to calculate calories for ${exercise.exercise_name}")
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Exception occurred: ${e.message}")
                }
            }

            Log.d("response", "Total calories burned: $totalCalories")

            sqliteUtils?.addOrUpdateHealthData(
                exerciseList,
                null,
                null,
                null,
                totalCalories,
                null,
                null,
                onSuccess = {
                    showToast("Exercises added successfully")
                    exerciseList.clear()
                    exerciseList.add(ExerciseAdd())
                    exerciseAddAdapter.notifyDataSetChanged()
                },
                onFailure = { e ->
                    Log.e("Error", "Failed to add exercise to database", e)
                    showToast("Failed to add exercises")
                }
            )
        }
    }

    override fun onDeleteClick(position: Int, type: String) {
        exerciseList.removeAt(position)
        exerciseAddAdapter.notifyItemRemoved(position)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
