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
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aman.fityatraapp.activities.MainActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.SQLiteUtils


class StepTrackingService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCount = 0
    private var lowAccuracyStepCount = 0
    private var highAccuracyStepCount = 0
    private var mediumAccuracyStepCount = 0
    private var otherStepCount = 0
    private val STEP_UPDATE_THRESHOLD = 50
    private var sqlUtils = SQLiteUtils(this)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Step_Tracking_Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(
            this,
            NOTIF_CHANNEL_ID
        )
            .setOngoing(true)
            .setSmallIcon(R.drawable.outline_notifications_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Service is running in the background")
            .setContentIntent(pendingIntent)
            .setSound(null)
            .build()

        startForeground(NOTIF_ID, notification)
    }


    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setupStepDetector()

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    private fun setupStepDetector() {
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("Event is ", event?.sensor?.type.toString())
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            Log.d("Steps", stepCount.toString())
            if (stepCount % STEP_UPDATE_THRESHOLD == 0) {
                updateStepCount()
            }
        }
    }

    private fun updateStepCount() {
        makeApiCallForStepCount()
    }

    private fun makeApiCallForStepCount() {
        sqlUtils.addOrUpdateHealthData(null, null, stepCount, 0, 0, null, null, onSuccess = {
            stepCount = 0
            lowAccuracyStepCount = 0
            highAccuracyStepCount = 0
            mediumAccuracyStepCount = 0
            otherStepCount = 0
        }, onFailure = {})
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Sensor Type is : ", sensor?.type.toString())
        when (sensor?.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                when (accuracy) {
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                        Log.d("Sensor Accuracy", "Step detector sensor accuracy is unreliable.")
                        otherStepCount++
                    }

                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                        Log.d("Sensor Accuracy", "Step detector sensor accuracy is low.")
                        lowAccuracyStepCount++
                    }

                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                        Log.d("Sensor Accuracy", "Step detector sensor accuracy is medium.")
                        mediumAccuracyStepCount++
                    }

                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                        Log.d("Sensor Accuracy", "Step detector sensor accuracy is high.")
                        highAccuracyStepCount++
                    }
                }
            }
        }
    }

    companion object {
        private const val NOTIF_ID = 1
        private const val NOTIF_CHANNEL_ID = "Step_Tracking_Channel"
    }

}
