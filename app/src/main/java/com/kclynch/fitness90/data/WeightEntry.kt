package com.kclynch.fitness90.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single weigh-in for a given calendar day. Unique on epochDay so
 * logging a second weight for the same day edits it in place.
 */
@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["epochDay"], unique = true)]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val weightLbs: Float
)
