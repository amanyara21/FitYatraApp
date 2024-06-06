package com.aman.fityatraapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.models.ExerciseAdd
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
            calculateCaloriesBurn()
        }
    }

    private fun calculateCaloriesBurn() {
        val exercise = exerciseList.map { exerItem(it.exerciseName, it.time) }
        Log.d("exercise", exercise.toString())

        lifecycleScope.launch {
            val responseExerDeferred = async { apiService.calculateCaloriesBurn(exercise) }
            val responseExer = responseExerDeferred.await()

            if (responseExer.isSuccessful) {
                val totalCaloriesBurn = responseExer.body()?.total_calorie_burn ?: 0
                firebaseUtils.addOrUpdateHealthData(exerciseList, null, 0, 0, totalCaloriesBurn, 0.0f,0.0f, onSuccess = {}, onFailure = {})
            }
        }
    }

    override fun onDeleteClick(position: Int, type: String) {
        exerciseList.removeAt(position)
        exerciseAddAdapter.notifyItemRemoved(position)
    }
}
