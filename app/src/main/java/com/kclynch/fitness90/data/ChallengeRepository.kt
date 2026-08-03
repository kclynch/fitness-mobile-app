package com.kclynch.fitness90.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ChallengeRepository(private val db: AppDatabase) {

    val challenge: Flow<Challenge?> = db.challengeDao().observeChallenge()
    val tasks: Flow<List<ChecklistTask>> = db.taskDao().observeTasks()
    val allChecks: Flow<List<DayCheck>> = db.dayCheckDao().observeAllChecks()

    fun checksForDay(dayNumber: Int): Flow<List<DayCheck>> =
        db.dayCheckDao().observeChecksForDay(dayNumber)

    suspend fun startChallenge(startDate: LocalDate, totalDays: Int = 90) {
        db.challengeDao().upsert(Challenge(startEpochDay = startDate.toEpochDay(), totalDays = totalDays))
        if (db.taskDao().observeTasks().first().isEmpty()) {
            DEFAULT_TASKS.forEachIndexed { index, title ->
                db.taskDao().insert(ChecklistTask(title = title, sortOrder = index))
            }
        }
    }

    suspend fun updateStartDate(startDate: LocalDate) {
        val current = db.challengeDao().observeChallenge().first()
        db.challengeDao().upsert(
            Challenge(startEpochDay = startDate.toEpochDay(), totalDays = current?.totalDays ?: 90)
        )
    }

    suspend fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val nextOrder = db.taskDao().maxSortOrder() + 1
        db.taskDao().insert(ChecklistTask(title = trimmed, sortOrder = nextOrder))
    }

    suspend fun renameTask(task: ChecklistTask, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        db.taskDao().update(task.copy(title = trimmed))
    }

    suspend fun deleteTask(task: ChecklistTask) {
        db.taskDao().delete(task)
    }

    suspend fun setChecked(taskId: Long, dayNumber: Int, checked: Boolean) {
        db.dayCheckDao().upsert(DayCheck(taskId = taskId, dayNumber = dayNumber, isChecked = checked))
    }

    /**
     * Flips whatever is currently in the database, rather than trusting a
     * checked/unchecked value computed earlier (e.g. by the widget, which
     * can only render a snapshot and may be toggled before it's refreshed).
     */
    suspend fun toggleChecked(taskId: Long, dayNumber: Int) {
        val current = db.dayCheckDao().find(taskId, dayNumber)?.isChecked ?: false
        db.dayCheckDao().upsert(DayCheck(taskId = taskId, dayNumber = dayNumber, isChecked = !current))
    }

    companion object {
        val DEFAULT_TASKS = listOf(
            "Workout for 45 minutes",
            "Drink a gallon of water",
            "Follow a diet plan",
            "Read 10 pages of a book",
            "Take a progress photo"
        )
    }
}
