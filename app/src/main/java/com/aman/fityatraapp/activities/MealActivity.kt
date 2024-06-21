package com.aman.fityatraapp.activities


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.models.Item
import com.aman.fityatraapp.utils.MealAddAdapter
import com.aman.fityatraapp.utils.SQLiteUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MealActivity : AppCompatActivity(), MealAddAdapter.OnDeleteClickListener {

    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var mealAddAdapter: MealAddAdapter
    private var mealList = mutableListOf<MealAdd>()
    private lateinit var saveBtn: Button
    private lateinit var title: TextView
    private var sqLiteUtils: SQLiteUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)
        supportActionBar?.hide()
        saveBtn = findViewById(R.id.saveBtn)
        title = findViewById(R.id.headerTitle)
        title.text = getString(R.string.add_meal)

        mealRecyclerView = findViewById(R.id.meal_recycler_view)
        mealAddAdapter = MealAddAdapter(mealList, this)
        mealRecyclerView.layoutManager = LinearLayoutManager(this)
        mealRecyclerView.adapter = mealAddAdapter

        mealList.add(MealAdd())
        mealAddAdapter.notifyItemInserted(mealList.size - 1)

        findViewById<Button>(R.id.iv_add_meal).setOnClickListener {
            mealList.add(MealAdd())
            mealAddAdapter.notifyItemInserted(mealList.size - 1)
        }

        saveBtn.setOnClickListener {
            calculateCalories()
        }

        sqLiteUtils = SQLiteUtils(this)
    }

    private fun calculateCalories() {
        val meals = mealList.map { "${it.dishName}:${it.quantity}" }.joinToString(";")
        val mealData = mealList.map { Item(it.dishName, it.quantity) }

        lifecycleScope.launch {
            val responseMealDeferred = async { apiService.calculateCalories(mealData) }
            val responseMeal = responseMealDeferred.await()

            if (responseMeal.isSuccessful) {
                val totalCalories = responseMeal.body()?.total_calories?.toInt() ?: 0
                Toast.makeText(this@MealActivity, "Meal added successfully", Toast.LENGTH_SHORT).show()

                sqLiteUtils?.addOrUpdateHealthData(
                    null,
                    mealList,
                    0,
                    totalCalories,
                    0,
                    null,
                    null,
                    onSuccess = {
                        mealList.clear()
                        mealList.add(MealAdd())
                        mealAddAdapter.notifyDataSetChanged()
                    },
                    onFailure = { error ->
                        Log.e("MealActivity", "Error adding meal to database", error)
                        Toast.makeText(this@MealActivity, "Failed to add meal", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this@MealActivity, "Failed to calculate calories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDeleteClick(position: Int, type: String) {
        mealList.removeAt(position)
        mealAddAdapter.notifyItemRemoved(position)
    }
}
