package com.aman.fityatraapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aman.fityatraapp.R
import com.aman.fityatraapp.databinding.ActivityMainBinding
import com.aman.fityatraapp.services.StepTrackingService
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.FirebaseUtils
import com.aman.fityatraapp.utils.HealthConnectManager
import com.aman.fityatraapp.utils.PermissionManager
import com.aman.fityatraapp.utils.SQLiteUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), PermissionManager.PermissionCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var sqLiteUtils: SQLiteUtils
    private lateinit var permissionManager:PermissionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager()
        permissionManager.initPermissionLauncher(this, this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        sqLiteUtils = SQLiteUtils(this)
        val isDataAvailable = sqLiteUtils.isUserDataAvailable()

        if (!isDataAvailable) {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
        }

        healthConnectManager = HealthConnectManager(this)
        lifecycleScope.launch {
            val response = apiService.startServer()
//            healthConnectManager.checkPermissionsAndRun()
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

                else -> false
            }
        }


    }
    override fun onPermissionsGranted() {
        startService(Intent(this, StepTrackingService::class.java))

    }


}


