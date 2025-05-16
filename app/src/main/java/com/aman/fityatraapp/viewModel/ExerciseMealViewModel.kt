package com.aman.fityatraapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExerciseMealViewModel @Inject constructor(
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _todayData = MutableLiveData<HealthData>()
    val todayData: LiveData<HealthData> = _todayData

    init {
        loadTodayData()
    }
    private fun loadTodayData() {
        viewModelScope.launch {
            val today = getTodayStartMillis()
            val data = healthRepository.getHealthDataByDate(today)
            _todayData.postValue(data)
        }
    }

    private fun getTodayStartMillis(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }
}
