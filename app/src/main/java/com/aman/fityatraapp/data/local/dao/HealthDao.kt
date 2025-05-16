package com.aman.fityatraapp.data.local.dao

import androidx.room.*
import com.aman.fityatraapp.data.local.model.HealthData
@Dao
interface HealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(healthData: HealthData)

    @Query("SELECT * FROM health WHERE date = :date")
    suspend fun getHealthDataByDate(date: Long): HealthData?

    @Query("SELECT * FROM health ORDER BY date DESC LIMIT 7")
    suspend fun getLast7DaysHealthData(): List<HealthData>

    @Query("DELETE FROM health WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("DELETE FROM health")
    suspend fun clearAll()
}
