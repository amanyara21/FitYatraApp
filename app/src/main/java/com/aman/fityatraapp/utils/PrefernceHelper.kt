package com.aman.fityatraapp.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fit_yatra_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean = prefs.getBoolean("first_launch", true)

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean("first_launch", false).apply()
    }
}
