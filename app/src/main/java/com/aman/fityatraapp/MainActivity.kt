package com.aman.fityatraapp

import android.content.Intent
import android.content.pm.PackageManager
import android.health.connect.HealthPermissions
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.platform.client.permission.Permission
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aman.fityatraapp.databinding.ActivityMainBinding
import com.aman.fityatraapp.ui.DietPlannerFragment
import com.aman.fityatraapp.ui.exercise.ExerciseFragment
import com.aman.fityatraapp.utils.HealthConnectManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var healthConnectManager: HealthConnectManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthConnectManager = HealthConnectManager(this)
        healthConnectManager.registerPermissionRequestLauncher(requestPermissionLauncher)

        lifecycleScope.launch {
            healthConnectManager.fetchHealthData()
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_diet,
                R.id.navigation_posture
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.fabChat.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
        }

        if (intent.getBooleanExtra("SHOW_DIET_PLAN_FRAGMENT", false)) {
            navController.navigate(R.id.navigation_diet)
        } else if (intent.getBooleanExtra("EXERCISE_FRAGMENT", false)) {
            navController.navigate(R.id.navigation_notifications)
        } else if (intent.getBooleanExtra("POSTURE_FRAGMENT", false)) {
            navController.navigate(R.id.navigation_posture)
        }

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Navigate to the home fragment
                    navController.navigate(R.id.navigation_home)
                    true
                }

                R.id.navigation_dashboard -> {
                    // Navigate to the dashboard fragment
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }

                R.id.navigation_notifications -> {
                    // Navigate to the notifications fragment
                    navController.navigate(R.id.navigation_notifications)
                    true
                }

                R.id.navigation_posture -> {
                    // Navigate to the notifications fragment
                    navController.navigate(R.id.navigation_posture)
                    true
                }

                R.id.navigation_diet -> {
                    // Navigate to the notifications fragment
                    navController.navigate(R.id.navigation_diet)
                    true
                }
                // Add cases for other bottom tabs if needed
                else -> false
            }
        }

        val availability = HealthConnectClient.sdkStatus(this)
        if (availability==1 || availability==2){
            Toast.makeText(this, "Download Health connect App to integrate", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Health connect App integrated", Toast.LENGTH_SHORT).show()
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // Permissions granted
                lifecycleScope.launch {
                    healthConnectManager.fetchHealthData()
                }
            } else {
                // Handle the case where permissions are not granted
                Log.e("MainActivity", "Permissions not granted")
            }
        }



}


