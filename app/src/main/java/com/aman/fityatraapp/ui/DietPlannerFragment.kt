package com.aman.fityatraapp.ui


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.Meal
import com.aman.fityatraapp.utils.SQLiteUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DietPlannerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mealPlan: MutableMap<String, Map<String, Meal>>
    private lateinit var adapter: DietPagerAdapter
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private var currentDayIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diet_planner, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        btnPrevious.setOnClickListener { navigateToPreviousDay() }
        btnNext.setOnClickListener { navigateToNextDay() }

        mealPlan = mutableMapOf()

        fetchMeals()

        return view
    }

    private fun fetchMeals() {
        val sqliteUtils = SQLiteUtils(requireContext())

        sqliteUtils.getSavedDietPlan(
            onSuccess = { dietPlanList ->
                mealPlan.clear()
                for (dietPlan in dietPlanList) {
                    Log.d("dietPlan", dietPlan.toString())
                    for (day in listOf(
                        "Day 1",
                        "Day 2",
                        "Day 3",
                        "Day 4",
                        "Day 5",
                        "Day 6",
                        "Day 7"
                    )) {
                        val dayPlan = when (day) {
                            "Day 1" -> dietPlan.day1
                            "Day 2" -> dietPlan.day2
                            "Day 3" -> dietPlan.day3
                            "Day 4" -> dietPlan.day4
                            "Day 5" -> dietPlan.day5
                            "Day 6" -> dietPlan.day6
                            "Day 7" -> dietPlan.day7
                            else -> null
                        }
                        dayPlan?.let {
                            mealPlan[day] = mapOf(
                                "Breakfast" to it.Breakfast,
                                "Lunch" to it.Lunch,
                                "Dinner" to it.Dinner
                            )
                        }
                    }
                }

                populateViewPager()
            },
            onFailure = { exception ->
                // Handle exception
                Log.e("DietPlannerFragment", "Error fetching meals", exception)
            }
        )
    }

    private fun populateViewPager() {
        adapter = DietPagerAdapter(requireContext(), mealPlan)
        viewPager.adapter = adapter
        val todayIndex = calculateTodayIndex()
        viewPager.setCurrentItem(todayIndex, false)
        currentDayIndex = todayIndex
    }

    private fun navigateToPreviousDay() {
        currentDayIndex = (currentDayIndex - 1 + 7) % 7
        viewPager.setCurrentItem(currentDayIndex, true)
    }

    private fun navigateToNextDay() {
        currentDayIndex = (currentDayIndex + 1) % 7
        viewPager.setCurrentItem(currentDayIndex, true)
    }

    private fun calculateTodayIndex(): Int {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
            .lowercase(Locale.getDefault())

        return when (today) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> 1
        }
    }

    inner class DietPagerAdapter(
        private val context: Context,
        private val mealPlan: MutableMap<String, Map<String, Meal>>
    ) :
        RecyclerView.Adapter<DietPagerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_day, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val day = mealPlan.keys.sorted()[position]
            holder.bind(day, mealPlan[day])
        }

        override fun getItemCount(): Int {
            return mealPlan.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textDay: TextView = itemView.findViewById(R.id.textDay)
            private val cardBreakfast: CardView = itemView.findViewById(R.id.cardBreakfast)
            private val cardLunch: CardView = itemView.findViewById(R.id.cardLunch)
            private val cardDinner: CardView = itemView.findViewById(R.id.cardDinner)

            fun bind(day: String, meals: Map<String, Meal>?) {
                textDay.text = day
                val breakfast = meals?.get("Breakfast")
                val lunch = meals?.get("Lunch")
                val dinner = meals?.get("Dinner")

                cardBreakfast.apply {
                    findViewById<TextView>(R.id.textBreakfastFoodname).text =
                        breakfast?.foodName ?: "N/A"
                    findViewById<TextView>(R.id.textBreakfastFat).text =
                        "Fat: ${breakfast?.totalLipidFat ?: "N/A"}"
                    findViewById<TextView>(R.id.textBreakfastProtein).text =
                        "Protein: ${breakfast?.protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textBreakfastCarbohydrates).text =
                        "Carbohydrates: ${breakfast?.carbohydrateByDifference ?: "N/A"}"
                }

                cardLunch.apply {
                    findViewById<TextView>(R.id.textLunchFoodname).text = lunch?.foodName ?: "N/A"
                    findViewById<TextView>(R.id.textLunchFat).text =
                        "Fat: ${lunch?.totalLipidFat ?: "N/A"}"
                    findViewById<TextView>(R.id.textLunchProtein).text =
                        "Protein: ${lunch?.protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textLunchCarbohydrates).text =
                        "Carbohydrates: ${lunch?.carbohydrateByDifference ?: "N/A"}"
                }

                cardDinner.apply {
                    findViewById<TextView>(R.id.textDinnerFoodname).text = dinner?.foodName ?: "N/A"
                    findViewById<TextView>(R.id.textDinnerFat).text =
                        "Fat: ${dinner?.totalLipidFat ?: "N/A"}"
                    findViewById<TextView>(R.id.textDinnerProtein).text =
                        "Protein: ${dinner?.protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textDinnerCarbohydrates).text =
                        "Carbohydrates: ${dinner?.carbohydrateByDifference ?: "N/A"}"
                }
            }
        }
    }
}








