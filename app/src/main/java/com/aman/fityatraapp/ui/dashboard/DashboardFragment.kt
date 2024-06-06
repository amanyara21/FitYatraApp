package com.aman.fityatraapp.ui.dashboard


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aman.fityatraapp.CalorieStatisticsActivity
import com.aman.fityatraapp.DiabetesStatisticsActivity
import com.aman.fityatraapp.StepCountStatisticsActivity
import com.aman.fityatraapp.WeightStatisticsActivity
import com.aman.fityatraapp.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupUI()
        observeViewModel()

        return binding.root
    }

    private fun setupUI() {
        binding.stepsCard.setOnClickListener {
            val intent = Intent(context, StepCountStatisticsActivity::class.java)
            startActivity(intent)
        }

        binding.calorieCard.setOnClickListener {
            val intent = Intent(context, CalorieStatisticsActivity::class.java)
            startActivity(intent)
        }

        binding.weightCard.setOnClickListener {
            val intent = Intent(context, WeightStatisticsActivity::class.java)
            startActivity(intent)
        }

        binding.diabetesCard.setOnClickListener {
            val intent = Intent(context, DiabetesStatisticsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.todayData.observe(viewLifecycleOwner) { data ->
            binding.stepsValue.text = "${data?.stepCount ?: 0} steps"
            val percent = (data?.stepCount?.div(6000.0))?.times(100) ?: 0.0
            binding.stepsPercentage.text = "${percent.toInt()}%"
            binding.stepsProgressBar.progress = percent.toInt()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTodayData()
    }
}
