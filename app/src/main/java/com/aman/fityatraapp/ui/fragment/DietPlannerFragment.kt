package com.aman.fityatraapp.ui.fragment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.local.model.Meal
import com.aman.fityatraapp.viewModel.DietPlannerViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DietPlannerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton

    private val viewModel: DietPlannerViewModel by viewModels()
    private var currentDayIndex = 0
    private var adapter: DietPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_diet_planner, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)

        btnPrevious.setOnClickListener { navigateToPreviousDay() }
        btnNext.setOnClickListener { navigateToNextDay() }

        observeViewModel()
        viewModel.loadWeeklyDietPlan()

        return view
    }

    private fun observeViewModel() {
        viewModel.mealPlan.observe(viewLifecycleOwner) { plan ->
            adapter = DietPagerAdapter(requireContext(), plan.toMutableMap())
            viewPager.adapter = adapter
            val todayIndex = calculateTodayIndex()
            viewPager.setCurrentItem(todayIndex, false)
            currentDayIndex = todayIndex
        }
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
        val today = LocalDate.now().dayOfWeek.value % 7
        return today
    }
}

class DietPagerAdapter(
    private val context: Context,
    private val mealPlan: Map<String, Map<String, Meal>>
) : RecyclerView.Adapter<DietPagerAdapter.ViewHolder>() {

    private val daysSorted = mealPlan.keys.sorted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_day, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = daysSorted.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = daysSorted[position]
        val meals = mealPlan[day]
        holder.bind(day, meals)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textDay: TextView = itemView.findViewById(R.id.textDay)
        private val cardBreakfast: CardView = itemView.findViewById(R.id.cardBreakfast)
        private val cardLunch: CardView = itemView.findViewById(R.id.cardLunch)
        private val cardDinner: CardView = itemView.findViewById(R.id.cardDinner)

        fun bind(day: String, meals: Map<String, Meal>?) {
            textDay.text = day

            bindMeal(cardBreakfast, meals?.get("Breakfast"), "Breakfast")
            bindMeal(cardLunch, meals?.get("Lunch"), "Lunch")
            bindMeal(cardDinner, meals?.get("Dinner"), "Dinner")
        }

        private fun bindMeal(card: CardView, meal: Meal?, label: String) {
            val title: TextView = card.findViewById(R.id.textBreakfastTitle)
            val nameView = card.findViewById<TextView>(R.id.textBreakfastFoodname)
            val calView = card.findViewById<TextView>(R.id.textBreakfastFat)
            val sugarView = card.findViewById<TextView>(R.id.textBreakfastProtein)
            val fatView = card.findViewById<TextView>(R.id.textBreakfastCarbohydrates)

            title.text= label
            nameView.text = meal?.foodName ?: "$label: N/A"
            calView.text = "Calories: ${meal?.calorie ?: "N/A"} kcal"
            sugarView.text = "Sugar: ${meal?.sugarsTotalIncludingNLEA ?: "N/A"} g"
            fatView.text = "Fat: ${meal?.totalLipidFat ?: "N/A"} g"
        }
    }
}










