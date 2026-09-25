package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mealType: String, // "Norashta", "Tushlik", "Kechki ovqat", "Tamaddi"
    val foodName: String,
    val portionDescription: String = "",
    val calories: Int,
    val proteinG: Float,
    val fatG: Float,
    val carbsG: Float,
    val imageUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
