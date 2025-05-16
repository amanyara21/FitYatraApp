package com.aman.fityatraapp.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.MealAdd

class MealAddAdapter(
    private var mealList: List<MealAdd>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<MealAddAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = mealList[position]
        holder.editDishName.setText(meal.dishName)
        holder.editQuantity.setText(meal.quantity.toString())

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(position, "meal")
        }

        holder.editDishName.addTextChangedListener {
            mealList[position].dishName = it.toString()
        }

        holder.editQuantity.addTextChangedListener { editable ->
            val newQuantity = editable.toString().toIntOrNull() ?: 0
            mealList[position].quantity = newQuantity
        }

    }

    override fun getItemCount(): Int {
        return mealList.size
    }

    fun updateData(newList: List<MealAdd>) {
        mealList = newList.toMutableList()
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editDishName: EditText = itemView.findViewById(R.id.edit_dish_name)
        val editQuantity: EditText = itemView.findViewById(R.id.edit_quantity)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete_meal)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int, type: String)
    }
}
