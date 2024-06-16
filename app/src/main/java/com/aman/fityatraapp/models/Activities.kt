package com.aman.fityatraapp.models
import java.io.Serializable

data class Activities(
    val activity: String = "",
    val description: String = "",
    val backgroundImage: String = "",
    val image: String = ""
) : Serializable