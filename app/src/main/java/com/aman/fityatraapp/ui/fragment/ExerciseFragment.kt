package com.aman.fityatraapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aman.fityatraapp.R
import com.aman.fityatraapp.databinding.FragmentExerciseBinding
import com.aman.fityatraapp.viewModel.ExerciseViewModel
import com.aman.fityatraapp.utils.ExerciseAdapter

class ExerciseFragment : Fragment() {

    private lateinit var binding: FragmentExerciseBinding
    private lateinit var viewModel: ExerciseViewModel
    private lateinit var adapter: ExerciseAdapter
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()
        viewModel = ViewModelProvider(this)[ExerciseViewModel::class.java]
        title = binding.root.findViewById(R.id.headerTitle)
        title.text = getString(R.string.exercises)

        binding.exerciseRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter = ExerciseAdapter(exercises)
            binding.exerciseRecyclerView.adapter = adapter
        }

        viewModel.getExercises()

        return binding.root
    }

}
