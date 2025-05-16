package com.aman.fityatraapp.repository

import com.aman.fityatraapp.data.local.dao.HealthDao
import com.aman.fityatraapp.data.local.model.HealthData
import javax.inject.Inject

class HealthRepository @Inject constructor(private val healthDao: HealthDao) {


    suspend fun insertOrUpdate(healthData: HealthData){
        healthDao.insertOrUpdate(healthData)
    }
    suspend fun getHealthDataByDate(date: Long): HealthData? {
        return healthDao.getHealthDataByDate(date)
    }

    suspend fun getLast7DaysHealthData(): List<HealthData> {
        return healthDao.getLast7DaysHealthData()
    }

    suspend fun deleteByDate(date: Long){
        healthDao.deleteByDate(date)
    }

    suspend fun clearAll(){
        healthDao.clearAll()
    }
}