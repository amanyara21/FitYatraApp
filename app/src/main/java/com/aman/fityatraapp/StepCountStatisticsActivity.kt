package com.aman.fityatraapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.GraphHelper
import com.aman.fityatraapp.utils.RoundedBarChartRenderer
import com.github.mikephil.charting.charts.BarChart


class StepCountStatisticsActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var titleTextView: TextView
    private lateinit var graphHelper: GraphHelper
    private var firebaseUtils = FirebaseUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_count_statistics)
        supportActionBar?.hide()
        barChart = findViewById(R.id.barChartStepCounts)
        titleTextView = findViewById(R.id.headerTitle)

        val roundedRenderer1 =
            RoundedBarChartRenderer(
                barChart,
                barChart.animator,
                barChart.viewPortHandler
            )
        barChart.renderer = roundedRenderer1
        titleTextView.text = getString(R.string.step_counts)
        graphHelper = GraphHelper()
        fetchDataForStepCounts(barChart)
    }

    private fun fetchDataForStepCounts(barChart: BarChart) {
        firebaseUtils.getLast7DaysData(
            onSuccess = { data ->
                graphHelper.displayGraph(
                    data,
                    barChart,
                    { it.stepCount },
                    "Step Counts",
                    resources.getColor(R.color.purple_200)
                )
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
    }
}


