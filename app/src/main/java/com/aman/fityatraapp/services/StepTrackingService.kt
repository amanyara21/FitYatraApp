package com.aman.fityatraapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.aman.fityatraapp.ui.MainActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.local.AppDatabase
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.repository.HealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


class StepTrackingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var appDatabase: AppDatabase
    private lateinit var healthRepository: HealthRepository

    private var stepCount = 0
    private var lowAccuracyStepCount = 0
    private var highAccuracyStepCount = 0
    private var mediumAccuracyStepCount = 0
    private var otherStepCount = 0
    private val STEP_UPDATE_THRESHOLD = 50

    override fun onCreate() {
        super.onCreate()
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fityatra_db"
        ).build()

        healthRepository = HealthRepository(appDatabase.healthDao())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setupStepDetector()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun setupStepDetector() {
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            Log.d("Steps", stepCount.toString())

            if (stepCount % STEP_UPDATE_THRESHOLD == 0) {
                updateStepCount()
            }
        }
    }

    private fun updateStepCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = getTodayStartMillis()
                val existingData = healthRepository.getHealthDataByDate(today)

                val updatedData = existingData?.copy(
                    stepCount = existingData.stepCount + stepCount
                ) ?: HealthData(
                    date = today,
                    stepCount = stepCount
                )

                healthRepository.insertOrUpdate(updatedData)

                // Reset counters
                stepCount = 0
                lowAccuracyStepCount = 0
                highAccuracyStepCount = 0
                mediumAccuracyStepCount = 0
                otherStepCount = 0

            } catch (e: Exception) {
                Log.e("StepService", "Failed to update DB", e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> otherStepCount++
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> lowAccuracyStepCount++
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> mediumAccuracyStepCount++
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> highAccuracyStepCount++
            }
        }
    }

    private fun startForegroundService() {
        val channelId = NOTIF_CHANNEL_ID
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channel = NotificationChannel(
            channelId,
            "Step Tracking Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.outline_notifications_24)
            .setContentTitle("FitYatra")
            .setContentText("Step tracking is running")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun getTodayStartMillis(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }

    companion object {
        private const val NOTIF_ID = 101
        private const val NOTIF_CHANNEL_ID = "Step_Tracking_Channel"
    }
}

