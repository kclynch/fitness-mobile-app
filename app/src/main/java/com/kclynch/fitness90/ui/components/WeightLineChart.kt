package com.kclynch.fitness90.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kclynch.fitness90.data.DayCompletionCategory
import com.kclynch.fitness90.data.WeightEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val PerfectDotGold = Color(0xFFFFD700)

@Composable
fun WeightLineChart(
    entries: List<WeightEntry>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    dayCategoryInRange: Map<LocalDate, DayCompletionCategory> = emptyMap(),
    showDayQuality: Boolean = false,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    if (entries.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Log at least two weigh-ins in this range to see a graph.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val minWeight = entries.minOf { it.weightLbs }
    val maxWeight = entries.maxOf { it.weightLbs }
    val weightSpan = (maxWeight - minWeight).let { if (it < 1f) 1f else it }
    val totalDaySpan = ChronoUnit.DAYS.between(rangeStart, rangeEnd).toFloat().let { if (it < 1f) 1f else it }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Column(modifier = modifier) {
        Text(
            text = "%.1f lbs".format(maxWeight),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Reserve a thin strip at the bottom for the day-quality dots
            // so they don't sit on top of the weight line itself.
            val stripHeight = if (showDayQuality) 16.dp.toPx() else 0f
            val plotHeight = size.height - stripHeight

            val gridYs = listOf(0f, 0.5f, 1f).map { plotHeight * (1f - it) }
            gridYs.forEach { y ->
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val xForDate = { date: LocalDate ->
                val xFraction = (ChronoUnit.DAYS.between(rangeStart, date).toFloat() / totalDaySpan).coerceIn(0f, 1f)
                xFraction * size.width
            }

            val points = entries.map { entry ->
                val date = LocalDate.ofEpochDay(entry.epochDay)
                val yFraction = ((entry.weightLbs - minWeight) / weightSpan).coerceIn(0f, 1f)
                Offset(x = xForDate(date), y = plotHeight - yFraction * plotHeight)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            points.forEach { point ->
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
            }

            if (showDayQuality) {
                val stripY = plotHeight + stripHeight / 2f
                dayCategoryInRange.forEach { (date, category) ->
                    val dotColor = if (category == DayCompletionCategory.PERFECT) {
                        PerfectDotGold
                    } else {
                        category.color()
                    }
                    drawCircle(color = dotColor, radius = 5.dp.toPx(), center = Offset(xForDate(date), stripY))
                }
            }
        }
        Text(
            text = "%.1f lbs".format(minWeight),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = rangeStart.format(dateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = rangeEnd.format(dateFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showDayQuality) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DayQualityLegendItem("Perfect", PerfectDotGold)
                DayQualityLegendItem("Missed 1", DayCompletionCategory.GREEN.color())
                DayQualityLegendItem("Missed 2-3", DayCompletionCategory.YELLOW.color())
                DayQualityLegendItem("Missed 4+", DayCompletionCategory.RED.color())
            }
        }
    }
}

@Composable
private fun DayQualityLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = " $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
