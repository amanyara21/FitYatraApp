package com.aman.fityatraapp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.api.ExerciseAdd

class ExerciseListAdapter(
    context: Context,
    exercises: MutableList<ExerciseAdd>
) : ArrayAdapter<ExerciseAdd>(context, 0, exercises) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        val exercise = getItem(position)

        view.findViewById<TextView>(R.id.tv_item_name).text = exercise?.exerciseName
        view.findViewById<TextView>(R.id.tv_item_detail).text = "${exercise?.duration ?: 0} mins"

        return view
    }
}
