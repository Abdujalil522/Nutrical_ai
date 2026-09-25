package com.example.data.repository

import com.example.data.local.MealLog
import com.example.data.local.NutriDao
import com.example.data.local.UserProfile
import kotlinx.coroutines.flow.Flow

class NutriRepository(private val dao: NutriDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfile? = dao.getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    fun getMealsForDate(date: String): Flow<List<MealLog>> {
        return dao.getMealsForDate(date)
    }

    fun getAllMeals(): Flow<List<MealLog>> {
        return dao.getAllMeals()
    }

    suspend fun insertMeal(meal: MealLog): Long {
        return dao.insertMeal(meal)
    }

    suspend fun updateMeal(meal: MealLog) {
        dao.updateMeal(meal)
    }

    suspend fun deleteMeal(meal: MealLog) {
        dao.deleteMeal(meal)
    }

    suspend fun deleteMealById(id: Long) {
        dao.deleteMealById(id)
    }

    fun getWaterLogForDate(date: String): Flow<com.example.data.local.WaterLog?> {
        return dao.getWaterLogForDate(date)
    }

    suspend fun getWaterLogForDateOnce(date: String): com.example.data.local.WaterLog? {
        return dao.getWaterLogForDateOnce(date)
    }

    suspend fun saveWaterLog(waterLog: com.example.data.local.WaterLog) {
        dao.saveWaterLog(waterLog)
    }

    suspend fun clearAllData() {
        dao.clearAllMeals()
        dao.clearAllWaterLogs()
    }
}
