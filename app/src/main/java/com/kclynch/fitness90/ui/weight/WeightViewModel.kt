package com.kclynch.fitness90.ui.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kclynch.fitness90.data.WeightDatabase
import com.kclynch.fitness90.data.WeightEntry
import com.kclynch.fitness90.data.WeightRepository
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
    val weightChangeInRange: Float? = null
)

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeightRepository(WeightDatabase.getInstance(application))

    private val rangeStart = MutableStateFlow(LocalDate.now().minusDays(29))
    private val rangeEnd = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<WeightUiState> = combine(
        repository.allEntries,
        rangeStart,
        rangeEnd
    ) { entries, start, end ->
        val sorted = entries.sortedBy { it.epochDay }
        val inRange = sorted.filter {
            val date = LocalDate.ofEpochDay(it.epochDay)
            !date.isBefore(start) && !date.isAfter(end)
        }
        val change = if (inRange.size >= 2) inRange.last().weightLbs - inRange.first().weightLbs else null

        WeightUiState(
            allEntries = sorted,
            rangeStart = start,
            rangeEnd = end,
            entriesInRange = inRange,
            weightChangeInRange = change
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeightUiState(rangeStart = rangeStart.value, rangeEnd = rangeEnd.value)
    )

    fun logWeight(date: LocalDate, weightLbs: Float) {
        viewModelScope.launch { repository.logWeight(date, weightLbs) }
    }

    fun setRange(start: LocalDate, end: LocalDate) {
        if (start.isAfter(end)) {
            rangeStart.value = end
            rangeEnd.value = start
        } else {
            rangeStart.value = start
            rangeEnd.value = end
        }
    }
}
