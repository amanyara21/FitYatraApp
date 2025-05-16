package com.aman.fityatraapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val _last7DaysData = MutableLiveData<List<HealthData>>()
    val last7DaysData: LiveData<List<HealthData>> = _last7DaysData

    fun loadData() {
        viewModelScope.launch {
            val data = repository.getLast7DaysHealthData()
            Log.d("HealthData", data.toString())
            _last7DaysData.postValue(data)
        }
    }
}
