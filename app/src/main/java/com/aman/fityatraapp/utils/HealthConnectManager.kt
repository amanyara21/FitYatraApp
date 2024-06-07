package com.aman.fityatraapp.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    private val firebaseUtils = FirebaseUtils()

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    private var permissionRequestLauncher: ActivityResultLauncher<Array<String>>? = null

    fun registerPermissionRequestLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        permissionRequestLauncher = launcher
    }

    suspend fun fetchHealthData() {
        if (!hasAllPermissions(PERMISSIONS)) {
            permissionRequestLauncher?.launch(PERMISSIONS.toTypedArray())
            return
        }

        val startTime = Instant.now().minus(7, ChronoUnit.DAYS)
        val endTime = Instant.now()

        val stepCountQuery = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val weightQuery = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val glucoseLevelQuery = ReadRecordsRequest(
            recordType = BloodGlucoseRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val calorieBurnQuery = ReadRecordsRequest(
            recordType = TotalCaloriesBurnedRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        val results = try {
            withContext(Dispatchers.IO) {
                val stepsResult = healthConnectClient.readRecords(stepCountQuery)
                val weightResult = healthConnectClient.readRecords(weightQuery)
                val glucoseResult = healthConnectClient.readRecords(glucoseLevelQuery)
                val caloriesBurnedResult = healthConnectClient.readRecords(calorieBurnQuery)

                Data(stepsResult, weightResult, glucoseResult, caloriesBurnedResult)
            }
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching health data", e)
            return
        }

        val stepCount = results.stepsResult.records.sumOf { it.count }
        val weight = results.weightResult.records.lastOrNull()?.weight?.inKilograms?.toFloat()
        val glucoseLevel =
            results.glucoseResult.records.lastOrNull()?.level?.inMilligramsPerDeciliter
        val caloriesBurned = results.caloriesBurnedResult.records.lastOrNull()?.energy?.inCalories

        saveDataToFirebase(
            stepCount.toInt(),
            weight,
            glucoseLevel?.toFloat(),
            caloriesBurned?.toInt()
        )
    }

    private fun saveDataToFirebase(
        stepCount: Int,
        weight: Float?,
        glucoseLevel: Float?,
        caloriesBurned: Int?
    ) {
        firebaseUtils.addOrUpdateHealthData(
            exercises = null,
            meals = null,
            stepCount = stepCount,
            calorieIntake = null,
            calorieBurn = caloriesBurned,
            weight = weight,
            glucoseLevel = glucoseLevel,
            onSuccess = {
                Log.d("Firebase", "Health data successfully written!")
            },
            onFailure = { e ->
                Log.w("Firebase", "Error writing health data", e)
            }
        )
    }

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContracts.RequestMultiplePermissions {
        return ActivityResultContracts.RequestMultiplePermissions()
    }
}

data class Data(
    val stepsResult: ReadRecordsResponse<StepsRecord>,
    val weightResult: ReadRecordsResponse<WeightRecord>,
    val glucoseResult: ReadRecordsResponse<BloodGlucoseRecord>,
    val caloriesBurnedResult: ReadRecordsResponse<TotalCaloriesBurnedRecord>
)