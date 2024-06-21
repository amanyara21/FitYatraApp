package com.aman.fityatraapp.ui.dashboard


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aman.fityatraapp.activities.CalorieStatisticsActivity
import com.aman.fityatraapp.activities.ExerciseAddActivity
import com.aman.fityatraapp.activities.MealActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.activities.DiabetesStatisticsActivity
import com.aman.fityatraapp.activities.StepCountStatisticsActivity
import com.aman.fityatraapp.activities.WeightStatisticsActivity
import com.aman.fityatraapp.databinding.FragmentDashboardBinding
import com.aman.fityatraapp.models.Goal
import com.aman.fityatraapp.models.UserData
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.SQLiteUtils
import kotlinx.coroutines.launch


class DashboardFragment : Fragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var sqliteUtils: SQLiteUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        sqliteUtils = SQLiteUtils(requireContext())
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.glucoseCard.setOnClickListener {
            navigateToDiabetesStatisticsActivity()
        }
        binding.addGlucoseData.setOnClickListener {
            openGlucoseEditor()
        }
        binding.stepCountCard.setOnClickListener {
            navigateToStepCountStatisticsActivity()
        }
        binding.updateStepCountGoal.setOnClickListener {
            openGoalEditor("step_count", "Step Count")
        }

        binding.calorieBurnCard.setOnClickListener {
            navigateToCalorieStatisticsActvity()
        }
        binding.updateCalorieBurnGoal.setOnClickListener {
            openGoalEditor("calorie_burn", "Calorie Burn")
        }
        binding.addCalorieBurnData.setOnClickListener {
            navigateToAddCalorieBurnActivity()
        }

        binding.calorieIntakeCard.setOnClickListener {
            navigateToCalorieStatisticsActvity()
        }
        binding.updateCalorieIntakeGoal.setOnClickListener {
            openGoalEditor("calorie_intake", "Calorie Intake")
        }
        binding.addCalorieIntakeData.setOnClickListener {
            navigateToAddCalorieIntakeActivity()
        }

        binding.weightCard.setOnClickListener {
            navigateToWeightStatisticsActivity()
        }
        binding.addWeightData.setOnClickListener {
            openWeightEditor()
        }
    }




    private fun observeViewModel() {
        viewModel.goalsLiveData.observe(viewLifecycleOwner) { goals ->
            updateUI(goals)
        }
    }

    private fun updateUI(goals: List<Goal>) {
        goals.forEach { goal ->
            when (goal.goalType) {
                "step_count" -> {
                    binding.stepCountGoal.text =
                        getString(R.string.goal_template, goal.goalValue.toString())
                }

                "calorie_burn" -> {
                    binding.calorieBurnGoal.text =
                        getString(R.string.goal_template, goal.goalValue.toString())
                }

                "calorie_intake" -> {
                    binding.calorieIntakeGoal.text =
                        getString(R.string.goal_template, goal.goalValue.toString())
                }
            }
        }
    }

    private fun openGoalEditor(goalType: String, type:String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)
        editText.hint = "Enter $type"


        with(builder) {
            setTitle("Edit $type Goal")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val goalValue = editText.text.toString().toIntOrNull()

                if (goalValue != null && goalValue > 0) {
                    viewModel.updateGoal(Goal(goalType, goalValue))
                } else {
                    Toast.makeText(requireContext(), "Goal value cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }


    private fun openWeightEditor() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextWeight)

        with(builder) {
            setTitle("Add Today's Weight")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val weight = editText.text.toString().toFloat()

                if (weight!=0.0f) {
                    sqliteUtils.addOrUpdateHealthData(emptyList(), emptyList(), 0, 0, 0,weight, null, onSuccess = {}, onFailure = {})
                } else {
                    Toast.makeText(requireContext(), "Weight cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun openGlucoseEditor() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)
        editText.hint = "2.4"

        with(builder) {
            setTitle("Add Glucose (HbA1c) Level (in %)")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val glucose = editText.text.toString().toFloatOrNull()
                if (glucose != null && glucose != 0.0f) {
                    sqliteUtils.getUserData(
                        onSuccess = { userData ->
                            if (userData != null) {
                                userData.HbA1c = glucose
                                sqliteUtils.addOrUpdateHealthData(
                                    emptyList(),
                                    emptyList(),
                                    0,
                                    0,
                                    0,
                                    null,
                                    glucose,
                                    onSuccess = {
                                        makeApiCall(userData)
                                    },
                                    onFailure = {
                                    }
                                )
                            } else {
                            }
                        },
                        onFailure = {
                        }
                    )
                } else {
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun navigateToAddCalorieIntakeActivity() {
        val intent = Intent(requireContext(), MealActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAddCalorieBurnActivity() {
        val intent = Intent(requireContext(), ExerciseAddActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToWeightStatisticsActivity() {
        val intent = Intent(requireContext(), WeightStatisticsActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToStepCountStatisticsActivity() {
        val intent = Intent(requireContext(), StepCountStatisticsActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToCalorieStatisticsActvity() {
        val intent = Intent(requireContext(), CalorieStatisticsActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToDiabetesStatisticsActivity() {
        val intent = Intent(requireContext(), DiabetesStatisticsActivity::class.java)
        startActivity(intent)
    }
    private fun makeApiCall(userData: UserData) {
        lifecycleScope.launch {
            val response = apiService.generateDietPlan(userData)
            if (response.isSuccessful) {
                Log.d("response", response.body()!!.toString())
                sqliteUtils.saveDietPlan(response.body()!!)
            }
        }
    }
}

