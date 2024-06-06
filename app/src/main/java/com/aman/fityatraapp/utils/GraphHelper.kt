package com.aman.fityatraapp.utils


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
        color: Int
    ) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        for (i in 0..6) {
            val date = currentDate.clone() as Calendar
            date.add(Calendar.DATE, -6 + i)
            val dateString = dateFormat.format(date.time)
            labels.add(dateString)
            val value =
                data.find { healthApiResponse -> dateFormat.format(healthApiResponse.date) == dateString }
                    ?.let(attribute)?.toFloat() ?: 0f // Finding corresponding value for the date
            entries.add(BarEntry(i.toFloat(), value))
        }


        val dataSet = BarDataSet(entries, title)
        dataSet.color = color
        dataSet.valueTextColor = color
        dataSet.valueTextSize = 12F
        val barData = BarData(dataSet)
        val barWidth = 0.4f
        barData.barWidth = barWidth
        barData.isHighlightEnabled = true

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.invalidate()

    }

}
