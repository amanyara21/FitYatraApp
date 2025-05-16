package com.aman.fityatraapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.viewModel.ExerciseDescriptionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseDescriptionActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var textViewExerciseDescription: TextView
    private lateinit var videoViewExercise: VideoView
    private lateinit var timerButton: Button
    private lateinit var cameraButton: Button
    private lateinit var chronometer: Chronometer

    private var isTimerRunning = false

    private val viewModel: ExerciseDescriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_description)
        supportActionBar?.hide()

        title = findViewById(R.id.headerTitle)
        textViewExerciseDescription = findViewById(R.id.textViewExerciseDescription)
        videoViewExercise = findViewById(R.id.videoViewExercise)
        timerButton = findViewById(R.id.timer_button)
        cameraButton = findViewById(R.id.camera_button)
        chronometer = findViewById(R.id.chronometer)

        val exerciseName = intent.getStringExtra("exerciseName")

        viewModel.getExerciseByName(exerciseName!!)

        viewModel.exercise.observe(this) { exercise ->
            exercise?.let {
                val videoUri = Uri.parse(it.video)

                title.text = it.name
                textViewExerciseDescription.text = it.description

                videoViewExercise.setVideoURI(videoUri)
                videoViewExercise.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(0f, 0f)
                }
                videoViewExercise.start()

                timerButton.setOnClickListener {
                    if (!isTimerRunning) {
                        startTimer()
                    } else {
                        stopTimer(exercise.name)
                    }
                }

                cameraButton.setOnClickListener {
                    val intent = Intent(this, CameraActivity::class.java)
                    intent.putExtra("Exercise", exercise.name)
                    startActivity(intent)
                }
            }
        }
    }

    private fun startTimer() {
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        isTimerRunning = true
        timerButton.text = getString(R.string.stop_exercise_timer)
    }

    private fun stopTimer(exerciseName: String) {
        chronometer.stop()
        isTimerRunning = false
        timerButton.text = getString(R.string.start_exercise_timer)
        Toast.makeText(this, "Exercise Timer Stopped", Toast.LENGTH_SHORT).show()

        val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        val elapsedMinutes = ((elapsedMillis / 1000) / 60).toInt()

        viewModel.saveExerciseData(exerciseName, elapsedMinutes)
    }

    override fun onResume() {
        super.onResume()
        if (!videoViewExercise.isPlaying) {
            videoViewExercise.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoViewExercise.isPlaying) {
            videoViewExercise.pause()
        }
    }
}
