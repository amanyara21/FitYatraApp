package com.aman.fityatraapp.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.Activities
import com.squareup.picasso.Picasso

class ActivitiesDescriptionActivity : AppCompatActivity() {
    private lateinit var textViewActivityName: TextView
    private lateinit var textViewActivityDescription: TextView
    private lateinit var activityImageView: ImageView
    private lateinit var activity: Activities
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activities_description)
        supportActionBar?.hide()

        textViewActivityName = findViewById(R.id.textViewActivityName)
        textViewActivityDescription = findViewById(R.id.textViewActivityDescription)
        title = findViewById(R.id.headerTitle)

        activityImageView = findViewById(R.id.imageViewActivity)
        activity = intent.getSerializableExtra("activity") as Activities

        title.text = activity.activity

        Picasso.get().load(activity.image).into(activityImageView)

        textViewActivityName.text = activity.activity
        textViewActivityDescription.text = activity.description

    }
}