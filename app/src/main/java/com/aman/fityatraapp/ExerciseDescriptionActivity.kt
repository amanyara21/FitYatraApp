package com.aman.fityatraapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.exerItem
import com.aman.fityatraapp.models.ExerciseAdd
import kotlinx.coroutines.launch

class ExerciseDescriptionActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var textViewExerciseDescription: TextView
    private lateinit var videoViewExercise: VideoView
    private lateinit var timerButton: Button
    private lateinit var cameraButton: Button
    private lateinit var chronometer: Chronometer
    private var firebaseUtils = FirebaseUtils()
    private var isTimerRunning = false

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

        // Get exercise name from intent
        val exerciseName = intent.getStringExtra("exerciseName")

        // Fetch exercise from Firebase by name
        firebaseUtils.getExerciseByName(exerciseName!!) { exercise ->
            exercise?.let { fetchedExercise ->
                val videoUri = Uri.parse(fetchedExercise.video)

                title.text = fetchedExercise.name
                textViewExerciseDescription.text = fetchedExercise.description

                videoViewExercise.setVideoURI(videoUri)
                videoViewExercise.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(0f, 0f)
                }
                videoViewExercise.start()

                timerButton.setOnClickListener {
                    if (!isTimerRunning) {
                        startTimer(chronometer)
                    } else {
                        stopTimer(chronometer)
                    }
                }
                cameraButton.setOnClickListener {
                    val intent = Intent(this, CameraActivity::class.java)
                    intent.putExtra("Exercise", fetchedExercise.name)
                    startActivity(intent)
                }
            } ?: run {
                // Exercise not found by name
                Log.e("ExerciseDescription", "Exercise not found by name: $exerciseName")
                // Handle error or show error message
            }
        }
    }

    private fun startTimer(chronometer: Chronometer) {
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
        isTimerRunning = true
        timerButton.text = getString(R.string.stop_exercise_timer)
    }

    private fun stopTimer(chronometer: Chronometer) {
        chronometer.stop()
        isTimerRunning = false
        timerButton.text = getString(R.string.start_exercise_timer)
        Toast.makeText(this, "Exercise Timer Stopped", Toast.LENGTH_SHORT).show()

        val elapsedMillis = SystemClock.elapsedRealtime() - chronometer.base
        val elapsedMinutes = ((elapsedMillis / 1000) / 60).toInt()

        val exerciseName = intent.getStringExtra("exerciseName")
        val exerciseItem = listOf(exerItem(exerciseName!!, elapsedMinutes))
        val exerciseList = listOf(ExerciseAdd(exerciseName, elapsedMinutes))

        lifecycleScope.launch {
            try {
                val responseExer = apiService.calculateCaloriesBurn(exerciseItem)
                if (responseExer.isSuccessful) {
                    val totalCaloriesBurn = responseExer.body()?.total_calorie_burn ?: 0

                    firebaseUtils.addOrUpdateHealthData(
                        exercises = exerciseList,
                        meals = emptyList(),
                        stepCount = 0,
                        calorieIntake = 0,
                        calorieBurn = totalCaloriesBurn,
                        0.0f,
                        0.0f,
                        onSuccess = {
                            Log.d("Firebase", "Health data successfully updated")
                        },
                        onFailure = { exception ->
                            Log.e("Firebase", "Error updating health data", exception)
                        }
                    )
                } else {
                    Log.d("API Call Error", responseExer.toString())
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception in updating health data", e)
            }
        }
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
