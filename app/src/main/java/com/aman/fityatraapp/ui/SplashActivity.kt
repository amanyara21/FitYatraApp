package com.aman.fityatraapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.PreferencesHelper
import com.aman.fityatraapp.viewModel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private val splashViewModel: SplashViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        lifecycleScope.launch {
            if (preferencesHelper.isFirstLaunch()) {
                splashViewModel.saveDefaultDietPlanToRoom()
                preferencesHelper.setFirstLaunchDone()
            }
            delay(2000)
            startActivity(Intent(this@SplashActivity, ChatBotActivity::class.java))
            finish()
        }
    }
}
