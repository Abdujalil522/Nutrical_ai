package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealLog): Long

    @Update
    suspend fun updateMeal(meal: MealLog)

    @Delete
    suspend fun deleteMeal(meal: MealLog)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getMealsForDate(date: String): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMeals(limit: Int): Flow<List<MealLog>>

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLogForDate(date: String): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    suspend fun getWaterLogForDateOnce(date: String): WaterLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWaterLog(waterLog: WaterLog)

    @Query("DELETE FROM meal_logs")
    suspend fun clearAllMeals()

    @Query("DELETE FROM water_logs")
    suspend fun clearAllWaterLogs()
}
