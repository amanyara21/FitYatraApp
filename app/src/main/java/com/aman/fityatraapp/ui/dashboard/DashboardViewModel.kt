package com.aman.fityatraapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.models.User
import com.aman.fityatraapp.utils.HealthApiResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

class DashboardViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _todayData = MutableLiveData<HealthApiResponse?>()
    val todayData: MutableLiveData<HealthApiResponse?> get() = _todayData


    fun fetchTodayData() {
        val currentUser = auth.currentUser
        currentUser?.let {
            db.child("todayData").child(it.uid).get().addOnSuccessListener { snapshot ->
                val data = snapshot.getValue<HealthApiResponse>()
                _todayData.value = data
            }.addOnFailureListener { _ ->
                // Handle error
            }
        }
    }


}