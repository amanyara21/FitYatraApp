package com.aman.fityatraapp.data.api

data class Item(val item: String, val quantity: Int)
data class exerItem(val exercise_name: String, val duration: Int, val weight: Int = 60)