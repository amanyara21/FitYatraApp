package com.aman.fityatraapp.ui



import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.Meal
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.ImageButton

class DietPlannerFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var mealPlan: MutableMap<String, Map<String, Meal>>
    private lateinit var adapter: DietPagerAdapter
    private lateinit var database: DatabaseReference
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
        database = FirebaseDatabase.getInstance().reference

        fetchMeals()

        return view
    }

    private fun fetchMeals() {
        database.child("dietplan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mealPlan.clear()
                for (daySnapshot in snapshot.children) {
                    val day = daySnapshot.key.toString()
                    val meals = mutableMapOf<String, Meal>()
                    for (mealSnapshot in daySnapshot.children) {
                        val mealType = mealSnapshot.key.toString()
                        val meal = mealSnapshot.getValue(Meal::class.java)
                        if (meal != null) {
                            meals[mealType] = meal
                        }
                    }
                    mealPlan[day] = meals
                }
                populateViewPager()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun populateViewPager() {
        adapter = DietPagerAdapter(requireContext(), mealPlan)
        viewPager.adapter = adapter
        val todayIndex = calculateTodayIndex()
        viewPager.setCurrentItem(todayIndex, false)
        currentDayIndex = todayIndex
        updateArrowVisibility()
    }

    private fun navigateToPreviousDay() {
        if (currentDayIndex > 0) {
            viewPager.currentItem = currentDayIndex - 1
            currentDayIndex -= 1
        } else {
            viewPager.currentItem = adapter.itemCount - 1
            currentDayIndex += 1
        }
    }

    private fun navigateToNextDay() {
        if (currentDayIndex < adapter.itemCount - 1) {
            viewPager.currentItem = currentDayIndex + 1
        } else {
            viewPager.currentItem = 0
        }
    }


    private fun updateArrowVisibility() {
        btnPrevious.visibility = if (currentDayIndex > 0) View.VISIBLE else View.INVISIBLE
        btnNext.visibility = if (currentDayIndex < adapter.itemCount - 1) View.VISIBLE else View.INVISIBLE
    }

    private fun calculateTodayIndex(): Int {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE")).toLowerCase()

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

    inner class DietPagerAdapter(private val context: Context, private val mealPlan: MutableMap<String, Map<String, Meal>>) :
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
                    findViewById<TextView>(R.id.textBreakfastFoodname).text = breakfast?.Foodname ?: "N/A"
                    findViewById<TextView>(R.id.textBreakfastFat).text = "Fat: ${breakfast?.fat ?: "N/A"}"
                    findViewById<TextView>(R.id.textBreakfastProtein).text = "Protein: ${breakfast?.Protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textBreakfastCarbohydrates).text = "Carbohydrates: ${breakfast?.Carbohydrate ?: "N/A"}"
                }

                cardLunch.apply {
                    findViewById<TextView>(R.id.textLunchFoodname).text = lunch?.Foodname ?: "N/A"
                    findViewById<TextView>(R.id.textLunchFat).text = "Fat: ${lunch?.fat ?: "N/A"}"
                    findViewById<TextView>(R.id.textLunchProtein).text = "Protein: ${lunch?.Protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textLunchCarbohydrates).text = "Carbohydrates: ${lunch?.Carbohydrate ?: "N/A"}"
                }

                cardDinner.apply {
                    findViewById<TextView>(R.id.textDinnerFoodname).text = dinner?.Foodname ?: "N/A"
                    findViewById<TextView>(R.id.textDinnerFat).text = "Fat: ${dinner?.fat ?: "N/A"}"
                    findViewById<TextView>(R.id.textDinnerProtein).text = "Protein: ${dinner?.Protein ?: "N/A"}"
                    findViewById<TextView>(R.id.textDinnerCarbohydrates).text = "Carbohydrates: ${dinner?.Carbohydrate ?: "N/A"}"
                }
            }
        }
    }
}
