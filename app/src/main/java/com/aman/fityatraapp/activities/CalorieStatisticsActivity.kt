package com.aman.fityatraapp.activities


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.GraphHelper
import com.aman.fityatraapp.utils.RoundedBarChartRenderer
import com.aman.fityatraapp.utils.SQLiteUtils
import com.github.mikephil.charting.charts.BarChart

class CalorieStatisticsActivity : AppCompatActivity() {

    private lateinit var barChartCalorieBurn: BarChart
    private lateinit var barChartCalorieIntake: BarChart
    private lateinit var title: TextView
    private lateinit var graphHelper: GraphHelper
    private lateinit var sqliteUtils: SQLiteUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calorie_statistics)
        supportActionBar?.hide()

        sqliteUtils = SQLiteUtils(this)

        // Initialize BarCharts
        barChartCalorieBurn = findViewById(R.id.barChartCalorieBurn)
        barChartCalorieIntake = findViewById(R.id.barChartCalorieIntake)
        title = findViewById(R.id.headerTitle)
        title.text = getString(R.string.calorie_statistics)

        val roundedRenderer1 =
            RoundedBarChartRenderer(
                barChartCalorieBurn,
                barChartCalorieBurn.animator,
                barChartCalorieBurn.viewPortHandler
            )
        barChartCalorieBurn.renderer = roundedRenderer1

        val roundedRenderer2 =
            RoundedBarChartRenderer(
                barChartCalorieIntake,
                barChartCalorieIntake.animator,
                barChartCalorieIntake.viewPortHandler
            )
        barChartCalorieIntake.renderer = roundedRenderer2

        graphHelper = GraphHelper()

        fetchCalorieData(barChartCalorieBurn, barChartCalorieIntake)

    }

    private fun fetchCalorieData(barChart1: BarChart, barChart2: BarChart) {
        sqliteUtils.getLast7DaysData(
            onSuccess = { data ->
                graphHelper.displayGraph(
                    data,
                    barChart1,
                    { it.calorieBurn },
                    "Calories Burn",
                    resources.getColor(R.color.purple_200)
                )
                graphHelper.displayGraph(
                    data,
                    barChart2,
                    { it.calorieIntake },
                    "Calories Intake",
                    resources.getColor(R.color.green)
                )
            },
            onFailure = { e ->
                e.printStackTrace()
            }
        )
    }
}
