package com.kclynch.fitness90.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeRepository
import com.kclynch.fitness90.data.ChecklistTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DayProgress(val dayNumber: Int, val completed: Int, val total: Int)

sealed interface ChallengeUiState {
    data object Loading : ChallengeUiState
    data object NotStarted : ChallengeUiState
    data class Active(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val totalDays: Int,
        val todayDayNumber: Int,
        val daysRemaining: Int,
        val isFinished: Boolean,
        val selectedDay: Int,
        val tasks: List<ChecklistTask>,
        val checkedTaskIds: Set<Long>,
        val dayProgress: Map<Int, DayProgress>
    ) : ChallengeUiState
}

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChallengeRepository(AppDatabase.getInstance(application))

    // null means "follow today" until the user explicitly picks a day.
    private val selectedDayOverride = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<ChallengeUiState> = combine(
        repository.challenge,
        repository.tasks,
        repository.allChecks,
        selectedDayOverride
    ) { challenge, tasks, checks, override ->
        if (challenge == null) {
            ChallengeUiState.NotStarted
        } else {
            val startDate = LocalDate.ofEpochDay(challenge.startEpochDay)
            val endDate = startDate.plusDays((challenge.totalDays - 1).toLong())
            val today = LocalDate.now()

            val rawDayNumber = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
            val todayDayNumber = rawDayNumber.coerceIn(1, challenge.totalDays)
            val isFinished = today.isAfter(endDate)
            val daysRemaining = if (isFinished) {
                0
            } else {
                ChronoUnit.DAYS.between(today, endDate).toInt().coerceAtLeast(0)
            }

            val selectedDay = (override ?: todayDayNumber).coerceIn(1, challenge.totalDays)

            val checksByDay = checks.groupBy { it.dayNumber }
            val dayProgress = (1..challenge.totalDays).associateWith { day ->
                val dayChecks = checksByDay[day].orEmpty()
                DayProgress(day, dayChecks.count { it.isChecked }, tasks.size)
            }
            val checkedTaskIds = checksByDay[selectedDay].orEmpty()
                .filter { it.isChecked }
                .map { it.taskId }
                .toSet()

            ChallengeUiState.Active(
                startDate = startDate,
                endDate = endDate,
                totalDays = challenge.totalDays,
                todayDayNumber = todayDayNumber,
                daysRemaining = daysRemaining,
                isFinished = isFinished,
                selectedDay = selectedDay,
                tasks = tasks,
                checkedTaskIds = checkedTaskIds,
                dayProgress = dayProgress
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChallengeUiState.Loading)

    fun startChallenge(startDate: LocalDate) {
        viewModelScope.launch { repository.startChallenge(startDate) }
    }

    fun updateStartDate(startDate: LocalDate) {
        viewModelScope.launch { repository.updateStartDate(startDate) }
    }

    fun selectDay(day: Int) {
        selectedDayOverride.value = day
    }

    fun jumpToToday() {
        selectedDayOverride.value = null
    }

    fun toggleTask(taskId: Long, dayNumber: Int, checked: Boolean) {
        viewModelScope.launch { repository.setChecked(taskId, dayNumber, checked) }
    }

    fun addTask(title: String) {
        viewModelScope.launch { repository.addTask(title) }
    }

    fun renameTask(task: ChecklistTask, newTitle: String) {
        viewModelScope.launch { repository.renameTask(task, newTitle) }
    }

    fun deleteTask(task: ChecklistTask) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}
