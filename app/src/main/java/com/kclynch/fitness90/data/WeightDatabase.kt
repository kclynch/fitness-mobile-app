package com.kclynch.fitness90.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Deliberately a separate database file from [AppDatabase]. Adding a table
 * to the existing, already-deployed database would require a version bump
 * and a hand-written migration that can't be tested on a real device from
 * here — getting that wrong would risk the user's existing challenge
 * progress. A brand-new database for a brand-new feature has no such risk:
 * there's nothing to migrate.
 */
@Database(entities = [WeightEntry::class], version = 1, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: WeightDatabase? = null

        fun getInstance(context: Context): WeightDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
