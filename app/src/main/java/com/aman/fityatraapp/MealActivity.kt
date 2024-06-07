package com.aman.fityatraapp


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.Item
import com.aman.fityatraapp.utils.MealAddAdapter
import com.aman.fityatraapp.utils.exerItem

import kotlinx.coroutines.async
import kotlinx.coroutines.launch



class MealActivity : AppCompatActivity(), MealAddAdapter.OnDeleteClickListener {

    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var mealAddAdapter: MealAddAdapter
    private var mealList = mutableListOf<MealAdd>()
    private lateinit var saveBtn: Button
    private lateinit var title: TextView
    private var firebaseUtils = FirebaseUtils()

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
    }

    private fun calculateCalories() {
        val meals = mealList.map { Item(it.dishName, it.quantity) }
        Log.d("meals", meals.toString())

        lifecycleScope.launch {
            val responseMealDeferred = async { apiService.calculateCalories(meals) }
            val responseMeal = responseMealDeferred.await()

            if (responseMeal.isSuccessful) {
                val totalCalories = responseMeal.body()?.total_calories?.toInt() ?: 0
                firebaseUtils.addOrUpdateHealthData(null, mealList, 0, totalCalories, 0,0.0f,0.0f, onSuccess = {}, onFailure = {})

                showToast("Meal Added successfully")
                mealList.clear()
                mealList.add(MealAdd())
                mealAddAdapter.notifyDataSetChanged()
            }
        }
    }


    override fun onDeleteClick(position: Int, type: String) {
        mealList.removeAt(position)
        mealAddAdapter.notifyItemRemoved(position)
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

}

