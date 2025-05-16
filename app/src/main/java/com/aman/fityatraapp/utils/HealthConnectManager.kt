package com.aman.fityatraapp.utils

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit


class HealthConnectManager(private val context: Context) {

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (HealthConnectClient.sdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }


    val permissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    // Create the permissions launcher
    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    private val requestPermissions = (context as ComponentActivity).registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(permissions)) {
            // Permissions successfully granted
            CoroutineScope(Dispatchers.Main).launch {
                fetchHealthData()
            }
        } else {
            // Lack of required permissions
            Log.w("HealthConnectManager", "Required permissions are not granted.")
        }
    }

    suspend fun checkPermissionsAndRun() {
        val granted = healthConnectClient?.permissionController?.getGrantedPermissions()
        if (granted!!.containsAll(permissions)) {
            fetchHealthData()
        } else {
            requestPermissions.launch(permissions)
        }
    }

    private suspend fun fetchHealthData() {
        val client = healthConnectClient
        if (client == null) {
            Log.e("HealthConnectManager", "Health Connect SDK is not available.")
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
                val stepsResult = client.readRecords(stepCountQuery)
                val weightResult = client.readRecords(weightQuery)
                val glucoseResult = client.readRecords(glucoseLevelQuery)
                val caloriesBurnedResult = client.readRecords(calorieBurnQuery)

                Data(stepsResult, weightResult, glucoseResult, caloriesBurnedResult)
            }
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching health data", e)
            return
        }

        val stepCount = results.stepsResult.records.sumOf { it.count }
        val weight = results.weightResult.records.lastOrNull()?.weight?.inKilograms?.toFloat()
        val glucoseLevel = results.glucoseResult.records.lastOrNull()?.level?.inMilligramsPerDeciliter
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
//        sqliteUtils.addOrUpdateHealthData(
//            exercises = null,
//            meals = null,
//            stepCount = stepCount,
//            calorieIntake = null,
//            calorieBurn = caloriesBurned,
//            weight = weight,
//            glucoseLevel = glucoseLevel,
//            onSuccess = {
//                Log.d("Firebase", "Health data successfully written!")
//            },
//            onFailure = { e ->
//                Log.w("Firebase", "Error writing health data", e)
//            }
//        )
    }
}



data class Data(
    val stepsResult: ReadRecordsResponse<StepsRecord>,
    val weightResult: ReadRecordsResponse<WeightRecord>,
    val glucoseResult: ReadRecordsResponse<BloodGlucoseRecord>,
    val caloriesBurnedResult: ReadRecordsResponse<TotalCaloriesBurnedRecord>
)