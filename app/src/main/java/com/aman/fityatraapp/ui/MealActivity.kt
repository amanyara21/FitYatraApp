package com.aman.fityatraapp.ui


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.MealAddAdapter
import com.aman.fityatraapp.viewModel.MealViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MealActivity : AppCompatActivity(), MealAddAdapter.OnDeleteClickListener {

    private lateinit var mealRecyclerView: RecyclerView
    private lateinit var mealAddAdapter: MealAddAdapter
    private lateinit var viewModel: MealViewModel
    private lateinit var saveBtn: Button
    private lateinit var title: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[MealViewModel::class.java]

        saveBtn = findViewById(R.id.saveBtn)
        title = findViewById(R.id.headerTitle)
        title.text = getString(R.string.add_meal)

        mealRecyclerView = findViewById(R.id.meal_recycler_view)
        mealAddAdapter = MealAddAdapter(mutableListOf(), this)
        mealRecyclerView.layoutManager = LinearLayoutManager(this)
        mealRecyclerView.adapter = mealAddAdapter

        viewModel.mealList.observe(this) {
            mealAddAdapter.updateData(it)
        }

        findViewById<Button>(R.id.iv_add_meal).setOnClickListener {
            viewModel.addMealItem()
        }

        saveBtn.setOnClickListener {
            viewModel.calculateCalories()
        }

        viewModel.successEvent.observe(this) {
            Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show()
            viewModel.mealList.value = mutableListOf(MealAdd())
        }

        viewModel.errorEvent.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteClick(position: Int, type: String) {
        viewModel.removeMealItem(position)
    }
}
