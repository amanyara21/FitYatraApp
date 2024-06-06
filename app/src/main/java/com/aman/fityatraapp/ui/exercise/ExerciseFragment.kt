package com.aman.fityatraapp.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.ExerciseAdapter
import com.aman.fityatraapp.utils.FirebaseUtils

class ExerciseFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var title: TextView
    private lateinit var firebaseUtils: FirebaseUtils
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        firebaseUtils = FirebaseUtils()
        recyclerView = view.findViewById(R.id.exerciseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        title = view.findViewById(R.id.headerTitle)
        title.text = getString(R.string.exercises)

        getExercises()

        return view
    }

    private fun getExercises() {
        firebaseUtils.getAllExercises { exercises ->
            exercises.let {
                adapter = ExerciseAdapter(it)
                recyclerView.adapter = adapter
            }
        }
    }
}
