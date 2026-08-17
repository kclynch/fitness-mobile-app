package com.kclynch.fitness90.ui.weight

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeDates
import com.kclynch.fitness90.data.ChallengeRepository
import com.kclynch.fitness90.data.DayCompletionCategory
import com.kclynch.fitness90.data.WeightDatabase
import com.kclynch.fitness90.data.WeightEntry
import com.kclynch.fitness90.data.WeightRepository
import com.kclynch.fitness90.data.completionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeightUiState(
    val allEntries: List<WeightEntry> = emptyList(),
    val rangeStart: LocalDate = LocalDate.now().minusDays(29),
    val rangeEnd: LocalDate = LocalDate.now(),
    val entriesInRange: List<WeightEntry> = emptyList(),
    val weightChangeInRange: Float? = null,
    val dayCategoryInRange: Map<LocalDate, DayCompletionCategory> = emptyMap()
)

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeightRepository(WeightDatabase.getInstance(application))

    // Read-only reuse of the challenge data, purely to correlate day
    // quality with weight on the graph — this doesn't touch the challenge
    // database's schema, so it carries none of the migration risk that
    // kept weight tracking in its own separate database.
    private val challengeRepository = ChallengeRepository(AppDatabase.getInstance(application))

    // Plain in-memory state doesn't survive the app's process being killed
    // in the background, which reads as "the range keeps resetting" even
    // though nothing was wrong within a single session. Persisting the two
    // dates to SharedPreferences makes the choice stick until the user
    // actually changes it.
    private val prefs: SharedPreferences =
        application.getSharedPreferences("weight_prefs", Context.MODE_PRIVATE)

    private val rangeStart = MutableStateFlow(
        LocalDate.ofEpochDay(
            prefs.getLong(KEY_RANGE_START, LocalDate.now().minusDays(29).toEpochDay())
        )
    )
    private val rangeEnd = MutableStateFlow(
        LocalDate.ofEpochDay(prefs.getLong(KEY_RANGE_END, LocalDate.now().toEpochDay()))
    )

    private val dayCategoryByDate: Flow<Map<LocalDate, DayCompletionCategory>> = combine(
        challengeRepository.challenge,
        challengeRepository.tasks,
        challengeRepository.allChecks
    ) { challenge, tasks, checks ->
        if (challenge == null) {
            emptyMap()
        } else {
            val status = ChallengeDates.status(challenge)
            val checksByDay = checks.groupBy { it.dayNumber }
            (1 until status.todayDayNumber).mapNotNull { day ->
                val completed = checksByDay[day].orEmpty().count { it.isChecked }
                val category = completionCategory(completed, tasks.size) ?: return@mapNotNull null
                status.startDate.plusDays((day - 1).toLong()) to category
            }.toMap()
        }
    }

    val uiState: StateFlow<WeightUiState> = combine(
        repository.allEntries,
        rangeStart,
        rangeEnd,
        dayCategoryByDate
    ) { entries, start, end, categoryByDate ->
        val sorted = entries.sortedBy { it.epochDay }
        val inRange = sorted.filter {
            val date = LocalDate.ofEpochDay(it.epochDay)
            !date.isBefore(start) && !date.isAfter(end)
        }
        val change = if (inRange.size >= 2) inRange.last().weightLbs - inRange.first().weightLbs else null
        val categoriesInRange = categoryByDate.filterKeys { !it.isBefore(start) && !it.isAfter(end) }

        WeightUiState(
            allEntries = sorted,
            rangeStart = start,
            rangeEnd = end,
            entriesInRange = inRange,
            weightChangeInRange = change,
            dayCategoryInRange = categoriesInRange
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeightUiState(rangeStart = rangeStart.value, rangeEnd = rangeEnd.value)
    )

    fun logWeight(date: LocalDate, weightLbs: Float) {
        viewModelScope.launch {
            repository.logWeight(date, weightLbs)
            // The saved range is a fixed pair of dates, not a rolling
            // "last 30 days" — so it goes stale as real time passes and a
            // freshly logged entry (today's, most commonly) can land
            // outside it. Widen the range just enough to show it.
            when {
                date.isAfter(rangeEnd.value) -> setRange(rangeStart.value, date)
                date.isBefore(rangeStart.value) -> setRange(date, rangeEnd.value)
            }
        }
    }

    fun setRange(start: LocalDate, end: LocalDate) {
        val (newStart, newEnd) = if (start.isAfter(end)) end to start else start to end
        rangeStart.value = newStart
        rangeEnd.value = newEnd
        prefs.edit()
            .putLong(KEY_RANGE_START, newStart.toEpochDay())
            .putLong(KEY_RANGE_END, newEnd.toEpochDay())
            .apply()
    }

    private companion object {
        const val KEY_RANGE_START = "range_start_epoch_day"
        const val KEY_RANGE_END = "range_end_epoch_day"
    }
}
