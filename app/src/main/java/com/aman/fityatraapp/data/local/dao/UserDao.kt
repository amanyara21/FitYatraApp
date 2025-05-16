package com.aman.fityatraapp.data.local.dao

import androidx.room.*
import com.aman.fityatraapp.data.local.model.UserData

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userData: UserData)

    @Query("SELECT * FROM user_data LIMIT 1")
    suspend fun getUserData(): UserData?

    @Query("SELECT EXISTS(SELECT 1 FROM user_data LIMIT 1)")
    suspend fun isUserDataAvailable(): Boolean

    @Query("DELETE FROM user_data")
    suspend fun deleteAll()
}
