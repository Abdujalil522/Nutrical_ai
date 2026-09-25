package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class, MealLog::class, WaterLog::class],
    version = 2,
    exportSchema = false
)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao

    companion object {
        @Volatile
        private var INSTANCE: NutriDatabase? = null

        fun getInstance(context: Context): NutriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriDatabase::class.java,
                    "nutrical_ai.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
