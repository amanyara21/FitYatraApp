package com.aman.fityatraapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.exerItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ExerciseAddActivity : AppCompatActivity(), ExerciseAddAdapter.OnDeleteClickListener {

    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAddAdapter: ExerciseAddAdapter
    private var exerciseList = mutableListOf<ExerciseAdd>()
    private lateinit var saveBtn: Button
    private lateinit var title: TextView
    private var firebaseUtils = FirebaseUtils()

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
    }

    private fun saveExerciseData() {
        lifecycleScope.launch {
            val exercises = exerciseList.map { exerItem(it.exerciseName, it.duration) }
            var totalCalories = 0
            exercises.forEach { exercise ->
                try {
                    val response = apiService.calculateCaloriesBurn(exercise)
                    if (response.isSuccessful) {
                        val caloriesForExercise = response.body()?.calories_burnt?.toInt() ?: 0
                        totalCalories += caloriesForExercise
                        showToast("Exercises Added successfully")
                        exerciseList.clear()
                        exerciseList.add(ExerciseAdd())
                        exerciseAddAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("Error", "Failed to calculate calories for ${exercise.exercise_name}")
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Exception occurred: ${e.message}")
                }
            }
            Log.d("response", "Total calories burned: $totalCalories")
            firebaseUtils.addOrUpdateHealthData(
                exerciseList, null, null, null, totalCalories, null, null,
                onSuccess = { },
                onFailure = { e -> Log.e("Error", e.message.toString()) }
            )
        }
    }


    override fun onDeleteClick(position: Int, type: String) {
        exerciseList.removeAt(position)
        exerciseAddAdapter.notifyItemRemoved(position)
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

}
