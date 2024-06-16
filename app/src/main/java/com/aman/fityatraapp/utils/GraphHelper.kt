package com.aman.fityatraapp.utils


import android.graphics.Color
import com.aman.fityatraapp.models.HealthData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GraphHelper {

    fun displayGraph(
        data: List<HealthData>,
        barChart: BarChart,
        attribute: (HealthData) -> Number?,
        title: String,
        color: Int = Color.BLUE
    ) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val dateToValueMap = data.associateBy { dateFormat.format(it.date) }

        val currentDate = Calendar.getInstance()
        for (i in 0..6) {
            val date = currentDate.clone() as Calendar
            date.add(Calendar.DATE, -6 + i)
            val dateString = dateFormat.format(date.time)
            labels.add(dateString)
            val value = dateToValueMap[dateString]?.let(attribute)?.toFloat() ?: 0f
            entries.add(BarEntry(i.toFloat(), value))
        }

        val dataSet = BarDataSet(entries, title)
        dataSet.color = color
        dataSet.valueTextColor = color
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        val barWidth = 0.4f
        barData.barWidth = barWidth
        barData.isHighlightEnabled = true

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()
    }
}

