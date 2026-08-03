package com.kclynch.fitness90.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Singleton row (id is always 1) describing the active 90-day challenge.
 */
@Entity(tableName = "challenge")
data class Challenge(
    @PrimaryKey
    val id: Int = 1,
    val startEpochDay: Long,
    val totalDays: Int = 90
)

/**
 * A single recurring checklist item that shows up on every day of the challenge.
 */
@Entity(tableName = "tasks")
data class ChecklistTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sortOrder: Int
)

/**
 * Whether a given task was completed on a given day (1-based day number).
 * Unique on (taskId, dayNumber) so toggling a checkbox is a simple upsert.
 */
@Entity(
    tableName = "day_checks",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistTask::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId", "dayNumber"], unique = true),
        Index(value = ["taskId"])
    ]
)
data class DayCheck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val dayNumber: Int,
    val isChecked: Boolean = false
)
