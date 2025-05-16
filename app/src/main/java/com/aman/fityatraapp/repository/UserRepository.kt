package com.aman.fityatraapp.repository

import com.aman.fityatraapp.data.local.dao.UserDao
import com.aman.fityatraapp.data.local.model.UserData
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {
    suspend fun insertUserData(userData: UserData) {
        userDao.insertUserData(userData)
    }

    suspend fun getUserData(): UserData? {
        return userDao.getUserData()
    }

    suspend fun isUserDataAvailable(): Boolean {
        return userDao.isUserDataAvailable()
    }
    suspend fun deleteAll(){
        userDao.deleteAll()
    }
}