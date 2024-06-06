package com.aman.fityatraapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.GraphHelper
import com.aman.fityatraapp.utils.RoundedBarChartRenderer
import com.github.mikephil.charting.charts.BarChart

class WeightStatisticsActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var titleTextView: TextView
    private lateinit var graphHelper: GraphHelper
    private var firebaseUtils = FirebaseUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_statistics)
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
        titleTextView.text = "Weight Graph"
        graphHelper = GraphHelper()
        fetchDataForStepCounts(barChart)
    }

    private fun fetchDataForStepCounts(barChart: BarChart) {
        firebaseUtils.getLast7DaysData(
            onSuccess = { data ->
                graphHelper.displayGraph(
                    data,
                    barChart,
                    { it.weight },
                    "Weight",
                    resources.getColor(R.color.purple_200)
                )
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
    }
}
