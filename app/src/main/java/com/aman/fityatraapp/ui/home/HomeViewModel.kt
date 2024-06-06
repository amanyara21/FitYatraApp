package com.aman.fityatraapp.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aman.fityatraapp.models.User
import com.aman.fityatraapp.utils.HealthApiResponse

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

class HomeViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _userData = MutableLiveData<User?>()
    val userData: MutableLiveData<User?> get() = _userData

    private val _todayData = MutableLiveData<HealthApiResponse?>()
    val todayData: MutableLiveData<HealthApiResponse?> get() = _todayData


    fun fetchUserData() {
        val currentUser = auth.currentUser
        currentUser?.let {
            db.child("users").child(it.uid).get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue<User>()
                _userData.value = user
            }.addOnFailureListener {
                // Handle error
            }
        }
    }

}
