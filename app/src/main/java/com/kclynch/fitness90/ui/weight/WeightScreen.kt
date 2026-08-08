package com.kclynch.fitness90.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kclynch.fitness90.ui.components.LogWeightDialog
import com.kclynch.fitness90.ui.components.WeightLineChart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(viewModel: WeightViewModel) {
    val state by viewModel.uiState.collectAsState()

    var showLogDialog by remember { mutableStateOf(false) }
    var logDialogInitialDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val entriesByDate = remember(state.allEntries) {
        state.allEntries.associateBy { LocalDate.ofEpochDay(it.epochDay) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weight Tracking", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                logDialogInitialDate = LocalDate.now()
                showLogDialog = true
            }) {
                Text("Log Today's Weight")
            }
            OutlinedButton(onClick = {
                logDialogInitialDate = LocalDate.now()
                showLogDialog = true
            }) {
                Text("Log / Edit a Date")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Date range", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { showStartPicker = true }) {
                Text("From: ${state.rangeStart.format(dateFormatter)}")
            }
            OutlinedButton(onClick = { showEndPicker = true }) {
                Text("To: ${state.rangeEnd.format(dateFormatter)}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { viewModel.setRange(LocalDate.now().minusDays(6), LocalDate.now()) },
                label = { Text("7D") }
            )
            AssistChip(
                onClick = { viewModel.setRange(LocalDate.now().minusDays(29), LocalDate.now()) },
                label = { Text("30D") }
            )
            AssistChip(
                onClick = { viewModel.setRange(LocalDate.now().minusDays(89), LocalDate.now()) },
                label = { Text("90D") }
            )
            AssistChip(
                onClick = {
                    val earliest = state.allEntries.minOfOrNull { LocalDate.ofEpochDay(it.epochDay) } ?: LocalDate.now()
                    viewModel.setRange(earliest, LocalDate.now())
                },
                label = { Text("All") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val changeText = state.weightChangeInRange?.let { change ->
            val sign = if (change > 0) "+" else ""
            "$sign%.1f lbs".format(change)
        } ?: "Not enough data in range"
        Text(
            text = "Change over range: $changeText",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        WeightLineChart(
            entries = state.entriesInRange,
            rangeStart = state.rangeStart,
            rangeEnd = state.rangeEnd,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }

    if (showLogDialog) {
        LogWeightDialog(
            initialDate = logDialogInitialDate,
            existingWeightLookup = { date -> entriesByDate[date]?.weightLbs },
            onDismiss = { showLogDialog = false },
            onConfirm = { date, weight ->
                viewModel.logWeight(date, weight)
                showLogDialog = false
            }
        )
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.rangeStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val newStart = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setRange(newStart, state.rangeEnd)
                    }
                    showStartPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.rangeEnd.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val newEnd = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setRange(state.rangeStart, newEnd)
                    }
                    showEndPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
