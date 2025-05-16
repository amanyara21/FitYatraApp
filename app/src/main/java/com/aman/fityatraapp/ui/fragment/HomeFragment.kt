package com.aman.fityatraapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.local.model.Goal
import com.aman.fityatraapp.utils.ExerciseAdapter
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.databinding.FragmentHomeBinding
import com.aman.fityatraapp.ui.MealExerciseListActivity
import com.aman.fityatraapp.ui.StatisticsActivity
import com.aman.fityatraapp.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ExerciseAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.gotoExercises.setOnClickListener {
            navigateToExerciseFragment()
        }
        binding.latestExerciseCard.setOnClickListener{
            navigateToMealExerciseActivity()
        }
        binding.progressRelativeLayout.setOnClickListener {
            navigateToStatisticsActivity("steps")
        }

        binding.calorieDataRelativeLayout.setOnClickListener {
            navigateToStatisticsActivity("calorie")
        }

        binding.exerciseRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter = ExerciseAdapter(exercises)
            binding.exerciseRecyclerView.adapter = adapter
        }

        viewModel.getExercises()
    }



    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.nameTextView.text = "${it.name}!✌️"
            }
        }

        viewModel.todayData.observe(viewLifecycleOwner) { data ->
            data?.let {
                viewModel.goals.value?.let { goals ->
                    updateUIValues(it, goals)
                }
            }
        }

        viewModel.goals.observe(viewLifecycleOwner) { goals ->
            Log.d("Goals", goals.toString())
            goals?.let {
                viewModel.todayData.value?.let { data ->
                    updateUIValues(data, goals)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIValues(data: HealthData, goals: List<Goal>) {
        Log.d("HealthData", data.toString())

        val stepGoal = goals.find { it.goalType == "step_count" }?.goalValue?.toDouble() ?: 6000.0
        val calorieBurnGoal = goals.find { it.goalType == "calorie_burn" }?.goalValue?.toDouble() ?: 3000.0
        val calorieIntakeGoal = goals.find { it.goalType == "calorie_intake" }?.goalValue?.toDouble() ?: 3000.0

        binding.completedStepsTextView.text = "Steps taken: ${data.stepCount}"
        val stepPercent = (data.stepCount.div(stepGoal)).times(100)
        binding.txtper.text = "${stepPercent.toInt()}%"
        binding.stepProgressBar.progress = stepPercent.toInt()

        binding.exer1Text.text = data.exercises.getOrNull(0)?.exerciseName ?: "Pushups"
        binding.exer2Text.text = data.exercises.getOrNull(1)?.exerciseName ?: "Lunges"
        binding.exer3Text.text = data.exercises.getOrNull(2)?.exerciseName ?: "Pullups"

        binding.totalExer1Text.text = "${data.exercises.getOrNull(0)?.duration ?: 0} mins"
        binding.totalExer2Text.text = "${data.exercises.getOrNull(1)?.duration ?: 0} mins"
        binding.totalExer3Text.text = "${data.exercises.getOrNull(2)?.duration ?: 0} mins"

        binding.completedCalorieBurnTextView.text = data.calorieBurn.toString()
        binding.completedCalorieIntakeTextView.text = data.calorieIntake.toString()

        binding.txtperCalorieBurn.text =
            "${((data.calorieBurn.div(calorieBurnGoal)).times(100)).toInt()}%"
        binding.calorieBurnProgressBar.progress =
            ((data.calorieBurn.div(calorieBurnGoal)).times(100)).toInt()

        binding.txtperCalorieIntake.text =
            "${((data.calorieIntake.div(calorieIntakeGoal)).times(100)).toInt()}%"
        binding.calorieIntakeProgressBar.progress =
            ((data.calorieIntake.div(calorieIntakeGoal)).times(100)).toInt()
    }

    private fun navigateToExerciseFragment() {
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.navigation_notifications)
    }

    private fun navigateToStatisticsActivity(type: String) {
        val intent=Intent(requireContext(), StatisticsActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }
    private fun navigateToMealExerciseActivity() {
        val intent = Intent(requireContext(), MealExerciseListActivity::class.java)
        startActivity(intent)
    }
}

