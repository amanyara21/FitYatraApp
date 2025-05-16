package com.aman.fityatraapp.utils

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.ui.ActivitiesDescriptionActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.Activities
import com.squareup.picasso.Picasso

class ActivityAdapter(private val activities: List<Activities>) :
    RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item_layout, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        holder.nameTextView.text = activity.activity
        Picasso.get()
            .load(activity.backgroundImage)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ActivitiesDescriptionActivity::class.java)
            intent.putExtra("activity", activity)
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return activities.size
    }

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    }
}
