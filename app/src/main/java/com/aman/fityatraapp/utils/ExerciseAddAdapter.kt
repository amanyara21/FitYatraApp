package com.aman.fityatraapp.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.ExerciseAdd

class ExerciseAddAdapter(
    private val exerciseList: List<ExerciseAdd>,
    private val onDeleteClickListener: OnDeleteClickListener
) : RecyclerView.Adapter<ExerciseAddAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.editExerciseName.setText(exercise.exerciseName)
        holder.editDuration.setText(exercise.time.toString())

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(position, "exercise")
        }

        holder.editExerciseName.addTextChangedListener {
            exerciseList[position].exerciseName = it.toString()
        }

        holder.editDuration.addTextChangedListener {
            exerciseList[position].time = it.toString().toIntOrNull() ?: 0
        }
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editExerciseName: EditText = itemView.findViewById(R.id.edit_exercise_name)
        val editDuration: EditText = itemView.findViewById(R.id.edit_time)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete_exercise)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int, type: String)
    }
}
