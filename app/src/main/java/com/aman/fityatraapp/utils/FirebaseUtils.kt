package com.aman.fityatraapp.utils


import android.util.Log
import com.aman.fityatraapp.models.Activities
import com.aman.fityatraapp.models.Exercise
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseUtils {

    
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    fun getAllExercises(callback: (List<Exercise>) -> Unit) {
        val database: DatabaseReference = database.child("Exercises")

        val exercisesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exercisesList = mutableListOf<Exercise>()
                for (exerciseSnapshot in dataSnapshot.children) {
                    val name = exerciseSnapshot.key ?: ""
                    val exercise =
                        exerciseSnapshot.getValue(Exercise::class.java)?.copy(name = name)
                    Log.d("exercisesList", exercise.toString())
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

    fun getActivitiesByCure(cure: String, callback: (List<Activities>) -> Unit) {
        database.child("Activities").child(cure)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val activities =
                        dataSnapshot.children.map { it.getValue(Activities::class.java)!! }
                    callback(activities)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
    }
}
