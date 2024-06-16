package com.aman.fityatraapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.GraphHelper
import com.aman.fityatraapp.utils.RoundedBarChartRenderer
import com.aman.fityatraapp.utils.SQLiteUtils
import com.github.mikephil.charting.charts.BarChart

class WeightStatisticsActivity : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var titleTextView: TextView
    private lateinit var graphHelper: GraphHelper
    private var sqliteUtils = SQLiteUtils(this)
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
        sqliteUtils.getLast7DaysData(
            onSuccess = { data ->
                Log.d("dataWeight", data.toString())
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
