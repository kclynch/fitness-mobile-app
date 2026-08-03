package com.kclynch.fitness90.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kclynch.fitness90.ui.home.DayProgress

@Composable
fun DayPickerRow(
    totalDays: Int,
    selectedDay: Int,
    todayDayNumber: Int,
    dayProgress: Map<Int, DayProgress>,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDay) {
        listState.animateScrollToItem((selectedDay - 3).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items((1..totalDays).toList()) { day ->
            val progress = dayProgress[day]
            val isComplete = progress != null && progress.total > 0 && progress.completed >= progress.total
            val isPartial = progress != null && progress.completed > 0 && !isComplete
            val isSelected = day == selectedDay
            val isToday = day == todayDayNumber

            val backgroundColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isComplete -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape)
                    .border(
                        width = if (isToday && !isSelected) 2.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable { onDaySelected(day) }
            ) {
                Text(
                    text = day.toString(),
                    color = contentColor,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isPartial) {
                    Box(
                        modifier = Modifier
                            .padding(top = 34.dp)
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    )
                }
            }
        }
    }
}
