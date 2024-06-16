package com.aman.fityatraapp.ui.exercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.models.Exercise
import com.aman.fityatraapp.utils.FirebaseUtils
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {

    private val firebaseUtils = FirebaseUtils()
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> get() = _exercises

    fun getExercises() {
        viewModelScope.launch {
            firebaseUtils.getAllExercises { exerciseList ->
                _exercises.postValue(exerciseList)
            }
        }
    }

}