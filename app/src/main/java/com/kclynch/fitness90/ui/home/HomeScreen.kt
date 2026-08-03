package com.kclynch.fitness90.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kclynch.fitness90.data.ChecklistTask
import com.kclynch.fitness90.ui.components.ChecklistItemRow
import com.kclynch.fitness90.ui.components.ConfirmDeleteDialog
import com.kclynch.fitness90.ui.components.CountdownCard
import com.kclynch.fitness90.ui.components.DayPickerRow
import com.kclynch.fitness90.ui.components.TaskEditDialog
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: ChallengeUiState.Active, viewModel: ChallengeViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<ChecklistTask?>(null) }
    var deletingTask by remember { mutableStateOf<ChecklistTask?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showChangeDateDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d") }
    val selectedDate = remember(state.startDate, state.selectedDay) {
        state.startDate.plusDays((state.selectedDay - 1).toLong())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("90 Day Challenge") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Change start date") },
                            onClick = {
                                showMenu = false
                                showChangeDateDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add checklist item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CountdownCard(
                startDate = state.startDate,
                endDate = state.endDate,
                totalDays = state.totalDays,
                todayDayNumber = state.todayDayNumber,
                daysRemaining = state.daysRemaining,
                isFinished = state.isFinished
            )

            Spacer(modifier = Modifier.height(16.dp))

            DayPickerRow(
                totalDays = state.totalDays,
                selectedDay = state.selectedDay,
                todayDayNumber = state.todayDayNumber,
                dayProgress = state.dayProgress,
                onDaySelected = viewModel::selectDay
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day ${state.selectedDay} · ${selectedDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.selectedDay != state.todayDayNumber) {
                    TextButton(onClick = { viewModel.jumpToToday() }) {
                        Icon(Icons.Filled.Today, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Today")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No checklist items yet. Tap + to add your first one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(state.tasks, key = { it.id }) { task ->
                        ChecklistItemRow(
                            task = task,
                            isChecked = state.checkedTaskIds.contains(task.id),
                            onCheckedChange = { checked ->
                                viewModel.toggleTask(task.id, state.selectedDay, checked)
                            },
                            onEdit = { editingTask = task },
                            onDelete = { deletingTask = task }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskEditDialog(
            title = "Add checklist item",
            initialValue = "",
            confirmLabel = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { title ->
                viewModel.addTask(title)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        TaskEditDialog(
            title = "Edit checklist item",
            initialValue = task.title,
            confirmLabel = "Save",
            onDismiss = { editingTask = null },
            onConfirm = { newTitle ->
                viewModel.renameTask(task, newTitle)
                editingTask = null
            }
        )
    }

    deletingTask?.let { task ->
        ConfirmDeleteDialog(
            itemTitle = task.title,
            onDismiss = { deletingTask = null },
            onConfirm = {
                viewModel.deleteTask(task)
                deletingTask = null
            }
        )
    }

    if (showChangeDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showChangeDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newStart = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.updateStartDate(newStart)
                    }
                    showChangeDateDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeDateDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
