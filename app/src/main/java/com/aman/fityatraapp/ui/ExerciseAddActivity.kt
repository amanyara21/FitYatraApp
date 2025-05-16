package com.aman.fityatraapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.viewModel.ExerciseAddViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseAddActivity : AppCompatActivity(), ExerciseAddAdapter.OnDeleteClickListener {

    private lateinit var exerciseRecyclerView: RecyclerView
    private lateinit var exerciseAddAdapter: ExerciseAddAdapter
    private lateinit var viewModel: ExerciseAddViewModel
    private lateinit var saveBtn: Button
    private lateinit var title: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_add)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ExerciseAddViewModel::class.java]

        saveBtn = findViewById(R.id.saveBtn)
        title = findViewById(R.id.headerTitle)
        title.text = getString(R.string.add_exercise)

        exerciseRecyclerView = findViewById(R.id.exercise_recycler_view)
        exerciseAddAdapter = ExerciseAddAdapter(mutableListOf(), this)
        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        exerciseRecyclerView.adapter = exerciseAddAdapter

        viewModel.exerciseList.observe(this) {
            exerciseAddAdapter.updateData(it)
        }

        findViewById<Button>(R.id.iv_add_exercise).setOnClickListener {
            viewModel.addExercise()
        }

        saveBtn.setOnClickListener {
            viewModel.calculateCalories()
        }

        viewModel.successEvent.observe(this) {
            Toast.makeText(this, "Exercises added successfully", Toast.LENGTH_SHORT).show()
            viewModel.exerciseList.value = mutableListOf(ExerciseAdd())
        }

        viewModel.errorEvent.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteClick(position: Int, type: String) {
        viewModel.removeExercise(position)
    }
}

