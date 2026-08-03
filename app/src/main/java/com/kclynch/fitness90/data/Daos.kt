package com.kclynch.fitness90.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge WHERE id = 1 LIMIT 1")
    fun observeChallenge(): Flow<Challenge?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(challenge: Challenge)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, id ASC")
    fun observeTasks(): Flow<List<ChecklistTask>>

    @Insert
    suspend fun insert(task: ChecklistTask): Long

    @Update
    suspend fun update(task: ChecklistTask)

    @Delete
    suspend fun delete(task: ChecklistTask)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM tasks")
    suspend fun maxSortOrder(): Int
}

@Dao
interface DayCheckDao {
    @Query("SELECT * FROM day_checks WHERE dayNumber = :dayNumber")
    fun observeChecksForDay(dayNumber: Int): Flow<List<DayCheck>>

    @Query("SELECT * FROM day_checks")
    fun observeAllChecks(): Flow<List<DayCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: DayCheck)
}
