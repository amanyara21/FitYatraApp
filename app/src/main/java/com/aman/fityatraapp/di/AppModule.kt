package com.aman.fityatraapp.di

import android.content.Context
import androidx.room.Room
import com.aman.fityatraapp.data.local.AppDatabase
import com.aman.fityatraapp.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fityatra_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHealthDao(appDatabase: AppDatabase): HealthDao = appDatabase.healthDao()

    @Provides
    @Singleton
    fun provideUserDao(appDatabase: AppDatabase): UserDao = appDatabase.userDao()

    @Provides
    @Singleton
    fun provideGoalDao(appDatabase: AppDatabase): GoalDao = appDatabase.goalDao()

    @Provides
    @Singleton
    fun provideMealDao(appDatabase: AppDatabase): MealDao = appDatabase.mealDao()

    @Provides
    @Singleton
    fun provideDietPlanDao(appDatabase: AppDatabase): DietPlanDao = appDatabase.dietPlanDao()
}
