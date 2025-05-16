package com.aman.fityatraapp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.MealAdd

class MealListAdapter(
    context: Context,
    meals: MutableList<MealAdd>
) : ArrayAdapter<MealAdd>(context, 0, meals) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        val meal = getItem(position)

        view.findViewById<TextView>(R.id.tv_item_name).text = meal?.dishName
        view.findViewById<TextView>(R.id.tv_item_detail).text = "${meal?.quantity ?: 0} grams"

        return view
    }
}
