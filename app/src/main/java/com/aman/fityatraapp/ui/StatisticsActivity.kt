package com.aman.fityatraapp.ui


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.GraphHelper
import com.aman.fityatraapp.utils.RoundedBarChartRenderer
import com.aman.fityatraapp.viewModel.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var barChart1: BarChart
    private lateinit var barChart2: BarChart
    private lateinit var graphHelper: GraphHelper
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        supportActionBar?.hide()
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]


        title = findViewById(R.id.headerTitle)
        barChart1 = findViewById(R.id.barChart1)
        barChart2 = findViewById(R.id.barChart2)

        graphHelper = GraphHelper()

        setupRoundedRenderer(barChart1)
        setupRoundedRenderer(barChart2)

        val type = intent.getStringExtra("type") ?: "weight"
        title.text = when (type) {
            "weight" -> "Weight Statistics"
            "calorie" -> "Calorie Statistics"
            "steps" -> "Step Count Statistics"
            "diabetes" -> "Glucose Level Statistics"
            else -> "Statistics"
        }

        viewModel.last7DaysData.observe(this) { data ->
            when (type) {
                "weight" -> {
                    graphHelper.displayGraph(
                        data, barChart1,
                        { it.weight }, "Weight",
                        getColor(R.color.purple_200)
                    )
                    barChart2.visibility = View.GONE
                }

                "calorie" -> {
                    graphHelper.displayGraph(
                        data, barChart1,
                        { it.calorieBurn }, "Calories Burned",
                        getColor(R.color.teal_700)
                    )
                    graphHelper.displayGraph(
                        data, barChart2,
                        { it.calorieIntake }, "Calories Intake",
                        getColor(R.color.green)
                    )
                    barChart2.visibility = View.VISIBLE
                }

                "steps" -> {
                    graphHelper.displayGraph(
                        data, barChart1,
                        { it.stepCount }, "Step Count",
                        getColor(R.color.purple_200)
                    )
                    barChart2.visibility = View.GONE
                }

                "diabetes" -> {
                    graphHelper.displayGraph(
                        data, barChart1,
                        { it.glucoseLevel }, "Glucose Level",
                        getColor(R.color.teal_700)
                    )
                    barChart2.visibility = View.GONE
                }
            }
        }

        viewModel.loadData()
    }

    private fun setupRoundedRenderer(chart: BarChart) {
        chart.renderer = RoundedBarChartRenderer(chart, chart.animator, chart.viewPortHandler)
    }
}
