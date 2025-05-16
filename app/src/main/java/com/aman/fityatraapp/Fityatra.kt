package com.aman.fityatraapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitYatra : Application(){
    override fun onCreate() {
        super.onCreate()
    }
}