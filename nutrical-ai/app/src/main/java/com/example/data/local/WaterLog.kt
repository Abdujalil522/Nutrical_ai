package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val amountMl: Int = 0,
    val targetMl: Int = 2500,
    val updatedAt: Long = System.currentTimeMillis()
)
