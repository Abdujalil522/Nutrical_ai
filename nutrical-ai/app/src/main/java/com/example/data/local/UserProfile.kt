package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val email: String = "",
    val name: String = "",
    val gender: String = "Erkak", // "Erkak" or "Ayol"
    val age: Int = 25,
    val weightKg: Float = 75.0f,
    val heightCm: Float = 175.0f,
    val targetGoal: String = "Ozish", // "Ozish", "Vazn yig'ish", "Baqquvvat bo'lish", "Formani saqlash"
    val targetWeightKg: Float = 70.0f,
    val activityLevel: String = "O'rtacha faol",
    val bmr: Int = 1720,
    val tdee: Int = 2400,
    val targetCalories: Int = 1900,
    val targetProteinG: Int = 140,
    val targetFatG: Int = 55,
    val targetCarbsG: Int = 210,
    val dailyWaterGoalMl: Int = 2500,
    val waterReminderEnabled: Boolean = true,
    val mealReminderEnabled: Boolean = true,
    val isOnboarded: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
