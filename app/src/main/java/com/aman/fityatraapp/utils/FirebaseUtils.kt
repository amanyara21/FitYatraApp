package com.aman.fityatraapp.utils

import android.content.Context
import android.util.Log
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.DietPlan
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class FirebaseUtils {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    companion object {
        const val RC_SIGN_IN = 9001
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

//    fun handleSignInResult(data: Intent?): Task<AuthCredential> {
//        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//        return task.continueWith { task ->
//            val account = task.result as GoogleSignInAccount
//            GoogleAuthProvider.getCredential(account.idToken, null)
//        }
//    }

    fun getUserData(uid: String): Task<DataSnapshot> {
        val userRef: DatabaseReference = database.child("users").child(uid)
        return userRef.get()
    }

    fun saveUserData(uid: String, userDetails: Map<String, String>): Task<Void> {
        val userRef: DatabaseReference = database.child("users").child(uid)
        return userRef.setValue(userDetails)
    }

    fun addOrUpdateHealthData(
        exercises: List<ExerciseAdd>?,
        meals: List<MealAdd>?,
        stepCount: Int?,
        calorieIntake: Int?,
        calorieBurn: Int?,
        weight: Float?,
        glucoseLevel: Float?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        Log.d("addOrUpdateHealthData", "User ID: $userId")

        val today = Date()
        val startOfToday = Calendar.getInstance().apply {
            time = today
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        database.child(userId).child("health").orderByChild("date").startAt(startOfToday.toDouble()).get()
            .addOnSuccessListener { snapshot ->
                Log.d("addOrUpdateHealthData", "Snapshot exists: ${snapshot.exists()}, children count: ${snapshot.childrenCount}")
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val existingData = snapshot.children.first().getValue(HealthData::class.java)
                    existingData?.let { data ->
                        Log.d("addOrUpdateHealthData", "Existing data found: $data")

                        // Update exercises
                        exercises?.forEach { exercise ->
                            val existingExerciseIndex = data.exercises?.indexOfFirst {
                                it.exerciseName.equals(exercise.exerciseName, ignoreCase = true)
                            }
                            if (existingExerciseIndex != null && existingExerciseIndex != -1) {
                                data.exercises[existingExerciseIndex].duration =
                                    data.exercises[existingExerciseIndex].duration.plus(exercise.duration)
                            } else {
                                data.exercises?.add(exercise)
                            }
                        }

                        // Update meals
                        meals?.forEach { meal ->
                            val existingMealIndex = data.meals?.indexOfFirst {
                                it.dishName.equals(meal.dishName, ignoreCase = true)
                            }
                            if (existingMealIndex != null && existingMealIndex != -1) {
                                data.meals[existingMealIndex].quantity =
                                    data.meals[existingMealIndex].quantity.plus(meal.quantity)
                            } else {
                                data.meals?.add(meal)
                            }
                        }

                        data.stepCount = (data.stepCount ?: 0) + (stepCount ?: 0)
                        data.calorieIntake = (data.calorieIntake ?: 0) + (calorieIntake ?: 0)
                        data.calorieBurn = (data.calorieBurn ?: 0) + (calorieBurn ?: 0)
                        data.weight = weight ?: data.weight
                        data.glucoseLevel = glucoseLevel ?: data.glucoseLevel

                        database.child("$userId/health/${snapshot.children.first().key}")
                            .setValue(data)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e ->
                                Log.e("addOrUpdateHealthData", "Error updating data: ${e.message}", e)
                                onFailure(e)
                            }
                    } ?: onFailure(Exception("Error updating data: No existing data found"))
                } else {
                    Log.d("addOrUpdateHealthData", "No existing data found for today. Creating new entry.")

                    val healthData = HealthData(
                        exercises?.toMutableList(),
                        meals?.toMutableList(),
                        stepCount,
                        calorieIntake,
                        calorieBurn,
                        weight,
                        glucoseLevel,
                        today.time
                    )
                    database.child("$userId/health").push().setValue(healthData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            Log.e("addOrUpdateHealthData", "Error creating new data: ${e.message}", e)
                            onFailure(e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("addOrUpdateHealthData", "Error retrieving data: ${e.message}", e)
                onFailure(e)
            }
    }


    fun getTodayHealthData(
        onSuccess: (HealthData?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val today = Date()
        val startOfToday = Calendar.getInstance().apply {
            time = today
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        database.child("$userId/health").orderByChild("date").startAt(startOfToday.toDouble()).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val healthData = snapshot.children.first().getValue(HealthData::class.java)
                    onSuccess(healthData)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getLast7DaysData(
        onSuccess: (List<HealthData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val today = Date()
        val startOfLast7Days = Calendar.getInstance().apply {
            time = today
            add(Calendar.DATE, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        database.child("$userId/health").orderByChild("date").startAt(startOfLast7Days.toDouble()).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.children.mapNotNull { it.getValue(HealthData::class.java) }
                onSuccess(data)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }




    fun getAllExercises(callback: (List<Exercise>) -> Unit) {
        val database: DatabaseReference = database.child("Exercises")

        val exercisesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exercisesList = mutableListOf<Exercise>()
                for (exerciseSnapshot in dataSnapshot.children) {
                    val name = exerciseSnapshot.key ?: ""
                    val exercise =
                        exerciseSnapshot.getValue(Exercise::class.java)?.copy(name = name)
                    if (exercise != null) {
                        exercisesList.add(exercise)
                    }
                }
                callback(exercisesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("getAllExercises:onCancelled: ${databaseError.toException()}")
            }
        }
        database.addValueEventListener(exercisesListener)
    }

    fun getExerciseByName(name: String, callback: (Exercise?) -> Unit) {
        val database: DatabaseReference = database.child("Exercises").child(name)

        val exerciseListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exercise = dataSnapshot.getValue(Exercise::class.java)?.copy(name = name)
                callback(exercise)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("getExerciseByName:onCancelled: ${databaseError.toException()}")
            }
        }
        database.addListenerForSingleValueEvent(exerciseListener)
    }

    fun getActivitiesByCure(cure: String,  callback: (List<Activities>) -> Unit) {
        database.child("Activities").child(cure).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val activities = dataSnapshot.children.map { it.getValue(Activities::class.java)!! }
                callback(activities)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }


    fun saveDietPlan(dietPlan: List<DietPlan>) {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        if (dietPlan.isNotEmpty()) {
            val firstDietPlan = dietPlan[0]

            try {
                database.child("$uid/dietplan").setValue(firstDietPlan)
                Log.d("FirebaseUtils", "Diet plan saved successfully")
            } catch (e: Exception) {
                Log.e("FirebaseUtils", "Error saving diet plan: ${e.message}")
                throw e
            }
        } else {
            Log.e("FirebaseUtils", "Diet plan list is empty")
        }
    }





}
