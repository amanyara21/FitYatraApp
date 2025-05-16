package com.aman.fityatraapp.ui.fragment


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.local.model.Goal
import com.aman.fityatraapp.databinding.FragmentDashboardBinding
import com.aman.fityatraapp.ui.ExerciseAddActivity
import com.aman.fityatraapp.ui.MealActivity
import com.aman.fityatraapp.ui.StatisticsActivity
import com.aman.fityatraapp.viewModel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        viewModel.loadGoals()
    }

    private fun observeViewModel() {
        viewModel.goalsLiveData.observe(viewLifecycleOwner) { updateUI(it) }
    }

    private fun updateUI(goals: List<Goal>) {
        goals.forEach { goal ->
            when (goal.goalType) {
                "step_count" -> binding.stepCountGoal.text = getString(R.string.goal_template, goal.goalValue.toString())
                "calorie_burn" -> binding.calorieBurnGoal.text = getString(R.string.goal_template, goal.goalValue.toString())
                "calorie_intake" -> binding.calorieIntakeGoal.text = getString(R.string.goal_template, goal.goalValue.toString())
            }
        }
    }

    private fun setupViews() {
        binding.glucoseCard.setOnClickListener { navigateToStatisticsActivity("diabetes") }
        binding.weightCard.setOnClickListener { navigateToStatisticsActivity("weight") }
        binding.stepCountCard.setOnClickListener { navigateToStatisticsActivity("steps") }
        binding.calorieBurnCard.setOnClickListener { navigateToStatisticsActivity("calorie") }
        binding.calorieIntakeCard.setOnClickListener { navigateToStatisticsActivity("calorie") }

        binding.addGlucoseData.setOnClickListener { openGlucoseEditor() }
        binding.addWeightData.setOnClickListener { openWeightEditor() }

        binding.updateStepCountGoal.setOnClickListener { openGoalEditor("step_count", "Step Count") }
        binding.updateCalorieBurnGoal.setOnClickListener { openGoalEditor("calorie_burn", "Calorie Burn") }
        binding.updateCalorieIntakeGoal.setOnClickListener { openGoalEditor("calorie_intake", "Calorie Intake") }

        binding.addCalorieBurnData.setOnClickListener { startActivity(Intent(requireContext(), ExerciseAddActivity::class.java)) }
        binding.addCalorieIntakeData.setOnClickListener { startActivity(Intent(requireContext(), MealActivity::class.java)) }
    }

    private fun navigateToStatisticsActivity(type: String) {
        val intent = Intent(requireContext(), StatisticsActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    private fun openGoalEditor(goalType: String, type: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)
        editText.hint = "Enter $type"

        builder.setTitle("Edit $type Goal")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val value = editText.text.toString().toIntOrNull()
                if (value != null && value > 0) {
                    viewModel.updateGoal(Goal(goalType, value))
                } else {
                    Toast.makeText(requireContext(), "Goal cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openWeightEditor() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextWeight)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Today's Weight")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val weight = editText.text.toString().toFloatOrNull()
                if (weight != null && weight > 0f) {
                    viewModel.updateWeight(weight)
                } else {
                    Toast.makeText(requireContext(), "Invalid weight", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGlucoseEditor() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)
        editText.hint = "2.4"

        AlertDialog.Builder(requireContext())
            .setTitle("Add Glucose (HbA1c) Level")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                val glucose = editText.text.toString().toFloatOrNull()
                if (glucose != null && glucose > 0f) {
                    viewModel.updateGlucose(glucose)
                } else {
                    Toast.makeText(requireContext(), "Invalid glucose value", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

