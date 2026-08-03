package com.kclynch.fitness90.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeDates
import com.kclynch.fitness90.data.ChallengeRepository
import com.kclynch.fitness90.data.ChecklistTask
import com.kclynch.fitness90.widget.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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
            val status = ChallengeDates.status(challenge)
            val selectedDay = (override ?: status.todayDayNumber).coerceIn(1, challenge.totalDays)

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
                startDate = status.startDate,
                endDate = status.endDate,
                totalDays = challenge.totalDays,
                todayDayNumber = status.todayDayNumber,
                daysRemaining = status.daysRemaining,
                isFinished = status.isFinished,
                selectedDay = selectedDay,
                tasks = tasks,
                checkedTaskIds = checkedTaskIds,
                dayProgress = dayProgress
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChallengeUiState.Loading)

    fun startChallenge(startDate: LocalDate) {
        viewModelScope.launch {
            repository.startChallenge(startDate)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun updateStartDate(startDate: LocalDate) {
        viewModelScope.launch {
            repository.updateStartDate(startDate)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun selectDay(day: Int) {
        selectedDayOverride.value = day
    }

    fun jumpToToday() {
        selectedDayOverride.value = null
    }

    fun toggleTask(taskId: Long, dayNumber: Int, checked: Boolean) {
        viewModelScope.launch {
            repository.setChecked(taskId, dayNumber, checked)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            repository.addTask(title)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun renameTask(task: ChecklistTask, newTitle: String) {
        viewModelScope.launch {
            repository.renameTask(task, newTitle)
            WidgetUpdater.refresh(getApplication())
        }
    }

    fun deleteTask(task: ChecklistTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            WidgetUpdater.refresh(getApplication())
        }
    }
}
